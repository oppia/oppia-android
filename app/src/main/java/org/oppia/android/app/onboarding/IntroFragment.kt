package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.IntroFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
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
    val args =
      checkNotNull(
        arguments?.getProto(
          INTRO_FRAGMENT_ARGUMENT_KEY,
          IntroFragmentArguments.getDefaultInstance()
        )
      ) {
        "Expected IntroFragment to have arguments."
      }

    val profileNickname = args.profileNickname

    val parentScreen = args.parentScreen

    val profileId =
      checkNotNull(arguments?.extractCurrentUserProfileId()) {
        "Expected profileId to be included in the arguments for IntroFragment."
      }

    return introFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileNickname,
      profileId,
      parentScreen
    )
  }

  companion object {
    /** Argument key for bundling arguments into [IntroFragment] . */
    const val INTRO_FRAGMENT_ARGUMENT_KEY = "IntroFragment.Arguments"

    /**
     * Creates a new instance of an [IntroFragment].
     *
     * @param profileNickname the nickname associated with this learner profile
     * @param parentScreen the parent screen opening this [IntroFragment] instance
     * @return a new instance of [IntroFragment]
     */
    fun newInstance(
      profileNickname: String,
      profileId: ProfileId,
      parentScreen: IntroActivityParams.ParentScreen
    ): IntroFragment {
      val argumentsProto =
        IntroFragmentArguments.newBuilder()
          .setProfileNickname(profileNickname)
          .setParentScreen(parentScreen)
          .build()
      return IntroFragment().apply {
        arguments = Bundle().apply {
          putProto(INTRO_FRAGMENT_ARGUMENT_KEY, argumentsProto)
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }
}
