package org.oppia.android.scripts.gae.compat

import org.oppia.android.scripts.gae.json.GaeEntityTranslation
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.VersionedStructure
import org.oppia.proto.v1.structure.LanguageType

data class CompleteExploration(
  val exploration: GaeExploration,
  val translations: Map<LanguageType, GaeEntityTranslation>
) : VersionedStructure {
  override val version = exploration.version
}
