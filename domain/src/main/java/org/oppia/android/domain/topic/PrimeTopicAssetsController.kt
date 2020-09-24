package org.oppia.android.domain.topic

// TODO(#169): Remove this controller & download priming once downloads are properly supported.

/**
 * Controller for conditionally pre-priming assets to enable full download support. Whether
 * downloading is enabled is gated by the [org.oppia.android.util.caching.CacheAssetsLocally] annotation.
 */
interface PrimeTopicAssetsController {
  /**
   * Initiates asset downloading in the background. UI affordances will be shown before and after
   * priming, if it's enabled (otherwise nothing will show).
   *
   * @param dialogStyleResId the resource ID for the alert dialog style used for the dialog-based UI
   *     affordances
   */
  fun downloadAssets(dialogStyleResId: Int)
}
