package org.oppia.util.parser

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.util.Log
import org.oppia.util.R
import org.oppia.util.parser.StringUtils.LI_TAG
import org.oppia.util.parser.StringUtils.OL_TAG
import org.oppia.util.parser.StringUtils.UL_TAG
import org.xml.sax.XMLReader
import java.util.*

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.
/**
 * Implements support for ordered ({@code <ol>}) and unordered ({@code <ul>}) lists in to Android TextView.
 *
 * <ul> and <ol> tags are added to the [listParents] array and removed when the closing tag is reached.
 *
 * <li> tags are handled corresponding to the parent tag.
 *
 * Reference: https://github.com/daphliu/android-spannable-list-sample/tree/master/app/src/main/java/com/daphneliu/sample/listspansample
 */
class CustomTagHandler(private val context: Context) : Html.TagHandler {
  private val INDENT_PX = context.resources.getDimensionPixelSize(R.dimen.bullet_leading_margin)
  private val LIST_ITEM_INDENT_PX = INDENT_PX * 2
  private val BULLET_SPAN = BulletSpanWithRadius(context, INDENT_PX)

  private val lists: Stack<ListTag> = Stack<ListTag>()

  /**
   * @see android.text.Html.TagHandler.handleTag
   */
  override fun handleTag(
    opening: Boolean,
    tag: String,
    output: Editable,
    xmlReader: XMLReader?
  ) {
    when {
      UL_TAG == tag -> {
        if (opening) {
          // handle <ul>
          lists.push(Ul())
        } else {
          // handle </ul>
          lists.pop()
        }
      }
      OL_TAG == tag -> {
        if (opening) {
          // handle <ol>
          lists.push(Ol())
          // use default start index of 1
        } else {
          // handle </ol>
          lists.pop()
        }
      }
      LI_TAG == tag -> {
        try {
          if (opening) {
            // handle <li>
            lists.peek().openItem(output)
          } else {
            // handle </li>
            lists.peek().closeItem(output, lists.size)
          }
        } catch (e: EmptyStackException) {
          Log.d("HtmlTagHandler", "No Such Element Found" + lists.isEmpty())
        }
      }
      else -> {
        Log.d("TagHandler", "Found an unsupported tag $tag")
      }
    }
  }

  /**
   * Abstract super class for [Ul] and [Ol].
   */
  private abstract class ListTag {
    /**
     * Opens a new list item.
     *
     * @param text
     */
    open fun openItem(text: Editable) {
      if (text.length > 0 && text[text.length - 1] != '\n') {
        text.append("\n")
      }
      val len = text.length
      text.setSpan(this, len, len, Spanned.SPAN_MARK_MARK)
    }

    /**
     * Closes a list item.
     *
     * @param text
     * @param indentation
     */
    fun closeItem(text: Editable, indentation: Int) {
      if (text.length > 0 && text[text.length - 1] != '\n') {
        text.append("\n")
      }
      val replaces = getReplaces(text, indentation)
      val len = text.length
      val listTag = getLast(text)
      val where = text.getSpanStart(listTag)
      text.removeSpan(listTag)
      if (where != len) {
        for (replace in replaces) {
          text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }

    protected abstract fun getReplaces(text: Editable?, indentation: Int): Array<Any>
    /**
     * Note: This knows that the last returned object from getSpans() will be the most recently added.
     *
     * @see Html
     */
    private fun getLast(text: Spanned): ListTag? {
      val listTags = text.getSpans(0, text.length, ListTag::class.java)
      return if (listTags.size == 0) {
        null
      } else listTags[listTags.size - 1]
    }
  }

  /**
   * Class representing the unordered list (`<ul>`) HTML tag.
   */
  private inner class Ul : ListTag() {
    override fun getReplaces(
      text: Editable?,
      indentation: Int
    ): Array<Any> { // Nested BulletSpans increases distance between BULLET_SPAN and text, so we must prevent it.
      var bulletMargin: Int = INDENT_PX
      if (indentation > 1) {
        bulletMargin = INDENT_PX - BULLET_SPAN.getLeadingMargin(true)
        if (indentation > 2) { // This get's more complicated when we add a LeadingMarginSpan into the same line:
// we have also counter it's effect to BulletSpan
          bulletMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX
        }
      }
      return arrayOf(
        LeadingMarginSpan.Standard(LIST_ITEM_INDENT_PX * (indentation - 1)),
        BulletSpanWithRadius(context,bulletMargin)
      )
    }
  }

  /**
   * Class representing the ordered list (`<ol>`) HTML tag.
   */
  private inner class Ol
  /**
   * Creates a new `<ul>` with start index of 1.
   */ @JvmOverloads constructor(private var nextIdx: Int = 1) : ListTag() {
    override fun openItem(text: Editable) {
      super.openItem(text)
      text.append(Integer.toString(nextIdx++)).append(". ")
    }

    override fun getReplaces(
      text: Editable?,
      indentation: Int
    ): Array<Any> {
      var numberMargin: Int = LIST_ITEM_INDENT_PX * (indentation - 1)
      if (indentation > 2) { // Same as in ordered lists: counter the effect of nested Spans
        numberMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX
      }
      return arrayOf(LeadingMarginSpan.Standard(numberMargin))
    }
  }
}
