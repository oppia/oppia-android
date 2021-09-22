package org.oppia.android.app.translation

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

class AppLanguageWatcherMixin @Inject constructor(
  private val activity: AppCompatActivity,
  private val translationController: TranslationController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler,
  private val oppiaLogger: OppiaLogger,
  private val activityRecreator: ActivityRecreator
) {
  fun initialize() {
    // TODO(#52): Hook this up properly to profiles, and handle the non-profile activity cases.
    val profileId = ProfileId.getDefaultInstance()
    val appLanguageLocaleDataProvider = translationController.getAppLanguageLocale(profileId)
    appLanguageLocaleDataProvider.toLiveData().observe(activity, { localeResult ->
      if (localeResult.isSuccess()) {
        // Only recreate the activity if the locale actually changed (to avoid an infinite
        // recreation loop).
        if (appLanguageLocaleHandler.updateLocale(localeResult.getOrThrow())) {
          // Recreate the activity to apply the latest locale state. Note that in some cases this
          // may result in 2 recreations for the user: one to notify that there's a new system
          // locale, and a second to actually apply that locale. This is due to a limitation in the
          // infrastructure where the app can't know which system locale it can use without a
          // LiveData trigger (this class). While this isn't an ideal user experience, the
          // expectation is that the recreation should happen fairly quickly. If, in practice,
          // that's not the case, the team will need to look into ways of synchronizing the UI-kept
          // locale faster (maybe by short-circuiting some of the system locale selection code since
          // the underlying I/O state is technically fixed and doesn't need a DataProvider past the
          // splash screen).
          activityRecreator.recreate(activity)
        }
      } else if (localeResult.isFailure()) {
        oppiaLogger.e(
          "AppLanguageWatcherMixin", "Failed to retrieve app string locale for activity: $activity"
        )
      }
    })
  }
}
