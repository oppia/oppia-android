package org.oppia.android.scripts.common.testing

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Message
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.scripts.common.BinaryProtoResourceLoader
import org.oppia.android.scripts.common.BinaryProtoResourceLoader.Companion.loadProto
import org.oppia.android.scripts.testing.proto.TestMessage
import org.oppia.android.scripts.testing.proto.TestMessage2
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.mockito.capture
import java.io.File

/**
 * Tests for [InterceptingBinaryProtoResourceLoader].
 *
 * Note that this test depends on its build environment to properly provide class-adjacent
 * resources.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class InterceptingBinaryProtoResourceLoaderTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  @Mock lateinit var mockBinaryProtoResourceLoader: BinaryProtoResourceLoader
  @Captor lateinit var classCaptor1: ArgumentCaptor<Class<*>>
  @Captor lateinit var classCaptor2: ArgumentCaptor<Class<Message>>
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor lateinit var testMessageCaptor: ArgumentCaptor<TestMessage>

  @Before
  fun setUp() {
    `when`(
      mockBinaryProtoResourceLoader.loadProto(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    ).thenReturn(TestMessage.getDefaultInstance())
  }

  @Test
  fun testLoadProto_noInterceptedResources_callsThroughToBaseLoader() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)

    loader.loadProto(javaClass, "unbound.pb", TestMessage.getDefaultInstance())

    // Since the loaded proto isn't bound the call should be forwarded to the base loader.
    verify(mockBinaryProtoResourceLoader)
      .loadProto(
        capture(classCaptor1),
        capture(classCaptor2),
        capture(stringCaptor),
        capture(testMessageCaptor)
      )
    assertThat(classCaptor1.value).isEqualTo(javaClass)
    assertThat(classCaptor2.value).isEqualTo(TestMessage::class.java)
    assertThat(stringCaptor.value).isEqualTo("unbound.pb")
    assertThat(testMessageCaptor.value).isEqualTo(TestMessage.getDefaultInstance())
  }

  @Test
  fun testLoadProto_nonInterceptedResource_callsThroughToBaseLoader() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    loader.loadProto(javaClass, "unbound.pb", TestMessage.getDefaultInstance())

    // Since the loaded proto isn't bound the call should be forwarded to the base loader.
    verify(mockBinaryProtoResourceLoader)
      .loadProto(
        capture(classCaptor1),
        capture(classCaptor2),
        capture(stringCaptor),
        capture(testMessageCaptor)
      )
    assertThat(classCaptor1.value).isEqualTo(javaClass)
    assertThat(classCaptor2.value).isEqualTo(TestMessage::class.java)
    assertThat(stringCaptor.value).isEqualTo("unbound.pb")
    assertThat(testMessageCaptor.value).isEqualTo(TestMessage.getDefaultInstance())
  }

  @Test
  fun testLoadProto_interceptedResource_doesNotCallThroughToBaseLoader() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    loader.loadProto(javaClass, "bound1.pb", TestMessage.getDefaultInstance())

    verifyNoMoreInteractions(mockBinaryProtoResourceLoader)
  }

  @Test
  fun testLoadProto_interceptedResource_correctClass_defaultBaseMessage_returnsConfiguredProto() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    val loadedProto = loader.loadProto(javaClass, "bound1.pb", TestMessage.getDefaultInstance())

    assertThat(loadedProto).isEqualTo(TEST_MESSAGE_1)
  }

  @Test
  fun testLoadProto_interceptedResource_correctClass_nonDefaultBase_returnsConfiguredProto() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    val loadedProto = loader.loadProto(javaClass, "bound1.pb", TEST_MESSAGE_2)

    // A non-default base message shouldn't change the output.
    assertThat(loadedProto).isEqualTo(TEST_MESSAGE_1)
  }

  @Test
  fun testLoadProto_interceptedResource_incorrectResourceClass_returnsConfiguredProto() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    val loadedProto =
      loader.loadProto(Test::class.java, "bound1.pb", TestMessage.getDefaultInstance())

    // An invalid resource class shouldn't change the result since it's ignored by the interceptor.
    assertThat(loadedProto).isEqualTo(TEST_MESSAGE_1)
  }

  @Test
  fun testLoadProto_differentInterceptedResource_returnsDifferentProto() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))
    loader.interceptResource("bound2.pb", createProtoFile("bound2.pb", TEST_MESSAGE_2))

    val loadedProto = loader.loadProto(javaClass, "bound2.pb", TestMessage.getDefaultInstance())

    // Multiple, different resources can be bound.
    assertThat(loadedProto).isEqualTo(TEST_MESSAGE_2)
  }

  @Test
  fun testLoadProto_reinterceptedResource_returnsDifferentProto() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))
    loader.interceptResource("bound1.pb", createProtoFile("bound1_again.pb", TEST_MESSAGE_2))

    val loadedProto = loader.loadProto(javaClass, "bound1.pb", TestMessage.getDefaultInstance())

    // A resource can be rebound.
    assertThat(loadedProto).isEqualTo(TEST_MESSAGE_2)
  }

  @Test
  fun testLoadProto_interceptedResource_wrongProtoClass_returnsDifferentProtoInstance() {
    val loader = InterceptingBinaryProtoResourceLoader(mockBinaryProtoResourceLoader)
    loader.interceptResource("bound1.pb", createProtoFile("bound1.pb", TEST_MESSAGE_1))

    val loadedProto = loader.loadProto(javaClass, "bound1.pb", TestMessage2.getDefaultInstance())

    // Proto doesn't perform runtime type checking, so it will attempt to load using the wrong proto
    // structure (and will not throw an exception unless the two protos are binary incompatible).
    assertThat(loadedProto).isNotInstanceOf(TestMessage::class.java)
  }

  private fun createProtoFile(name: String, message: Message): File =
    tempFolder.newFile(name).also { it.outputStream().use(message::writeTo) }

  private companion object {
    private val TEST_MESSAGE_1 = TestMessage.newBuilder().apply {
      strValue = "test message string 1"
      intValue = 10
    }.build()

    private val TEST_MESSAGE_2 = TestMessage.newBuilder().apply {
      strValue = "test message string 2"
      intValue = 20
      addStrValues("list str value")
    }.build()
  }
}
