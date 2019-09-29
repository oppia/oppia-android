package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** A fragment that contains stories*/
class StoryFragment : InjectableFragment() {
  @Inject lateinit var storyFragmentController: StoryFragmentController

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return storyFragmentController.handleCreateView(inflater, container)
  }
}
