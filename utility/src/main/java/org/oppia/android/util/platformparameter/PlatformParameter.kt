package org.oppia.android.util.platformparameter

/** Generic interface whose implementations will be provided as Platform Parameter Values. */
interface PlatformParameter<T> {
  val value: T
}
