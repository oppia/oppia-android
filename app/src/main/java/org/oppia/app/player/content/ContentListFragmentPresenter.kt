package org.oppia.app.player.content

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
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
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
      contentCardAdapter = ContentCardAdapter(context, contentList);
      binding.contentCardAdapter = ContentCardAdapter(context, contentList);
    }
    fetchDummyExplorations()

    return binding.root
  }

  //TODO (#121) :Replace this once interface for ExplorationDataController is available
  private fun fetchDummyExplorations() {
    contentList.add(
      GaeSubtitledHtml(
        "\u003cp\u003eMeet Matthew!\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;A boy with a red shirt and blue trousers.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;img_20180121_113315_pqwqhf863w_height_565_width_343.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003eMatthew is a young man who likes friends, sports, and eating cake! He also likes learning new things. We\u2019ll follow Matthew as he learns about fractions and how to use them.\u003cbr\u003e\u003c/p\u003e\u003cp\u003eYou should know the following before going on:\u003cbr\u003e\u003c/p\u003e\u003cul\u003e\u003cli\u003eThe counting numbers (1, 2, 3, 4, 5 \u2026.)\u003cbr\u003e\u003c/li\u003e\u003cli\u003eHow to tell whether one counting number is bigger or smaller than another\u003cbr\u003e\u003c/li\u003e\u003c/ul\u003e\u003cp\u003eYou should also get some paper and a pen or pencil to write with, and find a quiet place to work. Take your time, and go through the story at your own pace. Understanding is more important than speed!\u003cbr\u003e\u003c/p\u003e\u003cp\u003eOnce you\u2019re ready, click \u003cstrong\u003eContinue\u003c/strong\u003e to get started!\u003cbr\u003e\u003c/p\u003e",
        "content"
      )
    )
    contentList.add(
      GaeSubtitledHtml(
        "\u003cp\u003e\"OK!\" said Mr. Baker. \"Here's a question for you.\"\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 1\u003c/strong\u003e: If we talk about wanting \u003coppia-noninteractive-math raw_latex-with-value=\"\u0026amp;quot;\\\\frac{4}{7}\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-math\u003e of a cake, what does the 7 represent?\u003c/p\u003e",
        "content"
      )
    )
    contentList.add(
      GaeSubtitledHtml(
        "\u003cp\u003eThe number of pieces of cake I want.\u003c/p\u003e" +
            "\u003cp\u003eThe number of pieces the whole cake is cut into.\u003c/p\u003e" +
            "\u003cp\u003eI don't remember!\u003c/p\u003e",
        "content"
      )
    )
    contentList.add(GaeSubtitledHtml("\u003cp\u003eThe number of pieces of cake I want.\u003c/p\u003e", "Textinput"))
    contentList.add(GaeSubtitledHtml("\u003cp\u003eNot quite. Let's look at it again.\u003c/p\u003e", "Feedback"))
    contentList.add(GaeSubtitledHtml("\u003cp\u003eI don't remember!\u003c/p\u003e", "Textinput"))
    contentList.add(GaeSubtitledHtml("\u003cp\u003eThat's OK. Let's look at it again.\u003c/p\u003e", "Feedback"))
  }
}
