package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeExplorationContainer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/** Service that provides access to exploration endpoints. */
interface ExplorationService {

  @GET("explorehandler/init/{exploration_id}")
  fun getExplorationById(
    @Path("exploration_id") explorationId: String
  ): Call<GaeExplorationContainer>
}
