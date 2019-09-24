package org.oppia.app.player.content

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.ContentInteractionFragmentBinding
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import javax.inject.Inject

/** Presenter for [ContentInteractionFragment]. */
class ContentInteractionFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment
) {

  private lateinit var binding: ContentInteractionFragmentBinding
  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentInteractionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)


    fetchDummyExplorations()

    return binding.root
  }

  private fun fetchDummyExplorations() {

   contentList.add(GaeSubtitledHtml("content","\u003cp\u003eThe number of pieces of cake I want.\u003c/p\u003e"+
      "\u003cp\u003eThe number of pieces the whole cake is cut into.\u003c/p\u003e"+
      "\u003cp\u003eI don't remember!\u003c/p\u003e"))

  }
}
