package org.oppia.app.player.content

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.data.backends.gae.model.GaeSubtitledHtml

import java.util.ArrayList

/**
 */
class ContentListFragment : Fragment() {

  // TODO: Rename and change types of parameters
  private var mParam1: String? = null
  private var mParam2: String? = null

  var mLayoutManager: LinearLayoutManager? = null

  var rvContentCard: RecyclerView? = null

  var btnBack: Button? = null
  var btnContinue: Button? = null

  var adapter: ContentCardAdapter? = null

  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()
  var dummyList: MutableList<GaeSubtitledHtml> = ArrayList()

  var btnCount = 0;
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.content_list_fragment, container, false)

    rvContentCard = view.findViewById(R.id.rvContentCard)
    btnContinue = view.findViewById(R.id.btnContinue)
    btnBack = view.findViewById(R.id.btnBack)

    initviews()

    return view
  }

  private fun initviews() {

    mLayoutManager = LinearLayoutManager(activity)
    rvContentCard!!.layoutManager = mLayoutManager

    dummyList.add(createDummyContentData())
    dummyList.add(createDummyProblem())
    dummyList.add(createDummyQ1onNandD())

    contentList.add(createDummyContentData())


    adapter = ContentCardAdapter(requireContext(), contentList)
    rvContentCard!!.adapter = adapter

    btnContinue!!.setOnClickListener {
      btnCount++;
      btnBack!!.visibility = View.VISIBLE;
      contentList.clear()
     contentList.add(dummyList.get(btnCount))
      adapter!!.notifyDataSetChanged();
    }
    btnBack!!.setOnClickListener {
      btnCount--;
      if(btnCount==0)
      btnBack!!.visibility = View.GONE;
      contentList.clear()
      contentList.add(dummyList.get(btnCount))
      adapter!!.notifyDataSetChanged();
    }
  }

  private fun createDummyContentData(): GaeSubtitledHtml {

    return GaeSubtitledHtml(
      "content",
      "\u003cp\u003eMeet Matthew!\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Matthew.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\\\u0026amp;quot;Hi!\\\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;hello_height_253_width_108.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003eMatthew is a typical young man: he likes friends, sports, and eating cake! He also likes learning new things. We\u2019ll follow Matthew as he learns about fractions and how to use them.\u003c/p\u003e\u003cp\u003eYou should know the following before continuing:\u003c/p\u003e\u003cul\u003e\u003cli\u003eThe counting numbers (1, 2, 3, 4, 5, ...)" + "\u003c/li\u003e\u003cli\u003eHow to add, subtract, multiply, and divide these numbers\u003c/li\u003e\u003cli\u003eHow to tell whether a number is bigger than another.\u003c/li\u003e\u003c/ul\u003e\u003cp\u003eYou should also get some\u00a0paper and a pen or pencil\u00a0to write with, and find a quiet place to work. Be sure to take your time and go through the story at your own pace. Understanding is more important than speed!\u003cbr\u003e\u003c/p\u003e\u003cp\u003eReady? Click\u00a0\u003cstrong\u003eContinue\u003c/strong\u003e\u00a0to get started!\u003c/p\u003e"
    )
  }

  private fun createDummyProblem(): GaeSubtitledHtml {

    return GaeSubtitledHtml(
      "problem",
      "\u003cp\u003eMatthew had a problem. His Aunt Tina was visiting that afternoon, and he had nothing for her to eat!\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Matthew is feeling Sad.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;Sad_small_height_142_width_61.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u201cI could buy some cake!\u201d Matthew thought. He had passed a bakery earlier that day which had a very yummy-looking chocolate cake in the window. That would be perfect!\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;A bakery shop\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;img_20170804_023625_karrj6fyko_height_350_width_250.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e"
    );

  }

  private fun createDummyQ1onNandD(): GaeSubtitledHtml {

    return GaeSubtitledHtml(
      "Q1 on N and D",
      "\u003cp\u003eMatthew looked confused.\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\"Ah,\" said Mr. Baker. \"That just means how much of the whole thing do you want. Here, I\u2019ll show you.\u201d He picked up a napkin and a pencil. \u201cLook,\u201d he said, starting to draw.\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Sketch of circle divided into fifths.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;fifthsC_height_172_width_216.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\ufeff\u003cbr\u003e\u003c/p\u003e\u003cp\u003e\u201cHere\u2019s a cake. Pretend it\u2019s divided into five equal parts, OK?\u201d Then he filled in two of the parts.\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;Sketch of circle with 2/5 shaded.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;shaded_fifthsC_height_172_width_216.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\"You can call that \u003cem\u003etwo-fifths\u003c/em\u003e. And you write it like this:\u00a0\u003cstrong\u003e\u003coppia-noninteractive-math raw_latex-with-value=\"\u0026amp;quot;\\\\frac{2}{5}\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-math\u003e\u003c/strong\u003e.\u00a0It means: the whole cake is cut up into \u003cem\u003efive\u003c/em\u003e equal pieces (that\u2019s the number below the line) and you want \u003cem\u003etwo\u003c/em\u003e of those pieces (the number on top). We call the top number the\u00a0\u003cstrong\u003enumerator\u003c/strong\u003e, and the bottom number the\u00a0\u003cstrong\u003edenominator\u003c/strong\u003e.\"\u003cbr\u003e\u003c/p\u003e\u003coppia-noninteractive-image alt-with-value=\"\u0026amp;quot;numerator and denominator diagram\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;NDb_height_151_width_600.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\"Now let's see if you understand that!\" said Mr. Baker. \"Are you ready?\"\u003c/p\u003e");

  }
}
