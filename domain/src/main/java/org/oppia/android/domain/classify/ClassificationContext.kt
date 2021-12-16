package org.oppia.android.domain.classify

import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.WrittenTranslationContext

data class ClassificationContext(
  val writtenTranslationContext: WrittenTranslationContext =
    WrittenTranslationContext.getDefaultInstance(),
  val customizationArgs: Map<String, SchemaObject> = mapOf()
)
