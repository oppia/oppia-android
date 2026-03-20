package org.oppia.android.scripts.gae.proto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [ProtoVersionProvider]. */
class ProtoVersionProviderTest {
  @Test
  fun testCreateLatestProtoVersions_arePositive() {
    assertThat(
      ProtoVersionProvider.createLatestTopicSummaryProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestRevisionCardProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestConceptCardProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestExplorationProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestQuestionProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestStateProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestLanguageProtosVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestImageProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestClassroomProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestTopicListProtoVersion().version
    ).isGreaterThan(0)
    assertThat(
      ProtoVersionProvider.createLatestTopicContentProtoVersion().version
    ).isGreaterThan(0)
  }

  @Test
  fun testCreateCompatibilityContext_matchesLatestVersions() {
    val compatibilityContext = ProtoVersionProvider.createCompatibilityContext()

    assertThat(compatibilityContext.topicListRequestResponseProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestTopicListProtoVersion().version)
    assertThat(compatibilityContext.topicContentRequestResponseProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestTopicContentProtoVersion().version)
    assertThat(compatibilityContext.topicSummaryProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestTopicSummaryProtoVersion().version)
    assertThat(compatibilityContext.revisionCardProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestRevisionCardProtoVersion().version)
    assertThat(compatibilityContext.conceptCardProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestConceptCardProtoVersion().version)
    assertThat(compatibilityContext.explorationProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestExplorationProtoVersion().version)
    assertThat(compatibilityContext.questionProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestQuestionProtoVersion().version)
    assertThat(compatibilityContext.stateProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestStateProtoVersion().version)
    assertThat(compatibilityContext.languageProtosVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestLanguageProtosVersion().version)
    assertThat(compatibilityContext.imageProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestImageProtoVersion().version)
    assertThat(compatibilityContext.classroomProtoVersion.version)
      .isEqualTo(ProtoVersionProvider.createLatestClassroomProtoVersion().version)
  }
}
