package org.oppia.util.caching

import javax.inject.Qualifier

/** Corresponds to an injectable list of topic IDs to cache if [CacheAssetsLocaly] is true. */
@Qualifier annotation class TopicListToCache
