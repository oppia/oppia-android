package org.oppia.android.util.caching

import javax.inject.Qualifier

/**
 * Corresponds to an injectable boolean indicating whether lesson assets should be cached locally.
 */
@Qualifier annotation class CacheAssetsLocally

// TODO: move these qualifiers to their own files.

/**
 * Corresponds to an injectable boolean indicating whether lessons are contained in the app's local
 * assets, and that these lessons are encoded using protos.
 */
@Qualifier annotation class LoadLessonProtosFromAssets
