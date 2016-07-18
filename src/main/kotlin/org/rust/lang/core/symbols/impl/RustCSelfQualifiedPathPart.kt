package org.rust.lang.core.symbols.impl

import org.rust.lang.core.symbols.RustQualifiedPathPart

object RustCSelfQualifiedPathPart : RustQualifiedPathPart {

    override val name: String
        get() = "Self"

}
