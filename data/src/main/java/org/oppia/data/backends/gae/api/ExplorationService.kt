package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.ExplorationContainer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExplorationService {

  @GET("explorehandler/init/{exploration_id}")
  fun getExplorationById(@Path("exploration_id") explorationId: String): Call<ExplorationContainer>

}
