package org.oppia.android.util.platformparameter

import org.oppia.android.app.model.PlatformParameter

/**
 * Generic interface that is used to provide platform parameter values corresponding to the
 * [PlatformParameter] proto values. Objects that implement this interface will override the [value]
 * property to store the actual platform parameter value.
 * */
interface PlatformParameterValue<T> {
  val value: T
}
