package org.oppia.android.scripts.gae.json

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import org.junit.Test

/** Tests for [GaeSubtopicPage]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GaeSubtopicPageTest {
  private val adapter: JsonAdapter<GaeSubtopicPage> =
    MoshiFactory.createMoshi().adapter(GaeSubtopicPage::class.java)

  @Test
  fun testParseStudyGuideSubtopicPage_readsSections() {
    val json =
      """
      {
        "id": "fractions-1",
        "topic_id": "fractions",
        "sections": [
          {
            "heading": {
              "content_id": "section_heading_0",
              "unicode_str": "What is a fraction?"
            },
            "content": {
              "content_id": "section_content_1",
              "html": "<p>A fraction is part of a whole.</p>"
            }
          },
          {
            "heading": {
              "content_id": "section_heading_2",
              "unicode_str": "How do we write it?"
            },
            "content": {
              "content_id": "section_content_3",
              "html": "<p>We write it using a numerator and denominator.</p>"
            }
          }
        ],
        "sections_schema_version": 1,
        "next_content_id_index": 4,
        "language_code": "en",
        "version": 7
      }
      """.trimIndent()

    val page = checkNotNull(adapter.fromJson(json))

    assertThat(page.id).isEqualTo("fractions-1")
    assertThat(page.topicId).isEqualTo("fractions")
    assertThat(page.sectionsSchemaVersion).isEqualTo(1)
    assertThat(page.nextContentIdIndex).isEqualTo(4)
    assertThat(page.languageCode).isEqualTo("en")
    assertThat(page.version).isEqualTo(7)
    assertThat(page.sections).hasSize(2)
    assertThat(page.sections[0].heading.contentId).isEqualTo("section_heading_0")
    assertThat(page.sections[0].heading.text).isEqualTo("What is a fraction?")
    assertThat(page.sections[0].content.contentId).isEqualTo("section_content_1")
    assertThat(page.sections[0].content.text).isEqualTo("<p>A fraction is part of a whole.</p>")
    assertThat(page.sections[1].heading.contentId).isEqualTo("section_heading_2")
    assertThat(page.sections[1].heading.text).isEqualTo("How do we write it?")
    assertThat(page.sections[1].content.contentId).isEqualTo("section_content_3")
    assertThat(page.sections[1].content.text)
      .isEqualTo("<p>We write it using a numerator and denominator.</p>")
  }
}
