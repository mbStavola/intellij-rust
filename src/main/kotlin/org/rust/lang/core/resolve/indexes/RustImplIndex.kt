package org.rust.lang.core.resolve.indexes

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.*
import com.intellij.util.io.KeyDescriptor
import org.rust.lang.core.RustFileElementType
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.util.aliased
import org.rust.lang.core.psi.util.ref
import org.rust.lang.core.resolve.RustResolveEngine
import org.rust.lang.core.stubs.RustFileStub
import org.rust.lang.core.stubs.index.RustModulesIndex
import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.symbols.isEqualTo
import org.rust.lang.core.symbols.unfold
import org.rust.lang.core.types.RustType
import org.rust.lang.core.types.unresolved.RustUnresolvedPathType
import org.rust.lang.core.types.unresolved.RustUnresolvedType
import org.rust.lang.core.types.util.resolvedType
import org.rust.lang.core.types.visitors.impl.RustEqualityUnresolvedTypeVisitor
import org.rust.utils.Either
import java.io.DataInput
import java.io.DataOutput



class RustImplIndex : AbstractStubIndex<RustUnresolvedType, RustImplItemElement>() {

    companion object {

        fun findImplsFor(target: RustStructOrEnumItemElement): Sequence<RustImplItemElement> =
            findImplsByRefInternal(Either.left(target))

        fun findImplsFor(target: RustTraitItemElement): Sequence<RustImplItemElement> =
            findImplsByRefInternal(Either.right(target))

        private fun findImplsByRefInternal(target: Either<RustStructOrEnumItemElement, RustTraitItemElement>): Sequence<RustImplItemElement> {
            val item = Either.apply(target) { item: RustItemElement -> item }
            val type = RustUnresolvedPathType(item.canonicalCratePath!!)

            return findImplsForInternal(type, item)
        }

        private fun findImplsForInternal(target: RustUnresolvedType, pivot: RustCompositeElement): Sequence<RustImplItemElement> {
            val found: MutableList<RustImplItemElement> = arrayListOf()

            val project = pivot.project

            StubIndex
                .getInstance()
                .processElements(
                    KEY,
                    target,
                    project,
                    GlobalSearchScope.allScope(project),
                    RustImplItemElement::class.java,
                    {
                        found.add(it)
                        true /* continue */
                    })

            val resolved = lazy { RustResolveEngine.resolve(target, pivot) }

            return found.asSequence()
                        .filter { impl -> impl.type?.let { it.resolvedType == resolved } ?: false }
        }

        val KEY: StubIndexKey<RustUnresolvedType, RustImplItemElement> =
            StubIndexKey.createIndexKey("org.rust.lang.core.stubs.index.RustImplIndex")

    }

    override fun getVersion(): Int = RustFileElementType.stubVersion

    override fun getKey(): StubIndexKey<RustUnresolvedType, RustImplItemElement> = KEY

    override fun getKeyDescriptor(): KeyDescriptor<RustUnresolvedType> =
        object: KeyDescriptor<RustUnresolvedType> {

            override fun isEqual(lop: RustUnresolvedType?, rop: RustUnresolvedType?): Boolean =
                lop === rop ||
                lop?.let {
                    rop?.accept(
                        object: RustEqualityUnresolvedTypeVisitor(it) {
                            /**
                             * Compare hole-containing types
                             */
                            override fun visitPathType(type: RustUnresolvedPathType): Boolean = true
                        }
                    )
                } ?: false

            override fun getHashCode(value: RustUnresolvedType?): Int = value?.hashCode() ?: -1

            override fun read(`in`: DataInput): RustUnresolvedType {
                throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun save(out: DataOutput, value: RustUnresolvedType?) {
                throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
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
