package org.oppia.android.scripts.maven.data

/** Data class that stores the details related to a third-party dependency. */
data class Dependency(
  /** Name of the third-party dependency. */
  val name: String,

  /** Version of the third-party dependency. */
  val version: String,

  /** List of licenses corresponding to the third-party dependency. */
  val licenseList: List<CopyrightLicense>,
)
