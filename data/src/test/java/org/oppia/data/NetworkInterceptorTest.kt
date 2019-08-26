package org.oppia.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkSettings

private const val FAKE_RESPONSE_WITHOUT_XSSI_PREFIX: String = "{\"is_valid_response\": true}"
private const val FAKE_RESPONSE_WITH_XSSI_PREFIX: String =
  NetworkSettings.XSSI_PREFIX + "\n" + "{\"is_valid_response\": true}"

/** Tests for [NetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
class NetworkInterceptorTest {

  @Test
  fun testNetworkInterceptor_removeXSSIPrefixFromResponse_withXSSIPREFIX() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String = networkInterceptor.removeXSSIPrefixFromResponse(FAKE_RESPONSE_WITH_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FAKE_RESPONSE_WITHOUT_XSSI_PREFIX)
  }

  @Test
  fun testNetworkInterceptor_removeXSSIPrefixFromResponse_withoutXSSIPREFIX() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String = networkInterceptor.removeXSSIPrefixFromResponse(FAKE_RESPONSE_WITHOUT_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FAKE_RESPONSE_WITHOUT_XSSI_PREFIX)
  }
}
