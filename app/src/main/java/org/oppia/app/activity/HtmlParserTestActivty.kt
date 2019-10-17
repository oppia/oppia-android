package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import org.oppia.app.R
import org.oppia.util.parser.HtmlParser

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivty : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_test_html_parser)

    var test_html_content_text_view = findViewById(R.id.test_html_content_text_view)
    val rawDummyString: String = "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
    val htmlResult: Spannable = HtmlParser(applicationContext, /* entityType= */"", /* entityId= */"").parseHtml(rawDummyString, test_html_content_text_view)
    test_html_content_text_view.text = htmlResult

  }
}
