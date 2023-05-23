package org.oppia.android.scripts.common

import com.google.protobuf.Message
import java.io.InputStream

/** Standard resource-based implementation of [BinaryProtoResourceLoader]. */
class BinaryProtoResourceLoaderImpl : BinaryProtoResourceLoader {
  override fun <T : Message> loadProto(
    relativeResourceClass: Class<*>,
    protoType: Class<T>,
    resourcePath: String,
    baseMessage: T
  ): T {
    val parsedProto = relativeResourceClass.loadResource(resourcePath).use { inputStream ->
      baseMessage.newBuilderForType().mergeFrom(inputStream)
    }.build()
    return checkNotNull(protoType.cast(parsedProto)) { "An internal failure occurred." }
  }

  private companion object {
    private fun Class<*>.loadResource(name: String): InputStream =
      checkNotNull(getResourceAsStream(name)) { "Failed to find resource: $name." }
  }
}
