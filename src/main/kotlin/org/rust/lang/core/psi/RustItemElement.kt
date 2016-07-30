package org.rust.lang.core.psi

import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.symbols.RustQualifiedPathPart

interface RustItemElement : RustVisibilityOwner, RustOuterAttributeOwner

/**
 * Returns a fully-qualified [RustQualifiedPath] representing a 'path' to this [RustItemElement]
 * from the crate root, like `foo::bar::baz`. Returns null for the crate's root.
 *
 * This path is NOT guaranteed to be unique: items from different crates
 * can have the same path within respective crates.
 */
val RustItemElement.canonicalCratePath: RustQualifiedPath?
    get() =
        RustQualifiedPath.create(
            RustQualifiedPathPart.from(name),
            qualifier = containingMod?.canonicalCratePath,
            fullyQualified = true
        )

