package org.oppia.android.domain.profile

import org.oppia.android.app.model.ProfileDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import javax.inject.Inject
import javax.inject.Singleton

/** Shares the profile database between profile management and event metadata collection. */
@Singleton
class ProfileCacheStore @Inject constructor(cacheStoreFactory: PersistentCacheStore.Factory) {
  /** The single store used to read and update the profile database. */
  val store = cacheStoreFactory.create("profile_database", ProfileDatabase.getDefaultInstance())
}
