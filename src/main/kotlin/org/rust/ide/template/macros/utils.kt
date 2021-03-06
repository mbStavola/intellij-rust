package org.rust.ide.template.macros

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RustBlockElement
import org.rust.lang.core.psi.RustItemElement
import org.rust.lang.core.psi.RustPatBindingElement
import org.rust.lang.core.psi.impl.RustFile
import org.rust.lang.core.resolve.RustResolveEngine

fun getPatBindingNamesVisibleAt(place: PsiElement?): Set<String> {
    if (place == null) return emptySet()
    return RustResolveEngine.enumerateScopesFor(place)
        .takeWhile {
            // we are only interested in local scopes
            if (it is RustItemElement) {
                // workaround diamond inheritance issue
                // (ambiguity between RustItemElement.parent & RustResolveScope.parent)
                val item: RustItemElement = it
                item.parent is RustBlockElement
            } else {
                it !is RustFile
            }
        }
        .flatMap { RustResolveEngine.declarations(it, place) }
        .mapNotNull { (it as? RustPatBindingElement)?.name }
        .toHashSet()
}
