package org.oppia.android.data.backends.gae.api

import org.oppia.android.app.model.ExplorationContainer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

/** Service that provides access to exploration endpoints. */
@Singleton
interface ExplorationService {

  @GET("explorehandler/init/{exploration_id}")
  fun getExplorationById(
    @Path("exploration_id") explorationId: String
  ): Call<ExplorationContainer>
}
