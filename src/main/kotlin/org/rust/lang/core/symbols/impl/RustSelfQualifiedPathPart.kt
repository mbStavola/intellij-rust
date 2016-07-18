package org.rust.lang.core.symbols.impl

import org.rust.lang.core.symbols.RustQualifiedPathPart

object RustSelfQualifiedPathPart : RustQualifiedPathPart {

    override val name: String
        get() = "self"

}
