package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.IntroFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains the introduction message for new learners. */
class IntroFragment : InjectableFragment() {
  @Inject
  lateinit var introFragmentPresenter: IntroFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val profileNickname =
      checkNotNull(
        arguments?.getProto(
          PROFILE_NICKNAME_ARGUMENT_KEY,
          IntroFragmentArguments.getDefaultInstance()
        )
      ) {
        "Expected profileNickname to be included in the arguments for IntroFragment."
      }.profileNickname

    val profileId =
      checkNotNull(arguments?.extractCurrentUserProfileId()) {
        "Expected profileId to be included in the arguments for IntroFragment."
      }

    return introFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileNickname,
      profileId
    )
  }
}
