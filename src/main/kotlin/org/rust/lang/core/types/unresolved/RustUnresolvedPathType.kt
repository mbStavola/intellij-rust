package org.rust.lang.core.types.unresolved

import org.rust.lang.core.psi.RustPathElement
import org.rust.lang.core.psi.referenceName
import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.types.visitors.RustUnresolvedTypeVisitor

class RustUnresolvedPathType(val path: RustQualifiedPath) : RustUnresolvedType {

    override fun <T> accept(visitor: RustUnresolvedTypeVisitor<T>): T = visitor.visitPathType(this)

}
