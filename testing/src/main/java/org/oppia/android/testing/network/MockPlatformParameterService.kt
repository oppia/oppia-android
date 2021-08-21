package org.oppia.android.testing.network

import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock [PlatformParameterService] to check that the service is properly requested. */
class MockPlatformParameterService(
  private val delegate: BehaviorDelegate<PlatformParameterService>
) : PlatformParameterService {

  private val TEST_UNSUPPORTED_OBJECT_AS_PARAM_VALUE = listOf<String>()

  companion object {
    /** Mock app version which is used to get right response from [MockPlatformParameterService] */
    const val appVersionForCorrectResponse = "1.0"
    /** Mock app version which is used to get wrong response from [MockPlatformParameterService] */
    const val appVersionForWrongResponse = "2.0"
    /** Mock app version which is used to get empty response from [MockPlatformParameterService] */
    const val appVersionForEmptyResponse = "3.0"
  }

  override fun getPlatformParametersByVersion(
    version: String,
    plaformType: String
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
        TEST_BOOLEAN_PARAM_NAME to TEST_BOOLEAN_PARAM_SERVER_VALUE,
        SPLASH_SCREEN_WELCOME_MSG to SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE
      )
      appVersionForWrongResponse -> mapOf(
        TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
        TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
        TEST_BOOLEAN_PARAM_NAME to TEST_UNSUPPORTED_OBJECT_AS_PARAM_VALUE,
        SPLASH_SCREEN_WELCOME_MSG to SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE
      )
      else -> mapOf()
    }
  }
}
