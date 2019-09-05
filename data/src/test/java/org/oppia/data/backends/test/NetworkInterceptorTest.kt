package org.oppia.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.FakeJsonResponse
import org.oppia.data.backends.gae.NetworkInterceptor

/** Tests for [NetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
class NetworkInterceptorTest {
  @Test
  fun testNetworkInterceptor_withXssiPrefix_removesXssiPrefix() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(FakeJsonResponse.DUMMY_RESPONSE_WITH_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)
  }

  @Test
  fun testNetworkInterceptor_withoutXssiPrefix_removesXssiPrefix() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)
  }
}
