package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.Story;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StoryService {

  @GET("story_data_handler/{story_id}")
  Call<Story> getStory(@Path("story_id") String storyId, @Query("user_id") String userId, @Query("user") String user);

}