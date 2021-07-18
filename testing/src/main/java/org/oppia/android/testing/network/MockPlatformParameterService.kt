package org.oppia.android.testing.network

import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import org.oppia.android.data.backends.gae.model.GaePlatformParameters
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_VALUE
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock [PlatformParameterService] to check that the service is properly requested. */
class MockPlatformParameterService(
  private val delegate: BehaviorDelegate<PlatformParameterService>
) : PlatformParameterService {
  override fun getPlatformParametersByVersion(version: String): Call<GaePlatformParameters> {
    val parameters = createMockGaePlatformParameters()
    return delegate.returningResponse(parameters).getPlatformParametersByVersion(version)
  }

  // Creates a Mock Response containing GaePlatformParameter for testing
  private fun createMockGaePlatformParameters(): GaePlatformParameters {
    return GaePlatformParameters(
      arrayListOf(
        GaePlatformParameter(TEST_STRING_PARAM_NAME, TEST_STRING_PARAM_VALUE),
        GaePlatformParameter(TEST_INTEGER_PARAM_NAME, TEST_INTEGER_PARAM_VALUE),
        GaePlatformParameter(TEST_BOOLEAN_PARAM_NAME, TEST_BOOLEAN_PARAM_VALUE)
      )
    )
  }
}
