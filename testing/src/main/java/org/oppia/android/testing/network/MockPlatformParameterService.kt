package org.oppia.android.testing.network

import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock [PlatformParameterService] to check that the service is properly requested. */
class MockPlatformParameterService(
  private val delegate: BehaviorDelegate<PlatformParameterService>
) : PlatformParameterService {
  override fun getPlatformParametersByVersion(
    version: String,
    plaformType: String
  ): Call<Map<String, Any>> {
    val parameters = createMockPlatformParameterMap()
    return delegate.returningResponse(parameters).getPlatformParametersByVersion(version)
  }

  // Creates a Mock Response containing Map of PlatformParameters for testing
  private fun createMockPlatformParameterMap(): Map<String, Any> {
    return mapOf(
      TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
      TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
      TEST_BOOLEAN_PARAM_NAME to TEST_BOOLEAN_PARAM_SERVER_VALUE
    )
  }
}
