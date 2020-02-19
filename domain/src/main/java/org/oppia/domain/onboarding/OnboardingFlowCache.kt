package org.oppia.domain.onboarding

import org.oppia.app.model.OnboardingFlow
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** This class creates a PersistentCacheStore for onboarding flow. */
@Singleton
class OnboardingFlowCache @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val logger: Logger
) {
  private val onboardingFlowStore = cacheStoreFactory.create("on_boarding_flow", OnboardingFlow.getDefaultInstance())

  init {
    onboardingFlowStore.primeCacheAsync().invokeOnCompletion { it ->
      it?.let {
        logger.e(
          "OnboardingFlowCache",
          "Failed to prime cache ahead of LiveData conversion for user onboarding data.",
          it
        )
      }
    }
  }

  /** Returns the PersistentCacheStore for onboarding flow. */
  fun getOnboardingFlowCache(): PersistentCacheStore<OnboardingFlow> {
    return onboardingFlowStore
  }
}
