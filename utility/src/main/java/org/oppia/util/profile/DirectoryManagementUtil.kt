package org.oppia.util.profile

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.ProfileId
import java.io.File

@Singleton
class DirectoryManagementUtil @Inject constructor(

){
  fun getOrCreateDir(profileId: ProfileId): File {
    return File("temp")
  }

  fun deleteDir(profileId: ProfileId) {

  }
}
