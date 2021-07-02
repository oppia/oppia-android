package org.oppia.android.util.platformparameter

import org.oppia.android.app.model.PlatformParameter

/**
 * Generic interface that is used to provide Platform Parameter Values corresponding to the
 * [PlatformParameter] proto values. Objects that implement this interface will override the [value]
 * property to store the actual platform parameter value.
 */
interface PlatformParameterValue<T> {
  val value: T

  companion object {
    /**
     *  Creates a Platform Parameter Implementation containing the default value for a particular
     *  Platform Parameter
     */
    fun <T> createDefaultParameter(defaultValue: T): PlatformParameterValue<T> {
      return object : PlatformParameterValue<T> {
        override val value: T
          get() = defaultValue
      }
    }
  }
}
