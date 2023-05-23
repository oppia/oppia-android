package org.oppia.android.scripts.common.testing

import com.google.protobuf.Message
import org.oppia.android.scripts.common.BinaryProtoResourceLoader
import org.oppia.android.scripts.common.BinaryProtoResourceLoaderImpl
import java.io.File

/**
 * An intercepting implementation of [BinaryProtoResourceLoader] for use in tests.
 *
 * Use [interceptResource] to set up a resource interception. See that method's documentation for
 * more details on how interception works.
 *
 * @property baseResourceLoader the base [BinaryProtoResourceLoader] to fallback to
 */
class InterceptingBinaryProtoResourceLoader(
  private val baseResourceLoader: BinaryProtoResourceLoader = BinaryProtoResourceLoaderImpl()
) : BinaryProtoResourceLoader {
  private val resourceTable = mutableMapOf<String, File>()

  override fun <T : Message> loadProto(
    relativeResourceClass: Class<*>,
    protoType: Class<T>,
    resourcePath: String,
    baseMessage: T
  ): T {
    val parsedProto = resourceTable[resourcePath]?.inputStream()?.use { inputStream ->
      baseMessage.newBuilderForType().mergeFrom(inputStream)
    }?.build() ?: return baseResourceLoader.loadProto(
      relativeResourceClass, protoType, resourcePath, baseMessage
    )
    return checkNotNull(protoType.cast(parsedProto)) { "An internal failure occurred." }
  }

  /**
   * Arranges a resource to be loaded from a specified file rather than from [Class] resources.
   *
   * The provided [resourcePath] should match the ``resourcePath`` passed to [loadProto] in order
   * for the resource to be correctly intercepted. This must be arranged before [loadProto] is
   * called, and the interception stays for the lifetime of this loader.
   *
   * If a resource is not bound for interception, then the loading of the resource will fall back to
   * the provided [baseResourceLoader] used at the time of this loader's construction.
   *
   * @param resourcePath the path of the resource that's normally passed to [loadProto] to intercept
   * @param replacementFile the file to load instead of the resource for [resourcePath]
   */
  fun interceptResource(resourcePath: String, replacementFile: File) {
    resourceTable[resourcePath] = replacementFile
  }
}
