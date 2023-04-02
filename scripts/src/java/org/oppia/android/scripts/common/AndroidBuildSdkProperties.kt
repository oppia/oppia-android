package org.oppia.android.scripts.common

import java.util.Properties

/** Represents Android SDK properties available at build time. */
class AndroidBuildSdkProperties {
  private val androidSdkProperties by lazy {
    Properties().also { props ->
      AndroidBuildSdkProperties::class.java.getResourceAsStream(SDK_INFO_PROPERTIES_PATH).use {
        props.load(it)
      }
    }
  }

  /** The version of the Android SDK used for building. */
  val buildSdkVersion: Int
    get() = androidSdkProperties.getExpectedProperty("build_sdk_version").toInt()

  /** The version of Android built tools used for building. */
  val buildToolsVersion: String
    get() = androidSdkProperties.getExpectedProperty("build_tools_version")

  private companion object {
    // Note that this path must start with '/' since Bazel places the properties file not properly
    // relative to this class (since it's nested under a separate src directory; this may be fixable
    // in the future if script sources were ever moved to a top-level src directory).
    private const val SDK_INFO_PROPERTIES_PATH =
      "/scripts/src/java/org/oppia/android/scripts/common/sdk_info.properties"

    private fun Properties.getExpectedProperty(key: String): String {
      return checkNotNull(getProperty(key)) { "Expected property to be present: $key" }
    }
  }
}
