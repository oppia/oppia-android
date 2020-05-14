package org.oppia.util.parser

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_MARK_MARK
import android.text.style.LeadingMarginSpan
import java.util.Stack
import org.oppia.util.parser.LiTagHandler.ListTag
import org.oppia.util.parser.StringUtils.LI_TAG
import org.oppia.util.parser.StringUtils.OL_TAG
import org.oppia.util.parser.StringUtils.UL_TAG
import org.xml.sax.XMLReader

/**
 * Called when the HTML parser reaches an opening or closing tag.
 * We only handle list tags and ignore other tags.
 *
 * <ul> and <ol> tags are pushed to the [lists] stack and popped when the closing tag is reached.
 *
 * <li> tags are handled by the [ListTag] instance corresponding to the parent tag.
 *
 * Reference: https://github.com/daphliu/android-spannable-list-sample/tree/master/app/src/main/java/com/daphneliu/sample/listspansample
 */
class LiTagHandler(private val context: Context) : Html.TagHandler {

  private val lists = Stack<ListTag>()

  override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
    when (tag) {
      UL_TAG -> if (opening) {
        // handle <ul>
        lists.push(Ul())
      } else {
        // handle </ul>
        lists.pop()
      }
      OL_TAG -> if (opening) {
        // handle <ol>
        lists.push(Ol())
      } else {
        // handle </ol>
        lists.pop()
      }
      LI_TAG -> if (opening) {
        // handle <li>
        lists.peek().openItem(output)
      } else {
        // handle </li>
        lists.peek().closeItem(output, indentation = lists.size - 1)
      }
    }
  }

  /**
   * Handler for <li> tags. Subclasses set the bullet appearance.
   */
  private interface ListTag {

    /**
     * Called when an opening <li> tag is encountered.
     *
     * Inserts an invisible [Mark] span that doesn't do any styling.
     * Instead, [closeItem] will later find the location of this span so it knows where the opening tag was.
     */
    fun openItem(text: Editable)

    /**
     * Called when a closing </li> tag is encountered.
     *
     * Pops out the invisible [Mark] span and uses it to get the opening tag location.
     * Then, sets a [LeadingMarginSpan] from the opening tag position to closing tag position.
     */
    fun closeItem(text: Editable, indentation: Int)
  }

  /**
   * Subclass of [ListTag] for unordered lists.
   */
  private inner class Ul : ListTag {

    override fun openItem(text: Editable) {
      appendNewLine(text)
      start(text, BulletListItem())
    }

    override fun closeItem(text: Editable, indentation: Int) {
      appendNewLine(text)

      getLast<BulletListItem>(text)?.let { mark ->
        setSpanFromMark(text, mark, CustomBulletSpan(context, indentation, "•"))
      }
    }
  }

  /**
   * Subclass of [ListTag] for ordered lists.
   */
  private inner class Ol : ListTag {

    private var index = 1

    override fun openItem(text: Editable) {
      appendNewLine(text)
      start(text, NumberListItem(index))
      index++
    }

    override fun closeItem(text: Editable, indentation: Int) {
      appendNewLine(text)
      getLast<NumberListItem>(text)?.let { mark ->
        setSpanFromMark(text, mark, CustomBulletSpan(context, indentation, "${mark.number}."))
      }
    }
  }

  /**
   * These static methods are based on the Android Html class source code.
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java
   */
  companion object {

    /**
     * Appends a new line to [text] if it doesn't already end in a new line
     */
    private fun appendNewLine(text: Editable) {
      if (text.isNotEmpty() && text.last() != '\n') {
        text.append("\n")
      }
    }

    /**
     * Returns the most recently added span of type [T] in [text].
     *
     * Invisible marking spans are inserted to record the location of opening HTML tags in the text.
     * We do this rather than using a stack in case text is inserted and the relative location shifts around.
     *
     * The last span corresponds to the top of the "stack".
     */
    private inline fun <reified T : Mark> getLast(text: Spanned) =
      text.getSpans(0, text.length, T::class.java).lastOrNull()

    /**
     * Pops out the invisible [mark] span and uses it to get the opening tag location.
     * Then, sets a span from the opening tag position to closing tag position.
     */
    private fun setSpanFromMark(text: Spannable, mark: Mark, styleSpan: Any) {
      // Find the location where the mark is inserted in the string.
      val markerLocation = text.getSpanStart(mark)
      // Remove the mark now that the location is saved
      text.removeSpan(mark)

      val end = text.length
      if (markerLocation != end) {
        text.setSpan(styleSpan, markerLocation, end, SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }

    /**
     * Inserts an invisible [mark] span that doesn't do any styling.
     * Instead, [setSpanFromMark] will later find the location of this span so it knows where the opening tag was.
     */
    private fun start(text: Spannable, mark: Mark) {
      val currentPosition = text.length
      text.setSpan(mark, currentPosition, currentPosition, SPAN_MARK_MARK)
    }
  }
}
