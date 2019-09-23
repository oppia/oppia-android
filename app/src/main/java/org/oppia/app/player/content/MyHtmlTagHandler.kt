package org.oppia.app.player.content

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.StrikethroughSpan
import android.util.Log
import org.jsoup.Jsoup
import org.xml.sax.Attributes
import org.xml.sax.XMLReader

//class MyHtmlTagHandler : Html.TagHandler {

//  override fun handleTag(
//    opening: Boolean, tag: String, output: Editable,
//    xmlReader: XMLReader
//  ) {
//    HtmlParser.buildSpannedText(tag,
//      HtmlParser.TagHandler()
//      { b: Boolean, s: String, editable: Editable?, attributes: Attributes? ->
//        if (b && s.equals("oppia-noninteractive-image")) {
//          var value: String? = HtmlParser.getValue(attributes, "filepath-with-value");
//
//          Jsoup.parse(value).text();
//
//// Another possibility may be the static unescapeEntities method:
//          var strictMode: Boolean = true;
//          var unescapedString: String = org.jsoup.parser.Parser.unescapeEntities(value, strictMode);
//          Log.d("unescapedString", "*****" + unescapedString)
//          Log.d("value", "*****" + value)
//
//        }
//        false;
//
//      })
//  }
//}
//  override fun handleTag(
//    opening: Boolean, tag: String, output: Editable,
//    xmlReader: XMLReader
//  ) {
//    if (tag.equals("oppia-noninteractive-image", ignoreCase = true) || tag == "o") {
//      processStrike(opening, output)
//    }
//  }
//
//  private fun processStrike(opening: Boolean, output: Editable) {
//    val len = output.length
//    if (opening) {
//      output.setSpan(StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK)
//    } else {
//      val obj = getLast(output, StrikethroughSpan::class.java)
//      val where = output.getSpanStart(obj)
//
//      output.removeSpan(obj)
//
//
//      if (where != len) {
//        output.setSpan(StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//      }
//    }
//  }
//
//  private fun getLast(text: Editable, kind: Class<*>): Any? {
//    val objs = text.getSpans<Any>(0, text.length, kind)
//
//    if (objs.size == 0) {
//      return null
//    } else {
//      for (i in objs.size downTo 1) {
//        if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
//          return objs[i - 1]
//        }
//      }
//      return null
//    }
//  }

//}
