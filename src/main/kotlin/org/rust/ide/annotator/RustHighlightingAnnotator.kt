package org.rust.ide.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.rust.ide.colors.RustColor
import org.rust.ide.highlight.RustHighlighter
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.impl.mixin.isMut
import org.rust.lang.core.psi.util.elementType
import org.rust.lang.core.psi.visitors.RustComputingVisitor

// Highlighting logic here should be kept in sync with tags in RustColorSettingsPage
class RustHighlightingAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val highlightingInfo = if (element is RustReferenceElement) {
            val color = if (element is RustPathElement && element.isPrimitive) {
                RustColor.PRIMITIVE_TYPE
            } else {
                val ref = element.reference.resolve() ?: return
                // Highlight the element dependent on what it's referencing.
                HighlightingVisitor().compute(ref).color
            }

            HighlightingInfo(element.referenceNameElement, color)
        } else {
            HighlightingVisitor().compute(element)
        }

        highlightingInfo.apply(holder)
    }

    private data class HighlightingInfo(val element: PsiElement?, val color: RustColor?) {
        fun apply(holder: AnnotationHolder) {
            if (element != null && color != null) {
                holder.createInfoAnnotation(element, null).textAttributes = color.textAttributesKey
            }
        }
    }

    private class HighlightingVisitor : RustComputingVisitor<HighlightingInfo>(default = HighlightingInfo(null, null)) {

        fun highlight(element: PsiElement?, color: RustColor?) = set { HighlightingInfo(element, color) }

        override fun visitLitExpr(o: RustLitExprElement) {
            // Re-highlight literals in attributes
            if (o.parent is RustMetaItemElement) {
                val literal = o.firstChild
                highlight(literal, RustHighlighter.map(literal.elementType))
            }
        }

        override fun visitTypeParam(o: RustTypeParamElement) = highlight(o.identifier, RustColor.TYPE_PARAMETER)

        override fun visitAttr(o: RustAttrElement) = highlight(o, RustColor.ATTRIBUTE)

        override fun visitTraitRef(o: RustTraitRefElement) = highlight(o.path.identifier, RustColor.TRAIT)

        override fun visitPatBinding(o: RustPatBindingElement) {
            if (o.isMut) {
                highlight(o.identifier, RustColor.MUT_BINDING)
            }
        }

        override fun visitEnumItem(o: RustEnumItemElement)       = highlight(o.identifier, RustColor.ENUM)
        override fun visitEnumVariant(o: RustEnumVariantElement) = highlight(o.identifier, RustColor.ENUM_VARIANT)

        override fun visitStructItem(o: RustStructItemElement)   = highlight(o.identifier, RustColor.STRUCT)
        override fun visitTraitItem(o: RustTraitItemElement)     = highlight(o.identifier, RustColor.TRAIT)
        override fun visitModDeclItem(o: RustModDeclItemElement) = highlight(o.identifier, RustColor.MODULE)
        override fun visitModItem(o: RustModItemElement)         = highlight(o.identifier, RustColor.MODULE)

        override fun visitFieldDecl(o: RustFieldDeclElement)     = highlight(o.identifier, RustColor.FIELD)

        override fun visitExternCrateItem(o: RustExternCrateItemElement) = highlight(o.identifier, RustColor.CRATE)

        override fun visitMacroInvocation(m: RustMacroInvocationElement) = highlight(m, RustColor.MACRO)
        override fun visitMethodCallExpr(o: RustMethodCallExprElement)   = highlight(o.identifier, RustColor.INSTANCE_METHOD)
        override fun visitFnItem(o: RustFnItemElement)                   = highlight(o.identifier, RustColor.FUNCTION_DECLARATION)

        override fun visitImplMethodMember(o: RustImplMethodMemberElement) =
            highlight(o.identifier, if (o.isStatic) RustColor.STATIC_METHOD else RustColor.INSTANCE_METHOD)
        override fun visitTraitMethodMember(o: RustTraitMethodMemberElement) =
            highlight(o.identifier, if (o.isStatic) RustColor.STATIC_METHOD else RustColor.INSTANCE_METHOD)

    }
}
