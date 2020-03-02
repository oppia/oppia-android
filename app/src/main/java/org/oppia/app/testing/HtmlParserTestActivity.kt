package org.oppia.app.testing

import android.os.Bundle
import android.text.Spannable
import android.view.View
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

private const val CONCEPT_CARD_DIALOG_FRAGMENT_TAG = "CONCEPT_CARD_FRAGMENT"

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivity : InjectableAppCompatActivity(), HtmlParser.CustomOppiaTagActionListener {
  @Inject lateinit var htmlParserFactory: HtmlParser.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_html_parser_activity)

    val testHtmlContentTextView: TextView = findViewById(R.id.test_html_content_text_view)
    val rawDummyString =
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e<p>Take a look at the short <oppia-noninteractive-skillreview skill_id-with-value=\"UxTGIJqaHMLa\" text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview> to refresh your memory if you need to.</p>"

    val htmlParser = htmlParserFactory.create(/* entityType= */ "exploration",
      /* entityId= */"oppia",
      /* imageCenterAlign= */
      false,
      customOppiaTagActionListener = this
    )
    val htmlResult: Spannable =
      htmlParser.parseOppiaHtml(
        rawDummyString,
        testHtmlContentTextView,
        supportsLinks = true
      )
    testHtmlContentTextView.text = htmlResult
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.newInstance(skillId).showNow(this.supportFragmentManager, CONCEPT_CARD_DIALOG_FRAGMENT_TAG)
  }
}
