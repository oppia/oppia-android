package org.oppia.util.profile

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to manage creation and deletion of directories. */
@Singleton
class DirectoryManagementUtil @Inject constructor(private val context: Context) {

  /**
   * Gets or creates a directory associated with the given profileId.
   *
   * @param profileId name of the directory to be returned.
   * @return the directory with the name specified by profileId.
   */
  fun getOrCreateDir(profileId: String): File {
    return context.getDir(profileId, Context.MODE_PRIVATE)
  }

  /**
   * Deletes a directory with the name specified by profileId.
   *
   * @param profileId name of directory to be deleted.
   * @return whether directory was successfully deleted.
   */
  fun deleteDir(profileId: String): Boolean {
    return getOrCreateDir(profileId).deleteRecursively()
  }
}
