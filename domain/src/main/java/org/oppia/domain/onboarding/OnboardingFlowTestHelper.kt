package org.oppia.domain.onboarding

import org.oppia.app.model.OnboardingFlow
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Helper class to clear onboarding flow cache. */
@Singleton
class OnboardingFlowTestHelper @Inject constructor(
  onboardingFlowCache: OnboardingFlowCache,
  private val logger: Logger
) {
  private var onboardingFlowStore: PersistentCacheStore<OnboardingFlow> = onboardingFlowCache.getOnboardingFlowCache()

  /** Clears any indication that the user has previously completed onboarding the application. */
  fun clearOnboardingFlow() {
    onboardingFlowStore.clearCacheAsync().invokeOnCompletion { it ->
      it?.let {
        logger.e("OnboardingFlowTestHelper", "Failed to clear onboarding flow.", it)
      }
    }
  }
}
