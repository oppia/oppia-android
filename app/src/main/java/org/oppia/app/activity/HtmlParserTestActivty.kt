package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.widget.TextView
import org.oppia.app.R
import org.oppia.util.parser.HtmlParser

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivty : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_test_html_parser)

    val testHtmlContentTextView = findViewById(R.id.test_html_content_text_view) as TextView
    val rawDummyString = "\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
    val htmlResult: Spannable = HtmlParser(applicationContext, /* entityType= */"", /* entityId= */"").parseOppiaHtml(
      rawDummyString,
      testHtmlContentTextView
    )
    testHtmlContentTextView.text = htmlResult
  }
}
