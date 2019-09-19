package org.oppia.app.player.content

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_card_items.view.*
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.ContentCardItemsBinding
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.util.data.URLImageParser
import javax.inject.Inject

/** Presenter for [ContentListFragment]. */
class ContentListFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment
) {

  private lateinit var binding: ContentListFragmentBinding
  var contentCardAdapter: ContentCardAdapter? = null
  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.recyclerView.apply {

//      binding.recyclerView.layoutManager = LinearLayoutManager(context)
//      contentCardAdapter = ContentCardAdapter(context, contentList);
//      binding.contentCardAdapter = ContentCardAdapter(context, contentList);
     layoutManager = LinearLayoutManager(context)
      adapter = createRecyclerViewAdapter();
    }
    fetchDummyExplorations()

    return binding.root
  }

  private fun fetchDummyExplorations() {

    contentList.add(GaeSubtitledHtml("content","\u003cp\u003eMatthew looked confused.\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\"Ah,\" said Mr. Baker. \"That just means how much of the whole thing do you want. Here, I\u2019ll show you.\u201d He picked up a napkin and a pencil. \u201cLook,\u201d he said, starting to draw.\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Sketch of circle divided into fifths.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;fifthsC_height_172_width_216.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\ufeff\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\u201cHere\u2019s a cake. Pretend it\u2019s divided into five equal parts, OK?\u201d Then he filled in two of the parts.\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Sketch of circle with 2/5 shaded.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;shaded_fifthsC_height_172_width_216.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\"You can call that \u003cem\u003etwo-fifths\u003c/em\u003e. And you write it like this:\u00a0\u003cstrong\u003e\u003coppia-noninteractive-math raw_latex-with-value=\"\u0026amp;quot;\\\\frac{2}{5}\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-math\u003e\u003c/strong\u003e.\u00a0It means: the whole cake is cut up into \u003cem\u003efive\u003c/em\u003e equal pieces (that\u2019s the number below the line) and you want \u003cem\u003etwo\u003c/em\u003e of those pieces (the number on top). We call the top number the\u00a0\u003cstrong\u003enumerator\u003c/strong\u003e, and the bottom number the\u00a0\u003cstrong\u003edenominator\u003c/strong\u003e.\"\u003cbr\u003e\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;numerator and denominator diagram\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;NDb_height_151_width_600.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\"Now let's see if you understand that!\" said Mr. Baker. \"Are you ready?\"\u003c/p\u003e"))
    contentList.add(GaeSubtitledHtml("content","\u003cp\u003e\"OK!\" said Mr. Baker. \"Here's a question for you.\"\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 1\u003c/strong\u003e: If we talk about wanting \u003coppia-noninteractive-math raw_latex-with-value=\"\u0026amp;quot;\\\\frac{4}{7}\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-math\u003e of a cake, what does the 7 represent?\u003c/p\u003e"))
    contentList.add(GaeSubtitledHtml("content","\u003cp\u003eThe number of pieces of cake I want.\u003c/p\u003e"+
      "\u003cp\u003eThe number of pieces the whole cake is cut into.\u003c/p\u003e"+
      "\u003cp\u003eI don't remember!\u003c/p\u003e"))
    contentList.add(GaeSubtitledHtml("Textinput","\u003cp\u003eThe number of pieces of cake I want.\u003c/p\u003e"))
    contentList.add(GaeSubtitledHtml("Feedback","\u003cp\u003eNot quite. Let's look at it again.\u003c/p\u003e"))

    contentList.add(GaeSubtitledHtml("Textinput","\u003cp\u003eI don't remember!\u003c/p\u003e"))
    contentList.add(GaeSubtitledHtml("Feedback","\u003cp\u003eThat's OK. Let's look at it again.\u003c/p\u003e"))
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<GaeSubtitledHtml> {
    return BindableAdapter.Builder
      .newBuilder<GaeSubtitledHtml>()
      .registerViewDataBinder(
        inflateDataBinding = ContentCardItemsBinding::inflate,
        setViewModel = ContentCardItemsBinding::setHtmlContent)
      .build()
  }

  fun convertHtmlToString(gaeSubtitledHtml: GaeSubtitledHtml): String{
    var htmlContent : String=""
    val imageGetter = URLImageParser(binding.root.tvContents, context)
    val html: Spannable
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }
    return html.toString()
  }
}
