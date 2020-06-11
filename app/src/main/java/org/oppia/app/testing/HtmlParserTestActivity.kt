package org.oppia.app.testing

import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivity : InjectableAppCompatActivity() {
  @Inject lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_html_parser_activity)

    val testHtmlContentTextView: TextView = findViewById(R.id.test_html_content_text_view)
    val rawDummyString =
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
    val htmlResult: Spannable =
      htmlParserFactory.create(
        resourceBucketName, /* entityType= */ "exploration", /* entityId= */ "oppia", /* imageCenterAlign= */ false
      ).parseOppiaHtml(
        rawDummyString,
        testHtmlContentTextView
      )
    testHtmlContentTextView.text = htmlResult
    val testHtmlContentOrderedListTextView: TextView =
      findViewById(R.id.test_html_content_with_ordered_list_text_view)
    val rawDummyString2 ="""<ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3
                    <ol>
                        <li>Nested item 1</li>
                        <li>Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                        Nulla et tellus eu magna facilisis eleifend. Vestibulum faucibus pulvinar tincidunt. 
                        Nullam non mauris nisi.</li>
                    </ol>
                </li>
                <li>Item 4</li>
                <li>Item 5
                    <ol>
                        <li>Nested item 1</li>
                        <li>Nested item 2
                            <ol>
                                <li>Double nested item 1</li>
                                <li>Double nested item 2</li>
                            </ol>
                        </li>
                        <li>Nested item 3</li>
                    </ol>
                </li>
                <li>Item 6</li>
            </ul>
        """
    val htmlResult2: Spannable =
      htmlParserFactory.create(
        resourceBucketName, /* entityType= */ "exploration", /* entityId= */ "oppia", /* imageCenterAlign= */ false
      ).parseOppiaHtml(
        rawDummyString2,
        testHtmlContentOrderedListTextView
      )
    testHtmlContentOrderedListTextView.text = htmlResult2
    Log.d("HtmlTagHandler", "No Such Element Found" +htmlResult2)
  }
}
