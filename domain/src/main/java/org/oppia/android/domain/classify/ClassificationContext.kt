package org.oppia.android.domain.classify

import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.WrittenTranslationContext

/**
 * Represents the context provided to classifiers when they're classifying an answer.
 *
 * This object provides context for the interaction and learner settings to help classifiers
 * properly categorize and process answers.
 *
 * @property writtenTranslationContext the [WrittenTranslationContext] currently used by the learner
 * @property customizationArgs the customization arguments defined by the current interaction
 */
data class ClassificationContext(
  val writtenTranslationContext: WrittenTranslationContext =
    WrittenTranslationContext.getDefaultInstance(),
  val customizationArgs: Map<String, SchemaObject> = mapOf()
)
