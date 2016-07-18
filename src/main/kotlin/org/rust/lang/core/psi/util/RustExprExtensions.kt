package org.rust.lang.core.psi.util

import org.rust.lang.core.psi.*

/**
 *  `RustExprElement` related extensions
 */

val RustStructExprElement.fields: List<RustNamedElement> get() {
    val structOrEnum = path.reference.resolve() ?: return emptyList()
    return when (structOrEnum) {
        is RustStructItemElement  -> structOrEnum.fields
        is RustEnumVariantElement -> structOrEnum.enumStructArgs?.fieldDeclList ?: emptyList()
        else -> emptyList()
    }
}


/**
 * Extracts [RustLitExprElement] raw value
 */
val RustLitExprElement.stringLiteralValue: String?
    get() = ((stringLiteral ?: rawStringLiteral) as? RustLiteral.Text)?.value
