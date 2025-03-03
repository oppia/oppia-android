package org.oppia.android.testing.network

import org.oppia.android.data.backends.gae.api.PlatformParameterService
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock [PlatformParameterService] to check that the service is properly requested. */
class MockPlatformParameterService(
  private val delegate: BehaviorDelegate<PlatformParameterService>
) : PlatformParameterService {

  private val TEST_UNSUPPORTED_OBJECT_AS_PARAM_VALUE = listOf<String>()

  override fun getPlatformParametersByVersion(
    version: String,
    platformType: String
  ): Call<Map<String, Any>> {
    val parameters = createMockPlatformParameterMap(version)
    return delegate.returningResponse(parameters).getPlatformParametersByVersion(version)
  }

  // Creates a Mock Response containing Map of PlatformParameters for testing
  private fun createMockPlatformParameterMap(appVersion: String): Map<String, Any> {
    return when (appVersion) {
      appVersionForCorrectResponse -> mapOf(
        TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
        TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
        TEST_BOOLEAN_PARAM_NAME to TEST_BOOLEAN_PARAM_SERVER_VALUE
      )
      appVersionForWrongResponse -> mapOf(
        TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
        TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
        TEST_BOOLEAN_PARAM_NAME to TEST_UNSUPPORTED_OBJECT_AS_PARAM_VALUE
      )
      else -> mapOf()
    }
  }

  companion object {
    /** Server name for the test string platform parameter. */
    const val TEST_STRING_PARAM_NAME = "test_string_param_name"

    /** Server value for the test string platform parameter. */
    const val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"

    /** Server name for the test boolean platform parameter. */
    const val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"

    /** Server value for the test boolean platform parameter. */
    const val TEST_BOOLEAN_PARAM_SERVER_VALUE = true

    /** Server name for the test integer platform parameter. */
    const val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"

    /** Server value for the test integer platform parameter. */
    const val TEST_INTEGER_PARAM_SERVER_VALUE = 1

    /** Mock app version which is used to get right response from [MockPlatformParameterService]. */
    const val appVersionForCorrectResponse = "1.0"

    /** Mock app version which is used to get wrong response from [MockPlatformParameterService]. */
    const val appVersionForWrongResponse = "2.0"

    /** Mock app version which is used to get empty response from [MockPlatformParameterService]. */
    const val appVersionForEmptyResponse = "3.0"
  }
}
