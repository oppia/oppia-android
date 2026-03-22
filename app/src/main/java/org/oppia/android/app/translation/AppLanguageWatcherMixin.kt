package org.oppia.android.app.translation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.ForcedActivityLanguageMode
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * Activity mixin for automatically monitoring & recreating the activity whenever the current app
 * language changes (such as if it's set to system language & the system language changes).
 *
 * This is an activity-level class & must be initialized with a call to [initialize].
 */
class AppLanguageWatcherMixin @Inject constructor(
  private val activity: AppCompatActivity,
  private val translationController: TranslationController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler,
  private val localeController: LocaleController,
  private val oppiaLogger: OppiaLogger,
  private val activityRecreator: ActivityRecreator,
  private val profileManagementController: ProfileManagementController,
  private val activityLanguageLocaleHandler: ActivityLanguageLocaleHandler
) {

  /**
   * Initializes this mixin by starting language monitoring. This method should only ever be called
   * once for the lifetime of the current activity.
   *
   * Note that this method will synchronously ensure that [AppLanguageLocaleHandler] is properly
   * initialized if previous bootstrapping was lost (e.g. due to process death), so it must be
   * called before interacting with the locale handler to avoid inadvertent crashes in such
   * situations.
   *
   * @param languageMode the [ForcedActivityLanguageMode] to use for this activity
   */
  fun initialize(languageMode: ForcedActivityLanguageMode) {
    if (!appLanguageLocaleHandler.isInitialized()) {
      /* The handler might have been de-initialized since bootstrapping. This can generally happen
       * in two cases:
       * 1. Upon crash (later versions of Android will reopen the previous activity rather than
       * starting from the launcher activity if the crash occurred with the app in the foreground)
       * 2. Upon low-memory process death (the system will restore from a saved instance Bundle of
       * the application's activity stack)
       *
       * In both cases, the locale will be lost & can't be determined until the controller provides
       * the state. Since initialization happens during activity initialization, there's no way to
       * pass data from a previous instance of the application. Thus, the application can either
       * block the main thread on waiting for the data provider result (a strict mode violation that
       * could theoretically cause an ANR) or default the locale and, in the event the default is
       * wrong, restart the activity after the correct locale is retrieved. For the sake of avoiding
       * potential ANRs (even at the potential of perceived jank due to activity recreations), the
       * latter option is used here.
       */
      oppiaLogger.e(
        "AppLanguageWatcherMixin", "Restoring the display locale from de-initialization."
      )
      val defaultDisplayLocale = localeController.reconstituteDisplayLocale(
        localeController.getLikelyDefaultAppStringLocaleContext()
      )
      appLanguageLocaleHandler.initializeLocale(defaultDisplayLocale)
    }

    val currentUserProfileId = profileManagementController.getCurrentProfileId()

    val activityLanguageLocaleDataProvider = when (languageMode) {
      ForcedActivityLanguageMode.USE_SYSTEM_LANGUAGE ->
        translationController.getSystemLanguageLocale()
      ForcedActivityLanguageMode.USE_ENGLISH ->
        translationController.getLocaleFor(OppiaLanguage.ENGLISH)
      else -> if (currentUserProfileId == null) {
        translationController.getSystemLanguageLocale()
      } else {
        translationController.getAppLanguageLocale(currentUserProfileId)
      }
    }

    val liveData = activityLanguageLocaleDataProvider.toLiveData()
    liveData.observe(
      activity,
      object : Observer<AsyncResult<OppiaLocale.DisplayLocale>> {
        override fun onChanged(localeResult: AsyncResult<OppiaLocale.DisplayLocale>) {
          when (localeResult) {
            is AsyncResult.Success -> {
              // Only recreate the activity if the locale actually changed (to avoid an infinite
              // recreation loop).
              if (activityLanguageLocaleHandler.updateLocale(localeResult.value)) {
                // Recreate the activity to apply the latest locale state.
                liveData.removeObserver(this)
                activityRecreator.recreate(activity)
              }
            }
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "AppLanguageWatcherMixin",
                "Failed to retrieve app string locale for activity: $activity"
              )
            }
            is AsyncResult.Pending -> {} // Wait for an actual result.
          }
        }
      }
    )
  }
}