package org.oppia.android.util.platformparameter

/**
 * Generic interface whose implementations are used in providing Platform Parameter Values. It is
 * done by creating an object which will implement this interface, then this object overrides the
 * [value] property to store the actual platform parameter value.
 * */
interface PlatformParameter<T> {
  val value: T
}
