package org.oppia.app.player.content_interaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.content_interaction.ContentInteractionFragmentPresenter
import javax.inject.Inject

/** Fragment displays single/multiplechoice input interaction. */
class ContentInteractionFragment : InjectableFragment() {

  @Inject
  lateinit var contentInteractionFragmentPresenter: ContentInteractionFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return contentInteractionFragmentPresenter.handleCreateView(inflater, container)
  }
}
