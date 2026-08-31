package org.oppia.android.scripts.release

/**
 * The set of supported Oppia Android app flavors for release operations.
 *
 * Each flavor maps to a distinct Play Console track and GCS bucket. The [id] matches the
 * flavor string embedded in signed AAB filenames (e.g. `oppia-android-0.18-rc01-alpha-<hash>.aab`)
 * and in changelog filenames (e.g. `config/changelogs/0.18_alpha.md`).
 */
enum class AppFlavor(val id: String) {
  /** The alpha (internal testing) flavor, deployed to the alpha Play Console track. */
  ALPHA("alpha"),

  /** The beta (external testing) flavor, deployed to the beta Play Console track. */
  BETA("beta"),

  /** The general-availability flavor, deployed to the production Play Console track. */
  GA("ga");

  companion object {
    /**
     * Returns the [AppFlavor] whose [id] matches [flavorId], or `null` if no match is found.
     *
     * @param flavorId the flavor string from an AAB filename or changelog filename
     */
    fun fromId(flavorId: String): AppFlavor? = values().find { it.id == flavorId }
  }
}
