package org.oppia.util.parser

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.util.Log
import org.oppia.util.R
import org.oppia.util.parser.CustomTagHandler.ListItemTag
import org.oppia.util.parser.StringUtils.LI_TAG
import org.oppia.util.parser.StringUtils.OL_TAG
import org.oppia.util.parser.StringUtils.UL_TAG
import org.xml.sax.XMLReader
import java.util.*

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.
/**
 * Implements support for ordered ({@code <ol>}) and unordered ({@code <ul>}) lists in to Android TextView.
 * <ul> and <ol> tags are added to the [listParents] Stack and removed when the closing tag is reached.
 * <li> tags are handled [ListItemTag] instance corresponding to the parent tag.
 * Reference: https://github.com/daphliu/android-spannable-list-sample/tree/master/app/src/main/java/com/daphneliu/sample/listspansample
 */
class CustomTagHandler(private val context: Context) : Html.TagHandler {
  private val indent = context.resources.getDimensionPixelSize(R.dimen.bullet_leading_margin)
  private val listItemIndent = indent * 2
  private val bulletSpan = BulletSpanWithRadius(context, indent)

  private val listParents: Stack<ListItemTag> = Stack<ListItemTag>()

  override fun handleTag(
    opening: Boolean,
    tag: String,
    output: Editable,
    xmlReader: XMLReader?
  ) {
    when (tag) {
      UL_TAG -> {
        if (opening) {
          listParents.push(UnorderedListTag())
        } else {
          listParents.pop()
        }
      }
      OL_TAG -> {
        if (opening) {
          listParents.push(OrderedListTag())
        } else {
          listParents.pop()
        }
      }
      LI_TAG -> {
        try {
          if (opening) {
            listParents.peek().openItem(output)
          } else {
            listParents.peek().closeItem(output, listParents.size)
          }
        } catch (e: EmptyStackException) {
          Log.d("CustomTagHandler", "No Such Element Found" + listParents.isEmpty())
        }
      }
      else -> {
        Log.d("CustomTagHandler", "Found an unsupported tag $tag")
      }
    }
  }

  /** Abstract super class for [UnorderedListTag] and [OrderedListTag]. */
  private abstract class ListItemTag {
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
    /** Note: This knows that the last returned object from getSpans() will be the most recently added. */
    private fun getLast(text: Spanned): ListItemTag? {
      val listTags = text.getSpans(0, text.length, ListItemTag::class.java)
      return if (listTags.size == 0) {
        null
      } else listTags[listTags.size - 1]
    }
  }

  /** Class representing the unordered list (`<ul>`) HTML tag. */
  private inner class UnorderedListTag : ListItemTag() {
    override fun getReplaces(
      text: Editable?,
      indentation: Int
    ): Array<Any> {
      var bulletMargin: Int = indent
      if (indentation > 1) {
        bulletMargin = indent - bulletSpan.getLeadingMargin(true)
        if (indentation > 2) {
          bulletMargin -= (indentation - 2) * listItemIndent
        }
      }
      return arrayOf(
        LeadingMarginSpan.Standard(listItemIndent * (indentation - 1)),
        BulletSpanWithRadius(context, bulletMargin)
      )
    }
  }

  /** Class representing the ordered list (`<ol>`) HTML tag. */
  private inner class OrderedListTag
  /** Creates a new `<ol>` with start index of 1. */
  @JvmOverloads constructor(private var nextIdx: Int = 1) : ListItemTag() {
    override fun openItem(text: Editable) {
      super.openItem(text)
      text.append(Integer.toString(nextIdx++)).append(". ")
    }

    override fun getReplaces(
      text: Editable?,
      indentation: Int
    ): Array<Any> {
      var numberMargin: Int = listItemIndent * (indentation - 1)
      if (indentation > 2) {
        numberMargin -= (indentation - 2) * listItemIndent
      }
      return arrayOf(LeadingMarginSpan.Standard(numberMargin))
    }
  }
}
