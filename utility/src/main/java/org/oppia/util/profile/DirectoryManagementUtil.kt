package org.oppia.util.profile

import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

/** Utility to manage creation and deletion of directories. */
@Singleton
class DirectoryManagementUtil @Inject constructor(){

  /**
   * Gets or creates a directory associated with the given profileId.
   *
   * @param profileId name of the directory to be returned.
   * @return the directory with the name specified by profileId.
   */
  fun getOrCreateDir(profileId: String): File {
    return File("temp")
  }

  /**
   * Deletes a directory with the name specified by profileId.
   *
   * @param profileId name of directory to be deleted.
   */
  fun deleteDir(profileId: String) {

  }
}
