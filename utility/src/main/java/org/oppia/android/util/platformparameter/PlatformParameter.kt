package org.oppia.android.util.platformparameter

/**
 * Generic interface that is used to provide Platform Parameter Values corresponding to the
 * [PlatformParameter] proto values. Objects that implement this interface will override the [value]
 * property to store the actual platform parameter value.
 * */
interface PlatformParameter<T> {
  val value: T
}
