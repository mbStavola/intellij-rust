package org.rust.ide.template.macros

import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.codeInsight.template.macro.MacroBase

class RustSuggestIndexNameMacro : MacroBase("rustSuggestIndexName", "rustSuggestIndexName()") {
    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext, quick: Boolean): Result? {
        if (params.isNotEmpty()) return null
        val pats = getPatBindingNamesVisibleAt(context.psiElementAtStartOffset)
        return ('i'..'z')
            .map { it.toString() }
            .find { it !in pats }
            ?.let { TextResult(it) }
    }
}
