package org.rust.lang.core.resolve.indexes

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.AbstractStubIndex
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.io.KeyDescriptor
import org.rust.lang.core.RustFileElementType
import org.rust.lang.core.psi.*
import org.rust.lang.core.types.RustEnumType
import org.rust.lang.core.types.RustStructOrEnumTypeBase
import org.rust.lang.core.types.RustStructType
import org.rust.lang.core.types.RustType
import org.rust.lang.core.types.unresolved.RustUnresolvedPathType
import org.rust.lang.core.types.unresolved.RustUnresolvedType
import org.rust.lang.core.types.util.decay
import org.rust.lang.core.types.util.resolvedType
import org.rust.lang.core.types.visitors.impl.RustEqualityUnresolvedTypeVisitor
import org.rust.lang.core.types.visitors.impl.RustHashCodeComputingUnresolvedTypeVisitor
import java.io.DataInput
import java.io.DataOutput


class RustImplIndex : AbstractStubIndex<RustImplIndex.Key, RustImplItemElement>() {

    /**
     * This wrapper is required due to a subtle bug in the [com.intellij.util.indexing.MemoryIndexStorage], involving
     * use of the object's `hashCode`, while [com.intellij.util.indexing.MapIndexStorage] being using the one
     * impose by the [KeyDescriptor]
     */
    data class Key(val type: RustUnresolvedType) {

        override fun equals(other: Any?): Boolean =
            other is Key &&
            other.type.accept(
                object : RustEqualityUnresolvedTypeVisitor(type) {
                    /**
                     * Compare hole-containing types
                     */
                    override fun visitPathType(type: RustUnresolvedPathType): Boolean =
                        lop is RustUnresolvedPathType
                }
            ) && (hashCode() == other.hashCode() || throw Exception("WTF"))

        override fun hashCode(): Int =
            type.accept(
                object: RustHashCodeComputingUnresolvedTypeVisitor() {
                    override fun visitPathType(type: RustUnresolvedPathType): Int = 0xDEADBAE
                }
            )

    }

    companion object {

        fun findNonStaticMethodsFor(target: RustType, project: Project): Sequence<RustImplMethodMemberElement> =
            findMethodsFor(target, project)
                .filter { !it.isStatic }

        fun findStaticMethodsFor(target: RustType, project: Project): Sequence<RustImplMethodMemberElement> =
            findMethodsFor(target, project)
                .filter { it.isStatic }

        fun findMethodsFor(target: RustType, project: Project): Sequence<RustImplMethodMemberElement> =
            findImplsFor(target, project)
                .flatMap { it.implBody?.implMethodMemberList.orEmpty().asSequence() }

        fun findImplsFor(target: RustType, project: Project): Sequence<RustImplItemElement> =
            findImplsForInternal(target.decay, project)
                .filter {
                    impl -> impl.type?.let { it.resolvedType == target} ?: false
                }

        fun findImplsFor(target: RustStructOrEnumItemElement): Sequence<RustImplItemElement> {
            val impls =
                if (target is RustStructItemElement)
                    findImplsForInternal(RustStructType(target).decay, target.project)
                else if (target is RustEnumItemElement)
                    findImplsForInternal(RustEnumType(target).decay, target.project)
                else
                    throw IllegalStateException("Panic! `RustStructOrEnumItemElement` may not be extended by the classes beyond `RustStructItemElement` and `RustEnumItemElement`")

            return impls.filter { impl ->
                impl.type?.let {
                    it.resolvedType.let { ty ->
                        ty is RustStructOrEnumTypeBase && ty.item == target
                    }
                } ?: false
            }
        }

        private fun findImplsForInternal(target: RustUnresolvedType, project: Project): Sequence<RustImplItemElement> {
            val found: MutableList<RustImplItemElement> = arrayListOf()

            StubIndex
                .getInstance()
                .processElements(
                    KEY,
                    Key(target),
                    project,
                    GlobalSearchScope.allScope(project),
                    RustImplItemElement::class.java,
                    {
                        found.add(it)
                        true /* continue */
                    })

            return found.asSequence()

        }

        val KEY: StubIndexKey<Key, RustImplItemElement> =
            StubIndexKey.createIndexKey("org.rust.lang.core.stubs.index.RustImplIndex")

    }

    override fun getVersion(): Int = RustFileElementType.stubVersion

    override fun getKey(): StubIndexKey<Key, RustImplItemElement> = KEY

    override fun getKeyDescriptor(): KeyDescriptor<Key> =
        object: KeyDescriptor<Key> {

            override fun isEqual(lop: Key?, rop: Key?): Boolean =
                lop === rop || lop?.equals(rop) ?: false

            override fun getHashCode(value: Key?): Int =
                value?.hashCode() ?: -1

            override fun read(`in`: DataInput): Key? {
                return RustUnresolvedType.deserialize(`in`)?.let { Key(it) }
            }

            override fun save(out: DataOutput, value: Key?) {
                RustUnresolvedType.serialize(value?.type, out)
            }
        }
}


class RustAliasIndex : StringStubIndexExtension<RustUseItemElement>() {

    companion object {

        val KEY: StubIndexKey<String, RustUseItemElement> =
            StubIndexKey.createIndexKey("org.rust.lang.core.stubs.index.RustAliasIndex")

    }

    override fun getVersion(): Int = RustFileElementType.stubVersion

    override fun getKey(): StubIndexKey<String, RustUseItemElement> = KEY

}
