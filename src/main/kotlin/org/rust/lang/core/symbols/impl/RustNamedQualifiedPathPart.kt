package org.rust.lang.core.symbols.impl

import org.rust.lang.core.symbols.RustQualifiedPathPart

data class RustNamedQualifiedPathPart(override val name: String) : RustQualifiedPathPart
