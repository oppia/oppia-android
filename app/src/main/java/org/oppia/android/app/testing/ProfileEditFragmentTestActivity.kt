package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.testing.activity.TestActivity
import javax.inject.Inject

/** Test Activity for testing [ProfileEditFragment] and its view models. */
class ProfileEditFragmentTestActivity : TestActivity() {
  @Inject
  lateinit var profileEditFragmentTestActivityPresenter: ProfileEditFragmentTestActivityPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileEditFragmentTestActivityPresenter.handleOnCreate()
  }

  /** Dagger injector for [ProfileEditFragmentTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ProfileEditFragmentTestActivity)
  }

  companion object {
    // TODO(#4986): Remove the constants corresponding to bundles.
    private const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY =
      "ProfileEditActivity.profile_edit_profile_id"

    /** Returns an [Intent] for opening [ProfileEditFragmentTestActivity]. */
    fun createIntent(context: Context, profileId: Int): Intent {
      val intent = Intent(context, ProfileEditFragmentTestActivity::class.java)
      intent.putExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
      return intent
    }
  }
}
