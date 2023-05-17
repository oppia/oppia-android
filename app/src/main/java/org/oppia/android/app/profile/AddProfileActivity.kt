package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ScreenName.ADD_PROFILE_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

// TODO(#4986): Remove the constants corresponding to bundles.
private const val ADD_PROFILE_COLOR_RGB_EXTRA_KEY = "AddProfileActivity.add_profile_color_rgb"

/** Activity that allows users to create new profiles. */
class AddProfileActivity : InjectableAppCompatActivity() {
  @Inject lateinit var addProfileFragmentPresenter: AddProfileActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  companion object {
    /**
     * Returns an [Intent] for opening new instances of [AddProfileActivity] with the specified
     * avatar background [colorRgb].
     */
    fun createIntent(context: Context, colorRgb: Int): Intent {
      return Intent(context, AddProfileActivity::class.java).apply {
        putExtra(ADD_PROFILE_COLOR_RGB_EXTRA_KEY, colorRgb)
        decorateWithScreenName(ADD_PROFILE_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    addProfileFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    // TODO(#3641): Investigate on using finish instead of intent.
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profileChooserActivityParams = ProfileChooserActivityParams.getDefaultInstance()
      }.build()
    )
    return false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    addProfileFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data)
  }

  override fun onDestroy() {
    super.onDestroy()
    addProfileFragmentPresenter.dismissAlertDialog()
  }

  /** Dagger injector for [AddProfileActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: AddProfileActivity)
  }
}
