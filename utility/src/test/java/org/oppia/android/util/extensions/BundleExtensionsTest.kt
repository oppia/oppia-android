package org.oppia.android.util.extensions

import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.IncompatibleTestMessage
import org.oppia.android.app.model.TestMessage
import org.oppia.android.app.model.TestMessage2
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import androidx.test.ext.truth.content.IntentSubject.assertThat

/** Tests for BundleExtensions. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class BundleExtensionsTest {
  private val TEST_STRING = "String value"

  private val TEST_MESSAGE_WITH_STR_AND_INT =
    TestMessage.newBuilder().setStrValue(TEST_STRING).setIntValue(1).build()

  private val TEST_MESSAGE_WITH_INT = TestMessage.newBuilder().setIntValue(1).build()

  @Test
  fun testGetProto_noProtoInBundle_returnsDefault() {
    val bundle = Bundle()

    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage.getDefaultInstance()
    )

    assertThat(testMessage).isEqualTo(testMessage)
  }

  @Test
  fun testPutProto_noProtoInBundle_addsDataToProto() {
    val bundle = Bundle()

    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)

    assertThat(bundle.keySet()).contains("test_proto_key")
  }

  @Test
  fun testGetProto_protoInBundle_sameType_returnsCorrectProto() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)

    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage.getDefaultInstance()
    )

    assertThat(testMessage).isEqualTo(TEST_MESSAGE_WITH_STR_AND_INT)
  }

  @Test
  fun testGetProto_protoInBundle_sameType_defaultHasData_returnsUnmergedProto() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_INT)

    val testMessage = bundle.getProto(
      "test_proto_key",
      defaultValue = TestMessage.newBuilder().setStrValue("Different string value").build()
    )

    // The string from the default value should not be incorporated since the default is only used
    // if the proto is not defined yet in the bundle.
    assertThat(testMessage).isEqualTo(TEST_MESSAGE_WITH_INT)
  }

  @Test
  fun testPutProto_protoAlreadyInBundle_overridesExistingProto() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_INT)

    // Override the proto in the bundle.
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)
    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage.getDefaultInstance()
    )

    // The most recent proto value should be retrieved.
    assertThat(testMessage).isEqualTo(TEST_MESSAGE_WITH_STR_AND_INT)
  }

  @Test
  fun testGetProto_protoInBundle_thenRemoved_returnsDefault() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_INT)

    // Remove the proto from the bundle.
    bundle.remove("test_proto_key")
    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage.getDefaultInstance()
    )

    // The default should be used since the proto was removed.
    assertThat(testMessage).isEqualTo(TestMessage.getDefaultInstance())
  }

  @Test
  fun testPutProto_multipleProtos_eachCanBeRetrieved() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key1", TEST_MESSAGE_WITH_INT)
    bundle.putProto("test_proto_key2", TEST_MESSAGE_WITH_STR_AND_INT)

    val testMessage1 = bundle.getProto(
      "test_proto_key1", defaultValue = TestMessage.getDefaultInstance()
    )
    val testMessage2 = bundle.getProto(
      "test_proto_key2", defaultValue = TestMessage.getDefaultInstance()
    )

    // Both protos should be retrievable.
    assertThat(testMessage1).isEqualTo(TEST_MESSAGE_WITH_INT)
    assertThat(testMessage2).isEqualTo(TEST_MESSAGE_WITH_STR_AND_INT)
  }

  @Test
  fun testGetProto_oldProtoInBundle_differentButCompatibleType_returnsInteroperableProto() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)

    // Retrieve a "newer" version of the proto (using a proto that's binary-compatible).
    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage2.getDefaultInstance()
    )

    // The string should be retrievable from the new proto, and the new field should be defaulted.
    // The int will be ignored since it was "removed" (but is reserved).
    assertThat(testMessage.strValue).isEqualTo(TEST_STRING)
    assertThat(testMessage.boolValue).isFalse()
  }

  @Test
  fun testGetProto_protoInBundle_incompatibleType_returnsDefaultValue() {
    val bundle = Bundle()
    bundle.putProto("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)

    // Try retrieving the wrong proto type.
    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = IncompatibleTestMessage.getDefaultInstance()
    )

    // Proto incompatibilities should lead to the default value being returned.
    assertThat(testMessage).isEqualTo(IncompatibleTestMessage.getDefaultInstance())
  }

  @Test
  fun testGetProto_keyUsed_notAProto_returnsDefaultInstance() {
    val bundle = Bundle()
    bundle.putString("test_proto_key", "not_a_proto")

    // Try retrieving a proto when a non-proto was saved.
    val testMessage = bundle.getProto(
      "test_proto_key", defaultValue = TestMessage.getDefaultInstance()
    )

    // Like other bundle retrieval functions, return the default if there's a type incompatibility.
    assertThat(testMessage).isEqualTo(TestMessage.getDefaultInstance())
  }

  @Test
  fun testPutProtoExtra_intentWithoutExtras_initializesExtrasAndAddsProto() {
    val intent = Intent()

    intent.putProtoExtra("test_proto_key", TEST_MESSAGE_WITH_INT)

    assertThat(intent).extras().hasSize(1)
    assertThat(intent).extras().containsKey("test_proto_key")
  }

  @Test
  fun testPutProtoExtra_intentWithExtras_addsProto() {
    val intent = Intent()
    intent.putExtra("other_extra", "with_value")

    intent.putProtoExtra("test_proto_key", TEST_MESSAGE_WITH_INT)

    assertThat(intent).extras().hasSize(2)
    assertThat(intent).extras().containsKey("test_proto_key")
  }

  @Test
  fun testPutProtoExtra_intentWithSameKey_overridesWithProto() {
    val intent = Intent()
    intent.putExtra("first_extra", "with_value")

    intent.putProtoExtra("first_extra", TEST_MESSAGE_WITH_INT)

    assertThat(intent).extras().hasSize(1)
    assertThat(intent).extras().containsKey("first_extra")
    assertThat(intent.getSerializableExtra("first_extra")).isNotInstanceOf(String::class.java)
  }

  @Test
  fun testGetProtoExtra_intentWithoutExtras_returnsDefaultValue() {
    val intent = Intent()

    val message = intent.getProtoExtra("test_proto_key", TestMessage.getDefaultInstance())

    assertThat(message).isEqualToDefaultInstance()
  }

  @Test
  fun testGetProtoExtra_intentWithoutExtras_nonDefaultValue_returnsProvidedDefaultValue() {
    val intent = Intent()
    val sampleDefault = TestMessage.newBuilder().apply { intValue = 12 }.build()

    val message = intent.getProtoExtra("test_proto_key", sampleDefault)

    assertThat(message).isEqualTo(sampleDefault)
  }

  @Test
  fun testGetProtoExtra_intentWithExtras_withoutKey_returnsDefaultValue() {
    val intent = Intent()
    intent.putExtra("first_extra", "with_value")

    val message = intent.getProtoExtra("test_proto_key", TestMessage.getDefaultInstance())

    assertThat(message).isEqualToDefaultInstance()
  }

  @Test
  fun testGetProtoExtra_intentWithExtras_withMismatchedValue_returnsDefaultValue() {
    val intent = Intent()
    intent.putExtra("first_extra", "with_value")

    val message = intent.getProtoExtra("first_extra", TestMessage.getDefaultInstance())

    assertThat(message).isEqualToDefaultInstance()
  }

  @Test
  fun testGetProtoExtra_intentWithExtras_withCorrectValue_returnsProto() {
    val intent = Intent()
    intent.putExtra("other_extra", "with_value")
    intent.putProtoExtra("test_proto_key", TEST_MESSAGE_WITH_STR_AND_INT)

    val message = intent.getProtoExtra("test_proto_key", TestMessage.getDefaultInstance())

    assertThat(message).isEqualTo(TEST_MESSAGE_WITH_STR_AND_INT)
  }
}
