package org.oppia.android.app.player.exploration

import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/**
 * ManagerFragment of [ExplorationFragment] that observes data provider that retrieve default story
 * text size.
 */
class ExplorationManagerFragment : InjectableFragment() {
  @Inject
  lateinit var explorationManagerFragmentPresenter: ExplorationManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val profileId = checkNotNull(arguments) {
      "Expected arguments to be provided for fragment."
    }.getProto(PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance())
    explorationManagerFragmentPresenter.handleCreate(profileId)
  }

  companion object {
    private const val PROFILE_ID_ARGUMENT_KEY = "ExplorationManagerFragment.profile_id"

    /**
     * Returns a new instance of [ExplorationManagerFragment] corresponding to the specified
     * [profileId].
     */
    fun createNewInstance(profileId: ProfileId): ExplorationManagerFragment {
      return ExplorationManagerFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_ID_ARGUMENT_KEY, profileId)
        }
      }
    }
  }
}
