package org.oppia.android.testing.platformparameter

import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import org.oppia.android.data.backends.gae.model.GaePlatformParameters
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPlatformParameterService @Inject constructor(
  @OppiaRetrofit private val retrofit: Retrofit
) : PlatformParameterService {

  override fun getPlatformParametersByVersion(version: String): Call<GaePlatformParameters> {
    val delegate = getMockRetrofit().create(PlatformParameterService::class.java)
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

  // Creates a MockRetrofit instance
  private fun getMockRetrofit(): MockRetrofit {
    val behavior = NetworkBehavior.create()
    behavior.setFailurePercent(0)

    return MockRetrofit.Builder(retrofit).apply {
      networkBehavior(behavior)
    }.build()
  }
}
