package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.IntroFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.IntroActivityBinding
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val TAG_LEARNER_INTRO_FRAGMENT = "TAG_INTRO_FRAGMENT"

/** Argument key for bundling the profile nickname. */
const val PROFILE_NICKNAME_ARGUMENT_KEY = "IntroFragment.Arguments"

/** The Presenter for [IntroActivity]. */
@ActivityScope
class IntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: IntroActivityBinding

  /** Handle creation and binding of the [IntroActivity] layout. */
  fun handleOnCreate(profileNickname: String, profileId: ProfileId) {
    binding = DataBindingUtil.setContentView(activity, R.layout.intro_activity)
    binding.lifecycleOwner = activity

    if (getIntroFragment() == null) {
      val introFragment = IntroFragment()

      val argumentsProto =
        IntroFragmentArguments.newBuilder().setProfileNickname(profileNickname).build()

      val args = Bundle().apply {
        decorateWithUserProfileId(profileId)
        putProto(PROFILE_NICKNAME_ARGUMENT_KEY, argumentsProto)
      }

      introFragment.arguments = args

      activity.supportFragmentManager.beginTransaction().add(
        R.id.learner_intro_fragment_placeholder,
        introFragment,
        TAG_LEARNER_INTRO_FRAGMENT
      )
        .commitNow()
    }
  }

  private fun getIntroFragment(): IntroFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_LEARNER_INTRO_FRAGMENT
    ) as? IntroFragment
  }
}
