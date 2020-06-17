package org.oppia.domain.onboarding

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.LiveData
import org.oppia.app.model.AppStartupState
import org.oppia.app.model.AppStartupState.StartupMode
import org.oppia.app.model.OnboardingState
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val APP_STARTUP_STATE_DATA_PROVIDER_ID = "app_startup_state_data_provider_id"

/** Controller for persisting and retrieving the user's initial app state upon opening the app. */
@Singleton
class AppStartupStateController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val context: Context,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  private val onboardingFlowStore =
    cacheStoreFactory.create("on_boarding_flow", OnboardingState.getDefaultInstance())

  private val appStartupStateDataProvider by lazy {
    dataProviders.transform(APP_STARTUP_STATE_DATA_PROVIDER_ID, onboardingFlowStore) {
      AppStartupState.newBuilder().setStartupMode(computeAppStartupMode(it)).build()
    }
  }

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to
    // markOnboardingFlowCompleted().
    onboardingFlowStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache ahead of LiveData conversion for user onboarding data.",
          it)
      }
    }
  }

  /**
   * Saves that the user has completed the app onboarding flow. Note that this does not notify
   * existing subscribers of the changed state, nor can future subscribers observe this state until
   * the app restarts.
   */
  fun markOnboardingFlowCompleted() {
    onboardingFlowStore.storeDataAsync(updateInMemoryCache = false) {
      it.toBuilder().setAlreadyOnboardedApp(true).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing that the user already onboarded the app.", it)
      }
    }
  }

  /**
   * Returns a [LiveData] containing the user's startup state, which in turn affect what initial app
   * flow the user is directed to.
   */
  fun getAppStartupState(): LiveData<AsyncResult<AppStartupState>> {
    return dataProviders.convertToLiveData(appStartupStateDataProvider)
  }

  private fun computeAppStartupMode(onboardingState: OnboardingState): StartupMode {
    return when {
      hasAppExpired() -> StartupMode.APP_IS_DEPRECATED
      onboardingState.alreadyOnboardedApp -> StartupMode.USER_IS_ONBOARDED
      else -> StartupMode.USER_NOT_YET_ONBOARDED
    }
  }

  private fun hasAppExpired(): Boolean {
    val expirationDateString = getApplicationMetaData()?.getString("expiration_date")
    val expirationDate: Date? = parseDate(expirationDateString)
    // Assume the app is in an expired state if something fails when comparing the date.
    return expirationDate?.before(Date()) ?: true
  }

  private fun getApplicationMetaData(): Bundle? {
    return context.packageManager.getApplicationInfo(
      "org.oppia.app",
      PackageManager.GET_META_DATA
    ).metaData
  }

  private fun parseDate(dateString: String?): Date? {
    return try {
      expirationDateFormat.parse(dateString)
    } catch (e: ParseException) {
      logger.e("DOMAIN", "Failed to parse date string: $dateString", e)
      null
    }
  }
}
