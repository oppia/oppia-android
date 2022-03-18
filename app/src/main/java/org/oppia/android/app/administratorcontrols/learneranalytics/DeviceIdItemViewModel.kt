package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.clipboard.ClipboardController
import org.oppia.android.domain.clipboard.ClipboardController.CurrentClip
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * shows, and allows the user to copy, the device installation ID associated with analytics.
 */
class DeviceIdItemViewModel private constructor(
  private val clipboardController: ClipboardController,
  private val resourceHandler: AppLanguageResourceHandler,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val oppiaLogger: OppiaLogger,
  private val fragment: Fragment
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.DEVICE_ID) {
  /** The device installation ID associated with analytics. */
  val deviceId: LiveData<String?> by lazy {
    Transformations.map(
      loggingIdentifierController.getDeviceId().toLiveData(), this::processDeviceId
    )
  }

  /** The current ID copied to the user's clipboard, or ``null`` if there isn't one. */
  val currentCopiedId: LiveData<String?> by lazy {
    Transformations.map(clipboardController.getCurrentClip().toLiveData(), this::processCurrentClip)
  }

  /** Returns a human-readable label to represent the specified [deviceId]. */
  fun computeDeviceIdLabel(deviceId: String?): String {
    return if (deviceId != null) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.learner_analytics_device_id_label, deviceId
      )
    } else {
      resourceHandler.getStringInLocale(R.string.learner_analytics_error_retrieving_device_id_error)
    }
  }

  /** Copies the specified device ID (if non-null) to the user's clipboard. */
  fun copyDeviceId(deviceId: String?) {
    // Only copy the device ID if it's available.
    deviceId?.let {
      val appName = resourceHandler.getStringInLocale(R.string.app_name)
      clipboardController.setCurrentClip(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.learner_analytics_device_id_clipboard_label_description, appName
        ),
        deviceId
      ).toLiveData().observe(fragment) {
        if (it !is AsyncResult.Success) {
          oppiaLogger.w(
            "ProfileLearnerIdItemViewModel",
            "Encountered unexpected non-successful result when copying to clipboard: $it"
          )
        }
      }
    }
  }

  private fun processDeviceId(result: AsyncResult<String>) = (result as? AsyncResult.Success)?.value

  private fun processCurrentClip(result: AsyncResult<CurrentClip>): String? {
    return if (result is AsyncResult.Success) {
      when (val clip = result.value) {
        is CurrentClip.SetWithAppText -> clip.text
        CurrentClip.SetWithOtherContent, CurrentClip.Unknown -> null
      }
    } else null
  }

  /** Factory for creating new [DeviceIdItemViewModel]s. */
  class Factory @Inject constructor(
    private val clipboardController: ClipboardController,
    private val resourceHandler: AppLanguageResourceHandler,
    private val loggingIdentifierController: LoggingIdentifierController,
    private val oppiaLogger: OppiaLogger,
    private val fragment: Fragment
  ) {
    /** Returns a new [DeviceIdItemViewModel]. */
    fun create(): DeviceIdItemViewModel {
      return DeviceIdItemViewModel(
        clipboardController, resourceHandler, loggingIdentifierController, oppiaLogger, fragment
      )
    }
  }
}
