package org.oppia.android.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.IntroActivityBinding
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.ui.R
import javax.inject.Inject

private const val TAG_LEARNER_INTRO_FRAGMENT = "TAG_INTRO_FRAGMENT"

/** The Presenter for [IntroActivity]. */
@ActivityScope
class IntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: IntroActivityBinding

  /** Handle creation and binding of the [IntroActivity] layout. */
  fun handleOnCreate(
    profileNickname: String,
    profileId: ProfileId,
    parentScreen: IntroActivityParams.ParentScreen
  ) {
    binding = DataBindingUtil.setContentView(activity, R.layout.intro_activity)
    binding.lifecycleOwner = activity

    if (getIntroFragment() == null) {
      val introFragment = IntroFragment.newInstance(profileNickname, profileId, parentScreen)

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
