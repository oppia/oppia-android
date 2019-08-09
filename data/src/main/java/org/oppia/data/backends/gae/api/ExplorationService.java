package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.ExplorationContainer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ExplorationService {

  @GET("explorehandler/init/{exploration_id}")
  Call<ExplorationContainer> getExplorationById(@Path ("exploration _id") String explorationId);

}
