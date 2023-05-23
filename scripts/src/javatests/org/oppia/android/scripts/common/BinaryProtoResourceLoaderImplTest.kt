package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.scripts.testing.proto.TestMessage
import org.oppia.android.scripts.testing.proto.TestMessage2
import org.oppia.android.testing.assertThrows

/**
 * Tests for [BinaryProtoResourceLoaderImpl].
 *
 * Note that this test depends on its build environment to properly provide class-adjacent
 * resources.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BinaryProtoResourceLoaderImplTest {
  @Test
  fun testLoadProto_otherClassPackage_invalidResourceName_throwsException() {
    val loader = BinaryProtoResourceLoaderImpl()

    val exception = assertThrows(IllegalStateException::class) {
      loader.loadProto(
        Test::class.java, TestMessage::class.java, "invalid.pb", TestMessage.getDefaultInstance()
      )
    }

    assertThat(exception).hasMessageThat().contains("Failed to find resource: invalid.pb")
  }

  @Test
  fun testLoadProto_otherClassPackage_validResourceName_throwsException() {
    val loader = BinaryProtoResourceLoaderImpl()

    val exception = assertThrows(IllegalStateException::class) {
      loader.loadProto(
        Test::class.java,
        TestMessage::class.java,
        "test_proto1.pb",
        TestMessage.getDefaultInstance()
      )
    }

    assertThat(exception).hasMessageThat().contains("Failed to find resource: test_proto1.pb")
  }

  @Test
  fun testLoadProto_thisClass_invalidResourceName_throwsException() {
    val loader = BinaryProtoResourceLoaderImpl()

    val exception = assertThrows(IllegalStateException::class) {
      loader.loadProto(
        javaClass,
        TestMessage::class.java,
        "invalid.pb",
        TestMessage.getDefaultInstance()
      )
    }

    assertThat(exception).hasMessageThat().contains("Failed to find resource: invalid.pb")
  }

  @Test
  fun testLoadProto_thisClass_validResourceName_incorrectProtoClass_returnsDiffInstProto() {
    val loader = BinaryProtoResourceLoaderImpl()

    val proto =
      loader.loadProto(
        javaClass, TestMessage2::class.java, "test_proto1.pb", TestMessage2.getDefaultInstance()
      )

    // Proto doesn't perform type checking at runtime, so compatible protos will still try to load.
    assertThat(proto).isNotInstanceOf(TestMessage::class.java)
  }

  @Test
  fun testLoadProto_thisClass_validResourceName_correctProtoClass_defaultMessage_returnsProto() {
    val loader = BinaryProtoResourceLoaderImpl()

    val proto =
      loader.loadProto(
        javaClass, TestMessage::class.java, "test_proto1.pb", TestMessage.getDefaultInstance()
      )

    assertThat(proto.strValue).isEqualTo("test string 1")
    assertThat(proto.intValue).isEqualTo(1)
    assertThat(proto.strValuesList).isEmpty()
  }

  @Test
  fun testLoadProto_thisClass_validResourceName_correctProtoClass_nonDefaultMessage_returnsProto() {
    val loader = BinaryProtoResourceLoaderImpl()
    val baseMessage = TestMessage.newBuilder().apply {
      intValue = 3
      addStrValues("test value")
    }.build()

    val proto = loader.loadProto(javaClass, TestMessage::class.java, "test_proto1.pb", baseMessage)

    // The base message is just used to construct the proto--its values aren't used directly.
    assertThat(proto.strValue).isEqualTo("test string 1")
    assertThat(proto.intValue).isEqualTo(1)
    assertThat(proto.strValuesList).isEmpty()
  }

  @Test
  fun testLoadProto_thisClass_otherValidResourceName_returnsDifferentProto() {
    val loader = BinaryProtoResourceLoaderImpl()

    val proto =
      loader.loadProto(
        javaClass, TestMessage::class.java, "test_proto2.pb", TestMessage.getDefaultInstance()
      )

    assertThat(proto.strValue).isEqualTo("test string 2")
    assertThat(proto.intValue).isEqualTo(2)
    assertThat(proto.strValuesList).isEmpty()
  }
}
