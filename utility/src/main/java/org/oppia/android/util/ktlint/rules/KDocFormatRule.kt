package org.oppia.android.util.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.EditorConfigProperties
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

class KDocFormatRule : Rule("kdoc-format-closing") {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    if (node.psi is KtFile) {
      val kdocs = node.psi.children.filterIsInstance<KDocImpl>()
      kdocs.forEach { kdoc ->
        val lines = kdoc.text.lines()
        if (lines.last().contains("* */")) {
          emit(
            kdoc.startOffset + kdoc.text.lastIndexOf("* *"),
            "KDoc closing should be ' */' not '* */'",
            true
          )
          if (autoCorrect) {
            val kdocNode = kdoc.node
            val oldText = kdocNode.text
            val newText = oldText.replace("* */", " */")
            (kdocNode as? LeafPsiElement)?.rawReplaceWithText(newText)
          }
        }
      }
    }
  }
}