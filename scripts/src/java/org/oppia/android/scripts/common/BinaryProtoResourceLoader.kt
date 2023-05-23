package org.oppia.android.scripts.common

import com.google.protobuf.Message

/**
 * Provides support for loading binary proto buffer messages from a [Class]'s resources in a way
 * that's easier to test.
 */
interface BinaryProtoResourceLoader {
  /**
   * Loads a binary proto from [relativeResourceClass]'s adjacent class resources (e.g. using
   * [Class.getResource]).
   *
   * This method throws an exception if the provided resource isn't present.
   *
   * Note that it's recommended to use [Companion.loadProto] for a slightly easier API.
   *
   * @param relativeResourceClass the relative class which should have the expected resources
   * @param protoType the type of [Message] to load
   * @param resourcePath the relative path of the proto resource to load
   * @param baseMessage the base message from which to derive the new one (this can be anything, and
   *     it's generally recommended just to pass the proto's default instance to avoid needing to
   *     create any objects)
   * @return the loaded proto [Message] of type [T]
   */
  fun <T : Message> loadProto(
    relativeResourceClass: Class<*>,
    protoType: Class<T>,
    resourcePath: String,
    baseMessage: T
  ): T

  companion object {
    /**
     * Returns a parsed resource binary proto of type [T] for the provided [resourcePath] and
     * [relativeResourceClass] (using [baseMessage] to construct it). See
     * [BinaryProtoResourceLoader.loadProto] for more specific implementation details.
     */
    inline fun <reified T : Message> BinaryProtoResourceLoader.loadProto(
      relativeResourceClass: Class<*>,
      resourcePath: String,
      baseMessage: T
    ): T = loadProto(relativeResourceClass, T::class.java, resourcePath, baseMessage)
  }
}
