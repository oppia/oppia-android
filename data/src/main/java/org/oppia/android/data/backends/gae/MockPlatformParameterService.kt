package org.oppia.android.data.backends.gae

import org.oppia.android.data.backends.gae.api.PlatformParameterService
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

const val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"
const val TEST_BOOLEAN_PARAM_SERVER_VALUE = true
const val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"
const val TEST_INTEGER_PARAM_SERVER_VALUE = 1
const val TEST_STRING_PARAM_NAME = "test_string_param_name"
const val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"

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
