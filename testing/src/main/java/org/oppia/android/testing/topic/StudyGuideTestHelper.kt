package org.oppia.android.testing.topic

import org.oppia.android.app.model.StudyGuide
import org.oppia.android.app.model.StudyGuideSection
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import javax.inject.Inject
import javax.inject.Singleton

/** The subtopic title of the test_topic_id_0 subtopic 1 study guide (default English content). */
const val TEST_STUDY_GUIDE_SUBTOPIC_TITLE = "Test subtopic"

/**
 * Test fixtures for study guides, used to verify [org.oppia.android.domain.topic.TopicController]'s
 * `getStudyGuide()` and [org.oppia.android.domain.topic.StudyGuideRetriever] through
 * `TopicControllerTest`.
 *
 * These build the [StudyGuide]/[StudyGuideSection] protos that are expected to be loaded from the
 * local test assets, so tests can assert against them without duplicating large proto literals
 * inline. This mirrors the fixture-helper pattern already used in the testing library (e.g.
 * [org.oppia.android.testing.AssertionHelpers]). [StudyGuideRetriever] follows the
 * [org.oppia.android.domain.topic.RevisionCardRetriever] pattern and, like it, has no dedicated
 * test file; it is covered through `TopicControllerTest` using these fixtures.
 */
@Singleton
class StudyGuideTestHelper @Inject constructor() {
  /**
   * Returns the ordered list of [StudyGuideSection]s expected for the test_topic_id_0 subtopic 1
   * study guide in the default (English) content language. This is the multi-section case: the
   * first section is plain prose and the second embeds a skillreview rich-text tag.
   */
  fun createTestTopicSubtopic1Sections(): List<StudyGuideSection> = listOf(
    createSection(
      headingContentId = "section_heading_0",
      heading = "What is a test subtopic?",
      contentContentId = "section_content_1",
      content = "<p>Description of subtopic is here. This is the first section of a sample study" +
        " guide with dummy content not related to anything in particular.</p>"
    ),
    createSection(
      headingContentId = "section_heading_2",
      heading = "Review related skills",
      contentContentId = "section_content_3",
      content = "<p>This section reviews a related skill: " +
        "<oppia-noninteractive-skillreview " +
        "skill_id-with-value=\"&amp;quot;test_skill_id_0&amp;quot;\" " +
        "text-with-value=\"&amp;quot;test_skill_id_0 concept card&amp;quot;\">" +
        "</oppia-noninteractive-skillreview>.</p><p>Worked examples:</p>" +
        "<oppia-noninteractive-workedexample question-with-value=\"&amp;quot;" +
        "&amp;lt;strong&amp;gt;What is one half as a fraction?&amp;lt;/strong&amp;gt;" +
        "&amp;quot;\" answer-with-value=\"&amp;quot;&amp;lt;em&amp;gt;One half is 1/2." +
        "&amp;lt;/em&amp;gt;&amp;quot;\"></oppia-noninteractive-workedexample>" +
        "<oppia-noninteractive-workedexample question-with-value=\"&amp;quot;&amp;quot;\" " +
        "answer-with-value=\"&amp;quot;This answer should be ignored.&amp;quot;\">" +
        "</oppia-noninteractive-workedexample><oppia-noninteractive-workedexample " +
        "question-with-value=\"&amp;quot;This question should be ignored.&amp;quot;\" " +
        "answer-with-value=\"&amp;quot;&amp;quot;\"></oppia-noninteractive-workedexample>" +
        "<oppia-noninteractive-workedexample " +
        "question-with-value=\"&amp;quot;Malformed question\" " +
        "answer-with-value=\"&amp;quot;This answer should be ignored.&amp;quot;\">" +
        "</oppia-noninteractive-workedexample>"
    )
  )

  /**
   * Returns a [StudyGuideSection] built from the provided heading and content. [headingContentId]
   * and [contentContentId] are the content IDs used to look up translations, [heading] is the
   * section's plain-text heading, and [content] is its (possibly HTML) body.
   */
  fun createSection(
    headingContentId: String,
    heading: String,
    contentContentId: String,
    content: String
  ): StudyGuideSection = StudyGuideSection.newBuilder().apply {
    this.heading = SubtitledUnicode.newBuilder().apply {
      contentId = headingContentId
      unicodeStr = heading
    }.build()
    this.content = SubtitledHtml.newBuilder().apply {
      contentId = contentContentId
      html = content
    }.build()
  }.build()
}
