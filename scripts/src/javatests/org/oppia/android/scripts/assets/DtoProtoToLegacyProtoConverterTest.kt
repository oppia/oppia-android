package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToSubtopicRecord
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.ReferencedImageDto
import org.oppia.proto.v1.structure.RevisionCardDto
import org.oppia.proto.v1.structure.SingleLocalizableTextDto
import org.oppia.proto.v1.structure.StudyGuideSectionDto
import org.oppia.proto.v1.structure.SubtitledTextDto
import org.oppia.proto.v1.structure.SubtopicPageIdDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import org.oppia.proto.v1.structure.ThumbnailDto
import kotlin.test.assertFailsWith

/** Tests for [DtoProtoToLegacyProtoConverter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DtoProtoToLegacyProtoConverterTest {
  @Test
  fun testConvertRevisionCard_withStudyGuideSections_populatesStructuredSections() {
    val revisionCard = createRevisionCard(
      sections = listOf(
        StudyGuideSection(
          headingContentId = "section_heading_0",
          headingText = "What is a fraction?",
          contentContentId = "section_content_1",
          contentHtml = "<p>A fraction is part of a whole.</p>"
        ),
        StudyGuideSection(
          headingContentId = "section_heading_2",
          headingText = "How do we write it?",
          contentContentId = "section_content_3",
          contentHtml = "<p>We write it using a numerator and denominator.</p>"
        )
      )
    )

    val subtopicRecord =
      revisionCard.convertToSubtopicRecord(
        imageReferenceReplacements = mapOf(),
        subtopicSummaryDto = createSubtopicSummary(),
        languagePackDtos = listOf()
      )

    assertThat(subtopicRecord.sectionsList).hasSize(2)
    assertThat(subtopicRecord.sectionsList[0].heading.unicodeStr).isEqualTo("What is a fraction?")
    assertThat(subtopicRecord.sectionsList[0].content.html)
      .isEqualTo("<p>A fraction is part of a whole.</p>")
    assertThat(subtopicRecord.sectionsList[1].heading.unicodeStr)
      .isEqualTo("How do we write it?")
    assertThat(subtopicRecord.sectionsList[1].content.html)
      .isEqualTo("<p>We write it using a numerator and denominator.</p>")
  }

  @Test
  fun testConvertRevisionCard_withStudyGuideSections_combinesLegacyPageContentsLikeWeb() {
    val revisionCard = createRevisionCard(
      sections = listOf(
        StudyGuideSection(
          headingContentId = "section_heading_0",
          headingText = "What is a fraction?",
          contentContentId = "section_content_1",
          contentHtml = "<p>A fraction is part of a whole.</p>"
        ),
        StudyGuideSection(
          headingContentId = "section_heading_2",
          headingText = "How do we write it?",
          contentContentId = "section_content_3",
          contentHtml = "<p>We write it using a numerator and denominator.</p>"
        )
      )
    )

    val subtopicRecord =
      revisionCard.convertToSubtopicRecord(
        imageReferenceReplacements = mapOf(),
        subtopicSummaryDto = createSubtopicSummary(),
        languagePackDtos = listOf()
      )

    assertThat(subtopicRecord.pageContents.contentId).isEqualTo("content")
    assertThat(subtopicRecord.pageContents.html).isEqualTo(
      "<p><strong>What is a fraction?</strong></p>\n\n" +
        "<p>A fraction is part of a whole.</p>\n\n" +
        "<p><strong>How do we write it?</strong></p>\n\n" +
        "<p>We write it using a numerator and denominator.</p>"
    )
  }

  @Test
  fun testConvertRevisionCard_withNoStudyGuideSections_throws() {
    val revisionCard = createRevisionCard(sections = listOf())

    val exception = assertFailsWith<IllegalStateException> {
      revisionCard.convertToSubtopicRecord(
        imageReferenceReplacements = mapOf(),
        subtopicSummaryDto = createSubtopicSummary(),
        languagePackDtos = listOf()
      )
    }

    assertThat(exception).hasMessageThat().contains("Expected at least one study guide section")
  }

  private fun createRevisionCard(sections: List<StudyGuideSection>): RevisionCardDto {
    val defaultLocalization = ContentLocalizationDto.newBuilder().apply {
      language = LanguageType.ENGLISH
      putLocalizableTextContentMapping("title", createLocalizableText("Fractions"))
      sections.forEach {
        putLocalizableTextContentMapping(it.headingContentId, createLocalizableText(it.headingText))
        putLocalizableTextContentMapping(it.contentContentId, createLocalizableText(it.contentHtml))
      }
      thumbnail = ThumbnailDto.newBuilder().apply {
        referencedImage = ReferencedImageDto.newBuilder().apply {
          filename = "thumbnail.png"
        }.build()
        backgroundColorRgb = 0x123456
      }.build()
    }.build()
    return RevisionCardDto.newBuilder().apply {
      id = SubtopicPageIdDto.newBuilder().apply {
        topicId = "fractions"
        subtopicIndex = 1
      }.build()
      title = createSubtitledText(contentId = "title")
      addAllSections(
        sections.map {
          StudyGuideSectionDto.newBuilder().apply {
            heading = createSubtitledText(contentId = it.headingContentId)
            content = createSubtitledText(contentId = it.contentContentId)
          }.build()
        }
      )
      this.defaultLocalization = defaultLocalization
    }.build()
  }

  private fun createSubtopicSummary(): SubtopicSummaryDto {
    return SubtopicSummaryDto.newBuilder().apply {
      index = 1
      addReferencedSkillIds("skill_1")
    }.build()
  }

  private fun createSubtitledText(contentId: String): SubtitledTextDto =
    SubtitledTextDto.newBuilder().apply {
      this.contentId = contentId
    }.build()

  private fun createLocalizableText(text: String): LocalizableTextDto =
    LocalizableTextDto.newBuilder().apply {
      singleLocalizableText = SingleLocalizableTextDto.newBuilder().apply {
        this.text = text
      }.build()
    }.build()

  private data class StudyGuideSection(
    val headingContentId: String,
    val headingText: String,
    val contentContentId: String,
    val contentHtml: String
  )
}
