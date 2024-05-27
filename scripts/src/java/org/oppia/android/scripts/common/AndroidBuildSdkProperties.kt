package org.oppia.android.scripts.common

import java.io.InputStream
import java.util.Properties

/** Represents Android SDK properties available at build time. */
class AndroidBuildSdkProperties {
  private val androidSdkProperties by lazy {
    Properties().also { props ->
      AndroidBuildSdkProperties::class.java.loadResource(SDK_INFO_PROPERTIES_PATH).use(props::load)
    }
  }

  /** The version of the Android SDK used for building. */
  val buildSdkVersion: Int
    get() = androidSdkProperties.getExpectedProperty("build_sdk_version").toInt()

  /** The version of Android CLI tools used for building. */
  val buildToolsVersion: String
    get() = androidSdkProperties.getExpectedProperty("build_tools_version")

  private companion object {
    private const val SDK_INFO_PROPERTIES_PATH = "sdk_info.properties"

    private fun Class<*>.loadResource(name: String): InputStream =
      checkNotNull(getResourceAsStream(name)) { "Failed to find resource: $name." }

    private fun Properties.getExpectedProperty(key: String): String {
      return checkNotNull(getProperty(key)) { "Expected property to be present: $key" }
    }
  }
}
