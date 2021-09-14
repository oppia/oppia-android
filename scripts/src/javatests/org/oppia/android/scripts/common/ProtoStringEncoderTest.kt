package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.app.model.TestMessage
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.testing.assertThrows
import java.io.EOFException
import java.util.zip.ZipException

/** Tests for [ProtoStringEncoder]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ProtoStringEncoderTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Test
  fun testEncode_defaultProto_producesString() {
    val testMessage = TestMessage.getDefaultInstance()

    val base64 = testMessage.toCompressedBase64()

    // Verify that a valid base64 string is produced.
    assertThat(base64).isNotEmpty()
    assertThat(base64.length % 4).isEqualTo(0)
    assertThat(base64).matches("^([A-Z]|[a-z]|[0-9]|\\+|/)+=?=?$")
  }

  @Test
  fun testDecode_emptyString_throwsException() {
    assertThrows(EOFException::class) {
      TestMessage.getDefaultInstance().mergeFromCompressedBase64(base64 = "")
    }
  }

  @Test
  fun testDecode_badString_throwsException() {
    assertThrows(ZipException::class) {
      TestMessage.getDefaultInstance().mergeFromCompressedBase64(base64 = "asdf")
    }
  }

  @Test
  fun testDecode_encodedDefaultProto_mergedFromDefaultProto_producesDefaultProto() {
    val testMessage = TestMessage.getDefaultInstance()
    val encodedTestMessage = testMessage.toCompressedBase64()

    val decodedMessage =
      TestMessage.getDefaultInstance().mergeFromCompressedBase64(encodedTestMessage)

    assertThat(decodedMessage).isEqualToDefaultInstance()
  }

  @Test
  fun testDecode_encodedNonDefaultProto_mergedFromDefaultProto_producesValidProto() {
    val testMessage = TestMessage.newBuilder().apply {
      strValue = "test string"
    }.build()
    val encodedTestMessage = testMessage.toCompressedBase64()

    val decodedMessage =
      TestMessage.getDefaultInstance().mergeFromCompressedBase64(encodedTestMessage)

    assertThat(decodedMessage).isNotEqualToDefaultInstance()
    assertThat(decodedMessage.strValue).isEqualTo("test string")
  }

  @Test
  fun testDecode_encodedNonDefaultProto_mergedFromNonDefaultProto_producesProtoIgnoringBase() {
    val testMessage = TestMessage.newBuilder().apply {
      strValue = "test string"
    }.build()
    val encodedTestMessage = testMessage.toCompressedBase64()

    val decodedMessage =
      TestMessage.newBuilder().apply {
        intValue = 123
      }.build().mergeFromCompressedBase64(encodedTestMessage)

    // The intValue is not kept when reading the test message.
    assertThat(decodedMessage).isNotEqualToDefaultInstance()
    assertThat(decodedMessage.strValue).isEqualTo("test string")
    assertThat(decodedMessage.intValue).isEqualTo(0)
  }
}
