package org.oppia.android.scripts.gae.proto

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Message
import org.oppia.proto.v1.api.ClientCompatibilityContextDto
import org.oppia.proto.v1.versions.ApiVersions
import org.oppia.proto.v1.versions.ClassroomProtoVersion
import org.oppia.proto.v1.versions.ConceptCardProtoVersion
import org.oppia.proto.v1.versions.ExplorationProtoVersion
import org.oppia.proto.v1.versions.ImageProtoVersion
import org.oppia.proto.v1.versions.LanguageProtosVersion
import org.oppia.proto.v1.versions.QuestionProtoVersion
import org.oppia.proto.v1.versions.RevisionCardProtoVersion
import org.oppia.proto.v1.versions.StateProtoVersion
import org.oppia.proto.v1.versions.StructureVersions
import org.oppia.proto.v1.versions.TopicContentRequestResponseProtoVersion
import org.oppia.proto.v1.versions.TopicListRequestResponseProtoVersion
import org.oppia.proto.v1.versions.TopicSummaryProtoVersion

object ProtoVersionProvider {
  private val DEF_TOPIC_SUMMARY_VER = TopicSummaryProtoVersion.getDefaultInstance()
  private val DEF_REV_CARD_VER = RevisionCardProtoVersion.getDefaultInstance()
  private val DEF_CONCEPT_CARD_VER = ConceptCardProtoVersion.getDefaultInstance()
  private val DEF_EXP_VER = ExplorationProtoVersion.getDefaultInstance()
  private val DEF_QUESTION_VER = QuestionProtoVersion.getDefaultInstance()
  private val DEF_STATE_VER = StateProtoVersion.getDefaultInstance()
  private val DEF_LANGUAGE_VER = LanguageProtosVersion.getDefaultInstance()
  private val DEF_IMAGE_VER = ImageProtoVersion.getDefaultInstance()
  private val DEF_CLASSROOM_VER = ClassroomProtoVersion.getDefaultInstance()
  private val DEF_TOPIC_LIST_REQ_RESP_VER =
    TopicListRequestResponseProtoVersion.getDefaultInstance()
  private val DEF_TOPIC_CONTENT_REQ_RESP_VER =
    TopicContentRequestResponseProtoVersion.getDefaultInstance()

  fun createLatestTopicSummaryProtoVersion(): TopicSummaryProtoVersion =
    createStructureVersionProto(DEF_TOPIC_SUMMARY_VER, TopicSummaryProtoVersion.Builder::setVersion)

  fun createLatestRevisionCardProtoVersion(): RevisionCardProtoVersion =
    createStructureVersionProto(DEF_REV_CARD_VER, RevisionCardProtoVersion.Builder::setVersion)

  fun createLatestConceptCardProtoVersion(): ConceptCardProtoVersion =
    createStructureVersionProto(DEF_CONCEPT_CARD_VER, ConceptCardProtoVersion.Builder::setVersion)

  fun createLatestExplorationProtoVersion(): ExplorationProtoVersion =
    createStructureVersionProto(DEF_EXP_VER, ExplorationProtoVersion.Builder::setVersion)

  fun createLatestQuestionProtoVersion(): QuestionProtoVersion =
    createStructureVersionProto(DEF_QUESTION_VER, QuestionProtoVersion.Builder::setVersion)

  fun createLatestStateProtoVersion(): StateProtoVersion =
    createStructureVersionProto(DEF_STATE_VER, StateProtoVersion.Builder::setVersion)

  fun createLatestLanguageProtosVersion(): LanguageProtosVersion =
    createStructureVersionProto(DEF_LANGUAGE_VER, LanguageProtosVersion.Builder::setVersion)

  fun createLatestImageProtoVersion(): ImageProtoVersion =
    createStructureVersionProto(DEF_IMAGE_VER, ImageProtoVersion.Builder::setVersion)

  fun createLatestClassroomProtoVersion(): ClassroomProtoVersion =
    createStructureVersionProto(DEF_CLASSROOM_VER, ClassroomProtoVersion.Builder::setVersion)

  fun createLatestTopicListProtoVersion(): TopicListRequestResponseProtoVersion {
    return createApiVersionProto(
      DEF_TOPIC_LIST_REQ_RESP_VER, TopicListRequestResponseProtoVersion.Builder::setVersion
    )
  }

  fun createLatestTopicContentProtoVersion(): TopicContentRequestResponseProtoVersion {
    return createApiVersionProto(
      DEF_TOPIC_CONTENT_REQ_RESP_VER, TopicContentRequestResponseProtoVersion.Builder::setVersion
    )
  }

  fun createCompatibilityContext(): ClientCompatibilityContextDto {
    return ClientCompatibilityContextDto.newBuilder().apply {
      topicListRequestResponseProtoVersion = createLatestTopicListProtoVersion()
      topicContentRequestResponseProtoVersion = createLatestTopicContentProtoVersion()
      topicSummaryProtoVersion = createLatestTopicSummaryProtoVersion()
      revisionCardProtoVersion = createLatestRevisionCardProtoVersion()
      conceptCardProtoVersion = createLatestConceptCardProtoVersion()
      explorationProtoVersion = createLatestExplorationProtoVersion()
      questionProtoVersion = createLatestQuestionProtoVersion()
      stateProtoVersion = createLatestStateProtoVersion()
      languageProtosVersion = createLatestLanguageProtosVersion()
      imageProtoVersion = createLatestImageProtoVersion()
      classroomProtoVersion = createLatestClassroomProtoVersion()
    }.build()
  }

  private inline fun <reified M : Message, reified B : Message.Builder> createStructureVersionProto(
    defaultMessage: M,
    setVersion: B.(Int) -> B
  ): M = createVersionProto(defaultMessage, setVersion) { extractLatestStructureVersion() }

  private inline fun <reified M : Message, reified B : Message.Builder> createApiVersionProto(
    defaultMessage: M,
    setVersion: B.(Int) -> B
  ): M = createVersionProto(defaultMessage, setVersion) { extractLatestApiVersion() }

  private inline fun <reified M : Message, reified B : Message.Builder> createVersionProto(
    defaultMessage: M,
    setVersion: B.(Int) -> B,
    getLatestVersion: Descriptor.() -> Int
  ): M {
    return (defaultMessage.newBuilderForType() as B).apply {
      setVersion(descriptorForType.getLatestVersion())
    }.build() as M
  }

  private fun Descriptor.extractLatestStructureVersion(): Int =
    options.getExtension(StructureVersions.latestStructureProtoVersion)

  private fun Descriptor.extractLatestApiVersion(): Int =
    options.getExtension(ApiVersions.latestApiProtoVersion)
}
