package org.oppia.domain.onboarding

import androidx.lifecycle.LiveData
import org.oppia.app.model.OnboardingFlow
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the user onboarding information of the app. */
@Singleton
class OnboardingFlowController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val onboardingFlowStore = cacheStoreFactory.create("on_boarding_flow", OnboardingFlow.getDefaultInstance())

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to markOnboardingFlowCompleted().
    onboardingFlowStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed to prime cache ahead of LiveData conversion for user onboarding data.", it)
      }
    }
  }

  /**
   * Saves that the user has completed onboarding the app. Note that this does not notify existing subscribers of the changed state,
   * nor can future subscribers observe this state until app restart.
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
   * Returns a [LiveData] result indicating whether the user has onboarded the app. This is guaranteed to
   * provide the state of the store upon the creation of this controller even if [markOnboardingFlowCompleted] has since been
   * called.
   */
  fun getOnboardingFlow(): LiveData<AsyncResult<OnboardingFlow>> {
    return dataProviders.convertToLiveData(onboardingFlowStore)
  }
}
