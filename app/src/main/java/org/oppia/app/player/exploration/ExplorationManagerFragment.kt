package org.oppia.app.player.exploration

import android.content.Context
import android.os.Bundle
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

class ExplorationManagerFragment : InjectableFragment() {
  @Inject lateinit var explorationManagerFragmentPresenter: ExplorationManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val internalProfileId =
      arguments!!.getInt(ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    explorationManagerFragmentPresenter.handleCreate(internalProfileId)
    setRetainInstance(true)
  }
}
