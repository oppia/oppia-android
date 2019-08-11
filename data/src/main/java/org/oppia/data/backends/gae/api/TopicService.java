package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.Subtopic;
import org.oppia.data.backends.gae.model.Topic;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TopicService {

  @GET("topic_data_handler/{topic_name}")
  Call<Topic> getTopicByTopicName(@Path("topic_name") String topicName);

}
