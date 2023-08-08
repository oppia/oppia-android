package org.oppia.android.app.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.fragment.FragmentComponentBuilderInjector
import org.oppia.android.app.fragment.FragmentComponentFactory
import org.oppia.android.app.model.ScreenName.SPLASH_ACTIVITY
import org.oppia.android.app.notice.BetaNoticeClosedListener
import org.oppia.android.app.notice.DeprecationNoticeActionListener
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeClosedListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Enum class for the various deprecation notice actions available to the user. */
enum class DeprecationNoticeActionType {
  /** Action for when the user presses the 'Close' option on a deprecation dialog. */
  CLOSE,
  /** Action for when the user presses the 'Dismiss' option on a deprecation dialog. */
  DISMISS,
  /** Action for when the user presses the 'Update' option on a deprecation dialog. */
  UPDATE
}

/**
 * An activity that shows a temporary loading page until the app is fully loaded then navigates to
 * the profile selection screen.
 *
 * Note that this activity intentionally doesn't utilize the shared injectable activity base class
 * since it's used to bootstrap the app's locale context (which is passed along to activities
 * through their intents).
 */
class SplashActivity :
  AppCompatActivity(),
  FragmentComponentFactory,
  DeprecationNoticeActionListener,
  BetaNoticeClosedListener,
  GeneralAvailabilityUpgradeNoticeClosedListener {

  private lateinit var activityComponent: ActivityComponent

  @Inject
  lateinit var splashActivityPresenter: SplashActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val componentFactory = applicationContext as ActivityComponentFactory
    activityComponent = componentFactory.createActivityComponent(this)
    (activityComponent as ActivityComponentImpl).inject(this)
    splashActivityPresenter.handleOnCreate()
    intent.decorateWithScreenName(SPLASH_ACTIVITY)
  }

  override fun createFragmentComponent(fragment: Fragment): FragmentComponent {
    val builderInjector = activityComponent as FragmentComponentBuilderInjector
    return builderInjector.getFragmentComponentBuilderProvider().get().setFragment(fragment).build()
  }

  override fun onPositiveActionButtonClicked(noticeType: DeprecationNoticeActionType) {
    when (noticeType) {
      DeprecationNoticeActionType.CLOSE -> splashActivityPresenter.handleOnCloseAppButtonClicked()
      DeprecationNoticeActionType.DISMISS -> splashActivityPresenter.handleOnDismissButtonClicked()
      DeprecationNoticeActionType.UPDATE -> splashActivityPresenter.handleOnUpdateButtonClicked()
    }
  }

  override fun onNegativeActionButtonClicked(noticeType: DeprecationNoticeActionType) {
    when (noticeType) {
      DeprecationNoticeActionType.CLOSE -> splashActivityPresenter.handleOnCloseAppButtonClicked()
      DeprecationNoticeActionType.DISMISS -> splashActivityPresenter.handleOnDismissButtonClicked()
      DeprecationNoticeActionType.UPDATE -> splashActivityPresenter.handleOnUpdateButtonClicked()
    }
  }

  override fun onBetaNoticeOkayButtonClicked(permanentlyDismiss: Boolean) =
    splashActivityPresenter.handleOnBetaNoticeOkayButtonClicked(permanentlyDismiss)

  override fun onGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss: Boolean) =
    splashActivityPresenter.handleOnGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss)
}
