package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.IntroActivityBinding
import javax.inject.Inject

private const val TAG_LEARNER_INTRO_FRAGMENT = "TAG_INTRO_FRAGMENT"

/** Argument key for bundling the profileId. */
const val PROFILE_NICKNAME_ARGUMENT_KEY = "profile_nickname"

/** The Presenter for [IntroActivity]. */
@ActivityScope
class IntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: IntroActivityBinding

  /** Handle creation and binding of the [IntroActivity] layout. */
  fun handleOnCreate(profileNickname: String) {
    binding = DataBindingUtil.setContentView(activity, R.layout.intro_activity)
    binding.lifecycleOwner = activity

    if (getIntroFragment() == null) {
      val introFragment = IntroFragment()

      val args = Bundle()
      args.putString(PROFILE_NICKNAME_ARGUMENT_KEY, profileNickname)
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
