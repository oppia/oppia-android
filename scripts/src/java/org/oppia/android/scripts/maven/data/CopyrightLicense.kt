package org.oppia.android.scripts.maven.data

/** Data class that stores the details related to a license. */
data class CopyrightLicense(
  /** Name of the license. */
  val licenseName: String,

  /** Link of the license. */
  val licenseLink: String,

  /** License text corresponding to the license. */
  val licenseText: String
)
