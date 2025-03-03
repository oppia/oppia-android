package org.oppia.android.domain.platformparameter.testing

import javax.inject.Inject
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.threading.TestCoroutineDispatchers

/**
 * A test-only initialization utility for ensuring platform parameters and feature flags correctly
 * load in tests.
 *
 * Note that any production code that relies on platform parameters or feature flags very likely
 * will require its tests to use this class for initialization.
 *
 * This class must be the *first* injection in a test suite in order to ensure that platform
 * parameters and feature flags are loaded correctly before any other injections happen (since those
 * injections may, in turn, require these parameters and flags to already be initialized in the
 * Dagger graph).
 *
 * [TestPlatformParameterConfigRetriever] should be used for overriding flags (which must be done
 * statically before this class is injected).
 */
class PlatformParameterTestInitializer @Inject constructor(
  testCoroutineDispatchers: TestCoroutineDispatchers,
  monitorFactory: DataProviderTestMonitor.Factory,
  platformParameterController: PlatformParameterController
) {
  init {
    // Wait for parameters to successfully load. Note that this is particularly ordered to avoid a
    // race condition on priming the underlying platform parameter database and trying to load
    // parameters too quickly (which can cause a redundant initialization of
    // PlatformParameterProcessState.
    val paramsProvider = platformParameterController.loadParameters()
    testCoroutineDispatchers.runCurrent()
    monitorFactory.createMonitor(paramsProvider).waitForNextSuccessResult()
  }
}
