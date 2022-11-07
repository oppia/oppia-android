package org.oppia.android.app.spotlight

import android.util.Log
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotlightTargetStore @Inject constructor() {

  private val spotlightTargetList = ArrayList<SpotlightTarget>()

  fun addSpotlightTarget(spotlightTarget: SpotlightTarget) {
    spotlightTargetList.add(spotlightTarget)
    Log.d("overlay", "current target list: $spotlightTargetList")
  }

  fun getSpotlightTargetList() = spotlightTargetList

}