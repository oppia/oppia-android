package org.oppia.util.profile

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.ProfileId
import java.io.File

/** Utility to manage creation and deletion of directories */
@Singleton
class DirectoryManagementUtil @Inject constructor(){

  /** Get or creates a directory with the name specified by profileId */
  fun getOrCreateDir(profileId: ProfileId): File {
    return File("temp")
  }

  /** Deletes a directory with the name specified by profileId */
  fun deleteDir(profileId: ProfileId) {

  }
}
