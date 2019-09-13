package org.oppia.app.player.state.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that controls audio for a state and content.*/
class AudioFragment : InjectableFragment() {
  @Inject
  lateinit var audioFragmentController: AudioFragmentController

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return audioFragmentController.handleCreateView(inflater, container)
  }
}
