package org.rust.lang.core.psi.util

import org.rust.lang.core.psi.*
import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.types.unresolved.RustUnresolvedPathType
import org.rust.lang.core.types.util.type

/**
 *  `RustItemElement` related extensions
 */


val RustGenericDeclaration.typeParams: List<RustTypeParamElement>
    get() = genericParams?.typeParamList.orEmpty()

//val RustImplItemElement.ref: RustQualifiedPath?
//    get() = stub?.ref ?: if (traitRef != null) traitRef?.path else (type?.type as? RustUnresolvedPathType?)?.path

val RustUseItemElement.aliased: String?
    get() = stub?.alias ?: alias?.name
