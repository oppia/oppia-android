package org.oppia.android.app.onboarding

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.domain.onboarding.AppStartupStateController
import javax.inject.Inject
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController

/** [ViewModel] for final slide in onboarding flow. */
class OnboardingSlideFinalViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val appStartupStateController: AppStartupStateController,
  private val analyticsController: AnalyticsController,
  private val oppiaLogger: OppiaLogger
) : OnboardingViewPagerViewModel() {

  fun clickOnGetStarted() {
    appStartupStateController.markOnboardingFlowCompleted()
    logOnboardingCompleteEvent()
    (activity as RouteToProfileListListener).routeToProfileList()
  }

  private fun logOnboardingCompleteEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOnboardingContext(),
      null
    )
  }
}
