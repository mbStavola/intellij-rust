package org.rust.lang.core.resolve.indexes

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
import org.rust.utils.Either
import java.io.DataInput
import java.io.DataOutput


class RustImplIndex : StringStubIndexExtension<RustImplItemElement>() {

    companion object {

        fun findImplsFor(target: RustStructOrEnumItemElement): Sequence<RustImplItemElement> =
            findImplsForInternal(Either.left(target))

        fun findImplsFor(target: RustTraitItemElement): Sequence<RustImplItemElement> =
            findImplsForInternal(Either.right(target))

        private fun findImplsByRefInternal(target: Either<RustStructOrEnumItemElement, RustTraitItemElement>): Sequence<RustImplItemElement> {
            val item = Either.apply(target) { item: RustItemElement -> item }
            return findImplsForInternal(target)
                .filter { impl ->
                    impl.ref?.let {
                        RustResolveEngine.resolve(it, impl).let {
                            it is RustResolveEngine.ResolveResult.Resolved &&
                                item.canonicalCratePath.isEqualTo((it.element as? RustItemElement)?.canonicalCratePath)
                        }
                    } ?: false
                }
        }

        private fun findImplsForInternal(target: Either<RustStructOrEnumItemElement, RustTraitItemElement>): Sequence<RustImplItemElement> {
            val (name, pivot) =
                Either.apply(target) {
                    item: RustItemElement -> Pair(item.name, item)
                }

            if (name == null)
                return emptySequence()

            val aliases: MutableList<String> = arrayListOf(name)

            StubIndex
                .getInstance()
                .processElements(
                            RustAliasIndex.KEY,
                            name,
                            pivot.project,
                            GlobalSearchScope.allScope(pivot.project),
                            RustUseItemElement::class.java,
                            {
                                val alias = it.aliased
                                if (alias != null)
                                    aliases.add(alias)

                                true /* continue */
                            }
                )

            val found: MutableList<RustImplItemElement> = arrayListOf()

            aliases.forEach { name ->
                StubIndex
                    .getInstance()
                    .processElements(
                        KEY,
                                name,
                                pivot.project,
                                GlobalSearchScope.allScope(pivot.project),
                                RustImplItemElement::class.java,
                                {
                                    found.add(it)
                                    true /* continue */
                                })
            }

            return found.asSequence()
        }

        val KEY: StubIndexKey<String, RustImplItemElement> =
            StubIndexKey.createIndexKey("org.rust.lang.core.stubs.index.RustImplIndex")

    }

    override fun getVersion(): Int = RustFileElementType.stubVersion

    override fun getKey(): StubIndexKey<String, RustImplItemElement> = KEY

}

class RustAliasIndex : StringStubIndexExtension<RustUseItemElement>() {

    companion object {

        val KEY: StubIndexKey<String, RustUseItemElement> =
            StubIndexKey.createIndexKey("org.rust.lang.core.stubs.index.RustAliasIndex")

    }

    override fun getVersion(): Int = RustFileElementType.stubVersion

    override fun getKey(): StubIndexKey<String, RustUseItemElement> = KEY

}
