package org.oppia.android.util.properties

/**
 * Interface for application-available retrieving custom properties (e.g. system properties)
 * configured for the app.
 *
 * This retriever is primarily meant to be used as a way to customize app behaviors through
 * easy-to-configure system properties (particularly for rooted devices like emulators) to configure
 * app behavior without a rebuild or platform parameter. It's expected that these properties are
 * only available in developer builds of the app.
 *
 * All properties used through this must be prefixed with "org.oppia.android". A property can be set
 * as so:
 *
 * ```sh
 * adb shell settings put global org.oppia.android.property_name value
 * ```
 *
 * This can then be fetched with methods like [getPropertyString] using the key "property_name".
 *
 * Properties can be reset as so:
 *
 * ```sh
 * adb shell settings delete global org.oppia.android.property_name
 * ```
 */
interface CustomPropertyRetriever {
  /**
   * Retrieves a custom property as a string.
   *
   * @param name the un-qualified name of the property to retrieve
   * @return the string value of the property, or null if it cannot be found or is not defined
   */
  fun getPropertyString(name: String): String?

  /**
   * Retrieves a custom property as an integer.
   *
   * @param name the un-qualified name of the property to retrieve
   * @return the integer value of the property, or null if it cannot be found, is not defined, or
   *     cannot be parsed as an integer
   */
  fun getPropertyInt(name: String): Int?

  /**
   * Retrieves a custom property as a boolean.
   *
   * @param name the un-qualified name of the property to retrieve
   * @return the boolean value of the property, or null if it cannot be found, is not defined, or
   *     cannot be parsed as a boolean
   */
  fun getPropertyBoolean(name: String): Boolean?
}
