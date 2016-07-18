package org.rust.lang.core.types

import org.rust.lang.core.psi.*
import org.rust.lang.core.resolve.indexes.RustImplIndex
import org.rust.lang.core.types.util.resolvedType

abstract class RustStructOrEnumTypeBase(struct: RustStructOrEnumItemElement) : RustType {

    override val inherentImpls: Sequence<RustImplItemElement> by lazy {
        RustImplIndex.findImplsFor(struct).asSequence().filter { it.traitRef == null }
    }

}
