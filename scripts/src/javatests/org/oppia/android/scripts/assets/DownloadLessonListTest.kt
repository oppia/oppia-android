package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.TextFormat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.DownloadListVersions
import org.oppia.proto.v1.api.TopicListResponseDto
import org.oppia.proto.v1.api.TopicListResponseDto.AvailableTopicDto
import org.oppia.proto.v1.api.TopicListResponseDto.DownloadableTopicDto
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.ContentLocalizationsDto
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.SingleLocalizableTextDto
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.SubtitledTextDto

/** Tests for the pinned textproto written by [LessonListDownloader]. */
@Suppress("FunctionName")
class DownloadLessonListTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun testWritePinnedLessonVersions_capturesTitlesAndVersions() {
    val chapter = ChapterSummaryDto.newBuilder()
      .setExplorationId("exploration-id")
      .setContentVersion(3)
      .setTitle(titleReference())
      .setLocalizations(titleLocalizations("Comparing fractions"))
      .build()
    val story = StorySummaryDto.newBuilder()
      .setId("story-id")
      .setContentVersion(2)
      .setTitle(titleReference())
      .setLocalizations(titleLocalizations("A baker's story"))
      .addChapters(chapter)
      .build()
    val topic = DownloadableTopicSummaryDto.newBuilder()
      .setId("topic-id")
      .setContentVersion(1)
      .setName(titleReference())
      .setLocalizations(titleLocalizations("Fractions"))
      .addStorySummaries(story)
      .build()
    val response = TopicListResponseDto.newBuilder()
      .addAvailableTopics(
        AvailableTopicDto.newBuilder()
          .setDownloadableTopic(DownloadableTopicDto.newBuilder().setTopicSummary(topic))
      )
      .build()

    val output = tempFolder.newFile("pinned_lesson_versions.textproto")
    LessonListDownloader.writePinnedLessonVersions(response, output)

    val actual = TextFormat.parse(output.readText(), DownloadListVersions::class.java)
    val expected = TextFormat.parse(
      """
      tracked_topic_info {
        id: "topic-id"
        content_version: 1
        title: "Fractions"
        story_info {
          id: "story-id"
          content_version: 2
          title: "A baker's story"
          chapter_info {
            exploration_id: "exploration-id"
            exploration_content_version: 3
            title: "Comparing fractions"
          }
        }
      }
      """.trimIndent(),
      DownloadListVersions::class.java
    )
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun testParsePinnedLessonVersions_legacyTextprotoWithoutTitles() {
    val versions = TextFormat.parse(
      """
      tracked_topic_info {
        id: "topic-id"
        content_version: 1
        story_info {
          id: "story-id"
          content_version: 2
          chapter_info {
            exploration_id: "exploration-id"
            exploration_content_version: 3
          }
        }
      }
      """.trimIndent(),
      DownloadListVersions::class.java
    )

    val topic = versions.trackedTopicInfoList.single()
    val story = topic.storyInfoList.single()
    val chapter = story.chapterInfoList.single()
    assertThat(topic.title).isEmpty()
    assertThat(story.title).isEmpty()
    assertThat(chapter.title).isEmpty()
    assertThat(chapter.explorationId).isEqualTo("exploration-id")
    assertThat(chapter.explorationContentVersion).isEqualTo(3)
  }

  private fun titleReference(): SubtitledTextDto =
    SubtitledTextDto.newBuilder().setContentId("title").build()

  private fun titleLocalizations(title: String): ContentLocalizationsDto {
    val text = LocalizableTextDto.newBuilder()
      .setSingleLocalizableText(SingleLocalizableTextDto.newBuilder().setText(title))
      .build()
    return ContentLocalizationsDto.newBuilder()
      .setDefaultMapping(
        ContentLocalizationDto.newBuilder().putLocalizableTextContentMapping("title", text)
      )
      .build()
  }
}
