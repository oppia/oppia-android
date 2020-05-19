package org.oppia.app.testing

import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_html_parser_activity)

    val testHtmlContentTextView: TextView = findViewById(R.id.test_html_content_text_view)
    val rawDummyString1 =
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
    val htmlResult1: Spannable =
      htmlParserFactory.create( /* entityType= */ "exploration", /* entityId= */
        "oppia", /* imageCenterAlign= */
        false
      )
        .parseOppiaHtml(
          rawDummyString1,
          testHtmlContentTextView
        )
    testHtmlContentTextView.text = htmlResult1

    val testHtmlContentOrderedListTextView: TextView =
      findViewById(R.id.test_html_content_with_ordered_list_text_view)
    val rawDummyString2 = """
            <h1>nested unordered lists</h1>

<ul>
	<li>first level<ul>
		<li>second level<ul>
			<li>third level<ul>
				<li>fourth level<ul>
					<li>fifth level</li>
				</ul></li>
				<li>fourth level</li>
			</ul></li>
			<li>third level</li>
			</ul></li>
			<li>second level</li>
			<li>second level: this should be a long enough text that will be wrapped into multiple lines</li>
		</ul></li>
	<li>first level</li>
</ul>

<h1>nested ordered lists</h1>

<ol>
	<li>first level<ol>
		<li>second level<ol>
			<li>third level<ol>
				<li>fourth level<ol>
					<li>fifth level</li>
				</ol></li>
				<li>fourth level</li>
			</ol></li>
			<li>third level</li>
			</ol></li>
			<li>second level</li>
			<li>second level: this should be a long enough text that will be wrapped into multiple lines</li>
		</ol></li>
	<li>first level</li>
</ol>

<h1>Mixed (ol and ul) nested lists:</h1>

<ul>
	<li>first unordered<ol>
		<li>first ordered</li>
		<li>second ordered<ul>
			<li>unordered in second ordered<ol>
				<li>ordered in "unordered in second ordered"</li>
				<li>another ordered in ""unordered in second ordered"</li>
			</ol></li>
		</ul></li>
		<li>third ordered with some other formatting: <b>bold</b> and <i>italics</i></li>
	</ol></li>
	<li>second unordered</li>
</ul>
"""
    val htmlResult2: Spannable =
      htmlParserFactory.create( /* entityType= */ "exploration", /* entityId= */
        "oppia", /* imageCenterAlign= */
        false
      )
        .parseOppiaHtml(
          rawDummyString2,
          testHtmlContentOrderedListTextView
        )
    testHtmlContentOrderedListTextView.text = htmlResult2
    Log.d("text", "" + htmlResult2)
  }
}
