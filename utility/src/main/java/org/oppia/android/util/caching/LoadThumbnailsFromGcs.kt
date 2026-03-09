package org.oppia.android.util.caching

import javax.inject.Qualifier

/**
 * Corresponds to an injectable boolean indicating whether lesson thumbnails should be loaded
 * from Google Cloud Storage (GCS). When false, local drawable resources are used instead.
 *
 * This is primarily used to support developer builds where GCS is inaccessible, ensuring that
 * thumbnails still render correctly using bundled drawable assets.
 */
@Qualifier
annotation class LoadThumbnailsFromGcs
