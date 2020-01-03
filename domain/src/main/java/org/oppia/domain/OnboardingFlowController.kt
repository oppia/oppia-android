package org.oppia.domain

import androidx.lifecycle.LiveData
import org.oppia.app.model.OnboardingingFlow
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the user onBoarding information of the app. */
@Singleton
class OnboardingingFlowController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory, private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val onBoardingStore = cacheStoreFactory.create("onBoarding_flow", OnboardingingFlow.getDefaultInstance())

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to markOnboardingingFlowCompleted().
    onBoardingStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed to prime cache ahead of LiveData conversion for user on-boarding data.", it)
      }
    }
  }

  /**
   * Saves that the user has completed on-boarding the app. Note that this does not notify existing subscribers of the changed state,
   * nor can future subscribers observe this state until app restart.
   */
  fun markOnboardingingFlowCompleted() {
    onBoardingStore.storeDataAsync(updateInMemoryCache = false) {
      it.toBuilder().setAlreadyOnBoardedApp(true).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing that the user already onBoarded the app.", it)
      }
    }
  }

  /** Clears any indication that the user has previously completed on-boarding the application. */
  fun clearOnboardingingFlow() {
    onBoardingStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed to clear onBoarding flow.", it)
      }
    }
  }

  /**
   * Returns a [LiveData] result indicating whether the user has onBoarded the app. This is guaranteed to
   * provide the state of the store upon the creation of this controller even if [markOnboardingingFlowCompleted] has since been
   * called.
   */
  fun getOnboardingingFlow(): LiveData<AsyncResult<OnboardingingFlow>> {
    return dataProviders.convertToLiveData(onBoardingStore)
  }
}
