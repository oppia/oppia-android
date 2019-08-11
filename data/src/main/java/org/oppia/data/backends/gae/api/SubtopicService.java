package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.Subtopic;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SubtopicService {

  @GET("subtopic_data_handler/{topic_id}/{subtopic_id}")
  Call<Subtopic> getSubtopic(@Path("topic_id") String topicId, @Path("subtopic_id") String subtopicId);

}
