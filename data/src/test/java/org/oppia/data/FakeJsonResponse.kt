package org.oppia.data

import org.oppia.data.backends.gae.NetworkSettings

/** An object that contains fake json responses for test cases */
object FakeJsonResponse {

  /** Dummy json response with XSSI Prfix for [NetworkInterceptorTest] */
  const val DUMMY_RESPONSE_WITH_XSSI_PREFIX: String =
    NetworkSettings.XSSI_PREFIX + "\n" + "{\"is_valid_response\": true}"
  /** Dummy json response without XSSI Prfix for [NetworkInterceptorTest] */
  const val DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX: String = "{\"is_valid_response\": true}"
  /** Fake json response for [MockTopicService] */
  const val TOPIC_SERVICE_RESPONSE: String =
    "{\"canonical_story_dicts\":[{\"title\":\"Story 1\",\"description\":\"Story Description\",\"id\":\"4wv9hSZ4F67I\"}],\"is_moderator\":true,\"is_admin\":true,\"topic_name\":\"Topic1\",\"username\":\"rt4914\",\"skill_descriptions\":{\"A9j9taXAqqkV\":\"Skill 1\"},\"user_email\":\"test@example.com\",\"iframed\":false,\"additional_story_dicts\":[],\"additional_angular_modules\":[],\"is_topic_manager\":false,\"uncategorized_skill_ids\":[],\"topic_id\":\"baWJOUFeUAnn\",\"degrees_of_mastery\":{\"A9j9taXAqqkV\":null},\"subtopics\":[{\"skill_ids\":[\"A9j9taXAqqkV\"],\"title\":\"Subtopic 1\",\"id\":1}],\"is_super_admin\":true}"

}
