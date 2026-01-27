package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.clipboard.ClipboardController
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getLastUpdateTime
import org.oppia.android.util.extensions.getVersionName
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import javax.inject.Inject

private const val COPY_ICON_RESET_DELAY_MS = 2000L

/** [ViewModel] for [AppVersionFragment]. */
@FragmentScope
class AppVersionViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val clipboardController: ClipboardController,
  private val oppiaLogger: OppiaLogger,
  private val fragment: Fragment,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  context: Context
) : ObservableViewModel() {

  private val versionName: String = context.getVersionName()
  private val lastUpdateDateTime = context.getLastUpdateTime()

  /** Indicates whether the installation ID was recently copied. */
  val isInstallationIdCopied = ObservableField(false)

  /** Indicates whether the app version was recently copied. */
  val isAppVersionCopied = ObservableField(false)

  /** The app version name. */
  val appVersion: String
    get() = versionName

  /** The device installation ID. */
  val installationId: LiveData<String?> by lazy {
    Transformations.map(
      loggingIdentifierController.getInstallationId().toLiveData()
    ) { result ->
      (result as? AsyncResult.Success)?.value
    }
  }

  /** Copies the specified installation ID (if non-null) to the user's clipboard. */
  fun copyInstallationId(installationId: String?) {
    installationId?.let {
      val appName = resourceHandler.getStringInLocale(R.string.app_name)
      clipboardController.setCurrentClip(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.learner_analytics_device_id_clipboard_label_description, appName
        ),
        installationId
      ).toLiveData().observe(fragment) { result ->
        if (result is AsyncResult.Success) {
          isInstallationIdCopied.set(true)
          lifecycleSafeTimerFactory.createTimer(COPY_ICON_RESET_DELAY_MS).observe(fragment) {
            isInstallationIdCopied.set(false)
          }
        } else {
          oppiaLogger.w(
            "AppVersionViewModel",
            "Encountered unexpected non-successful result when copying to clipboard: $result"
          )
        }
      }
    }
  }

  /** Copies the app version to the user's clipboard. */
  fun copyAppVersion() {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    clipboardController.setCurrentClip(
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.learner_analytics_device_id_clipboard_label_description, appName
      ),
      versionName
    ).toLiveData().observe(fragment) { result ->
      if (result is AsyncResult.Success) {
        isAppVersionCopied.set(true)
        lifecycleSafeTimerFactory.createTimer(COPY_ICON_RESET_DELAY_MS).observe(fragment) {
          isAppVersionCopied.set(false)
        }
      } else {
        oppiaLogger.w(
          "AppVersionViewModel",
          "Encountered unexpected non-successful result when copying to clipboard: $result"
        )
      }
    }
  }

  /** Returns a localized, human-readable app version name. */
  fun computeVersionNameText(): String =
    resourceHandler.getStringInLocaleWithWrapping(R.string.app_version_name, versionName)

  /** Returns a localized, human-readable lastUpdateDateTime. */
  fun computeLastUpdatedDateText(): String =
    resourceHandler.getStringInLocaleWithWrapping(
      R.string.app_last_update_date, getDateTime(lastUpdateDateTime)
    )

  private fun getDateTime(lastUpdateTime: Long): String =
    resourceHandler.computeDateString(lastUpdateTime)
}
