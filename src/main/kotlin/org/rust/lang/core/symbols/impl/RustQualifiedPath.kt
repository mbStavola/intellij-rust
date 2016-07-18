package org.rust.lang.core.symbols.impl

import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.symbols.RustQualifiedPathPart

internal class RustQualifiedPath(
    override val qualifier: RustQualifiedPath?,
    override val part: RustQualifiedPathPart,
    override val fullyQualified: Boolean
) : RustQualifiedPath {

    init {
        // FQ-path may not have qualifier
        check(!fullyQualified || qualifier == null)
    }

}
