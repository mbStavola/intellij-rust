package org.rust.lang.core.types.visitors.impl

import org.rust.lang.core.psi.RustCompositeElement
import org.rust.lang.core.resolve.RustResolveEngine
import org.rust.lang.core.types.*
import org.rust.lang.core.types.unresolved.*
import org.rust.lang.core.types.visitors.RustUnresolvedTypeVisitor
import org.rust.lang.core.types.visitors.impl.RustTypificationEngine

open class RustTypeResolvingVisitor(private val pivot: RustCompositeElement) : RustUnresolvedTypeVisitor<RustType> {

    private fun visit(type: RustUnresolvedType): RustType = type.accept(this)

    override fun visitUnknown(type: RustUnknownType): RustType = RustUnknownType

    override fun visitUnitType(type: RustUnitType): RustType = RustUnitType

    override fun visitTupleType(type: RustUnresolvedTupleType): RustType =
        RustTupleType(type.elements.map { visit(it) })

    override fun visitPathType(type: RustUnresolvedPathType): RustType =
        RustResolveEngine.resolve(type.path, pivot).let {
            when (it) {
                is RustResolveEngine.ResolveResult.Resolved -> RustTypificationEngine.typify(it.element!!)
                else                                        -> RustUnknownType
            }
        }

    override fun visitFunctionType(type: RustUnresolvedFunctionType): RustType =
        RustFunctionType(type.paramTypes.map { visit(it) }, visit(type.retType))

    override fun visitInteger(type: RustIntegerType): RustType = type

    override fun visitFloat(type: RustFloatType): RustType = type

    override fun visitString(type: RustStringType): RustType = type

    override fun visitChar(type: RustCharacterType): RustType = type

    override fun visitBoolean(type: RustBooleanType): RustType = type

    override fun visitReference(type: RustUnresolvedReferenceType): RustType = RustReferenceType(visit(type.referenced), type.mutable)
}

