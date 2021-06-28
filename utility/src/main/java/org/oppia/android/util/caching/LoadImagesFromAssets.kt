package org.oppia.android.util.caching

import javax.inject.Qualifier

/**
 * Corresponds to an injectable boolean indicating whether images (including thumbnails) should be
 * retrieved from the app's local assets.
 */
@Qualifier annotation class LoadImagesFromAssets
