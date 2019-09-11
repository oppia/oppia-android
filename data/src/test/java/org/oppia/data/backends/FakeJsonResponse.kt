package org.oppia.data.backends

import org.oppia.data.backends.gae.NetworkSettings

/** An object that contains fake json responses for test cases */
object FakeJsonResponse {

  /** Dummy json response with XSSI Prefix for [NetworkInterceptorTest] */
  const val DUMMY_RESPONSE_WITH_XSSI_PREFIX: String =
    NetworkSettings.XSSI_PREFIX + "\n" + "{\"is_valid_response\": true}"
  /** Dummy json response without XSSI Prefix for [NetworkInterceptorTest] */
  const val DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX: String = "{\"is_valid_response\": true}"
  /** Fake json response for [MockTopicService] */
  const val TOPIC_SERVICE_RESPONSE: String =
    "{\"canonical_story_dicts\":[{\"title\":\"Story 1\",\"description\":\"Story Description\",\"id\":\"4wv9hSZ4F67I\"}],\"is_moderator\":true,\"is_admin\":true,\"topic_name\":\"Topic1\",\"username\":\"rt4914\",\"skill_descriptions\":{\"A9j9taXAqqkV\":\"Skill 1\"},\"user_email\":\"test@example.com\",\"iframed\":false,\"additional_story_dicts\":[],\"additional_angular_modules\":[],\"is_topic_manager\":false,\"uncategorized_skill_ids\":[],\"topic_id\":\"baWJOUFeUAnn\",\"degrees_of_mastery\":{\"A9j9taXAqqkV\":null},\"subtopics\":[{\"skill_ids\":[\"A9j9taXAqqkV\"],\"title\":\"Subtopic 1\",\"id\":1}],\"is_super_admin\":true}"
  /** Fake json response for [MockClassroomService] */
  const val CLASSROOM_SERVICE_RESPONSE: String =
    "{\"is_super_admin\":true,\"topic_summary_dicts\":[{\"version\":3,\"language_code\":\"en\",\"subtopic_count\":0,\"canonical_story_count\":1,\"uncategorized_skill_count\":0,\"topic_model_last_updated\":1567587844908.068,\"id\":\"BpslppK1Tb89\",\"additional_story_count\":0,\"total_skill_count\":0,\"topic_model_created_on\":1567587801448.179,\"name\":\"Math\"}],\"user_email\":\"test@example.com\",\"iframed\":false,\"username\":\"veena\",\"is_topic_manager\":false,\"is_moderator\":true,\"is_admin\":true}"

}
