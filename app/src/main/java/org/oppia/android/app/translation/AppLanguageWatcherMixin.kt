package org.oppia.android.app.translation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.oppialogger.OppiaLogger
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
  private val activityRecreator: ActivityRecreator
) {
  /**
   * Initializes this mixin by starting language monitoring. This method should only ever be called
   * once for the lifetime of the current activity.
   *
   * Note that this method will synchronously ensure that [AppLanguageLocaleHandler] is properly
   * initialized if previous bootstrapping was lost (e.g. due to process death), so it must be
   * called before interacting with the locale handler to avoid inadvertent crashes in such
   * situations.
   */
  fun initialize() {
    if (!appLanguageLocaleHandler.isInitialized()) {
      /* The handler might have been de-initialized since bootstrapping. This can generally happen
       * in two cases:
       * 1. Upon crash (later versions of Android will reopen the previous activity rather than
       *   starting from the launcher activity if the crash occurred with the app in the foreground)
       * 2. Upon low-memory process death (the system will restore from a saved instance Bundle of
       *   the application's activity stack)
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

    // TODO(#52): Hook this up properly to profiles, and handle the non-profile activity cases.
    val profileId = ProfileId.getDefaultInstance()
    val appLanguageLocaleDataProvider = translationController.getAppLanguageLocale(profileId)
    val liveData = appLanguageLocaleDataProvider.toLiveData()
    liveData.observe(
      activity,
      object : Observer<AsyncResult<OppiaLocale.DisplayLocale>> {
        override fun onChanged(localeResult: AsyncResult<OppiaLocale.DisplayLocale>) {
          when (localeResult) {
            is AsyncResult.Success -> {
              // Only recreate the activity if the locale actually changed (to avoid an infinite
              // recreation loop).
              if (appLanguageLocaleHandler.updateLocale(localeResult.value)) {
                // Recreate the activity to apply the latest locale state. Note that in some cases
                // this may result in 2 recreations for the user: one to notify that there's a new
                // system locale, and a second to actually apply that locale. This is due to a
                // limitation in the infrastructure where the app can't know which system locale it
                // can use without a LiveData trigger (this class). While this isn't an ideal user
                // experience, the expectation is that the recreation should happen fairly quickly.
                // If, in practice, that's not the case, the team will need to look into ways of
                // synchronizing the UI-kept locale faster (maybe by short-circuiting some of the
                // system locale selection code since the underlying I/O state is technically fixed
                // and doesn't need a DataProvider past the splash screen). Finally, if the decision
                // is made to recreate the activity then ensure it can never happen again in this
                // activity by removing this observer.
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
