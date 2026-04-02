package org.oppia.android.util.profile

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Suppress("FunctionName")
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileIdMigrationUtilTest {

  @Test
  fun testMigrate_legacyProfileIdWithValidId_migratesCorrectly() {
    val legacyProfileId = LegacyProfileId.newBuilder().setInternalId(5).build()
    val profileId = legacyProfileId.toProfileId()
    assertThat(profileId.hasInternalId()).isTrue()
    assertThat(profileId.internalId).isEqualTo(5)
  }

  @Test
  fun testMigrate_legacyProfileIdWithZeroId_convertsToNull() {
    val legacyProfileId = LegacyProfileId.newBuilder().setInternalId(0).build()
    val profileId = legacyProfileId.toProfileId()
    assertThat(profileId.hasInternalId()).isFalse()
  }

  @Test
  fun testMigrate_uninitializedProfileId_hasNoInternalId() {
    val profileId = ProfileId.newBuilder().build()
    assertThat(profileId.hasInternalId()).isFalse()
  }

  @Test
  fun testToLegacy_profileIdWithInternalId_convertsCorrectly() {
    val profileId = ProfileId.newBuilder().setInternalId(10).build()
    val legacyProfileId = profileId.toLegacyProfileId()
    assertThat(legacyProfileId.internalId).isEqualTo(10)
  }

  @Test
  fun testToLegacy_uninitializedProfileId_convertsToZero() {
    val profileId = ProfileId.newBuilder().build()
    val legacyProfileId = profileId.toLegacyProfileId()
    assertThat(legacyProfileId.internalId).isEqualTo(0)
  }
}
