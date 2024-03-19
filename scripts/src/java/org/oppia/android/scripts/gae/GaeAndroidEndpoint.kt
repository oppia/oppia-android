package org.oppia.android.scripts.gae

import kotlinx.coroutines.Deferred
import org.oppia.proto.v1.api.TopicContentRequestDto
import org.oppia.proto.v1.api.TopicContentResponseDto
import org.oppia.proto.v1.api.TopicListRequestDto
import org.oppia.proto.v1.api.TopicListResponseDto

interface GaeAndroidEndpoint {
  fun fetchTopicListAsync(
    request: TopicListRequestDto, reportProgress: (Int, Int) -> Unit = { _, _ -> }
  ): Deferred<TopicListResponseDto>

  fun fetchTopicContentAsync(
    request: TopicContentRequestDto, reportProgress: (Int, Int) -> Unit = { _, _ -> }
  ): Deferred<TopicContentResponseDto>
}
