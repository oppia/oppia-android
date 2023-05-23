package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Message
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.scripts.common.BinaryProtoResourceLoader.Companion.loadProto
import org.oppia.android.scripts.testing.proto.TestMessage
import org.oppia.android.testing.mockito.capture

/**
 * Tests for logic in [BinaryProtoResourceLoader].
 *
 * Note that this test depends on its build environment to properly provide class-adjacent
 * resources.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BinaryProtoResourceLoaderTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockBinaryProtoResourceLoader: BinaryProtoResourceLoader
  @Captor lateinit var classCaptor1: ArgumentCaptor<Class<*>>
  @Captor lateinit var classCaptor2: ArgumentCaptor<Class<Message>>
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor lateinit var testMessageCaptor: ArgumentCaptor<TestMessage>

  @Test
  fun testLoadProto_testProto_callsLoadProtoWithCorrectClass() {
    mockBinaryProtoResourceLoader.loadProto(javaClass, "test.pb", TestMessage.getDefaultInstance())

    // The convenience function should just forward its call to the loader's loadProto method.
    verify(mockBinaryProtoResourceLoader)
      .loadProto(
        capture(classCaptor1),
        capture(classCaptor2),
        capture(stringCaptor),
        capture(testMessageCaptor)
      )
    assertThat(classCaptor1.value).isEqualTo(javaClass)
    assertThat(classCaptor2.value).isEqualTo(TestMessage::class.java)
    assertThat(stringCaptor.value).isEqualTo("test.pb")
    assertThat(testMessageCaptor.value).isEqualTo(TestMessage.getDefaultInstance())
  }
}
