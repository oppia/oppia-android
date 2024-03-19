package org.oppia.android.scripts.gae.compat

import org.oppia.android.scripts.gae.json.GaeEntityTranslations
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.VersionedStructure
import org.oppia.proto.v1.structure.LanguageType

data class CompleteExploration(
  val exploration: GaeExploration,
  val translations: Map<LanguageType, VersionedStructure<GaeEntityTranslations>>
) {
  val version: Int = exploration.version
}
