package org.oppia.android.util.profile

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.LooperMode

/** Tests for [CurrentUserProfileIdIntentDecorator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CurrentUserProfileIdIntentDecoratorTest {

  @Test
  fun testDecorator_decorateWithProfileId_returnsIntentWithCorrectProfileId() {
    val intent = Intent().apply {
      decorateWithUserProfileId(
        ProfileId.newBuilder().apply {
          internalId = 0
        }.build()
      )
    }
    val currentProfileId = intent.extractCurrentUserProfileId()
    assertThat(currentProfileId.internalId).isEqualTo(0)
  }

  @Test
  fun testDecorator_withoutProfileId_returnsIntentWithDefaultProfileId() {
    val currentProfileId = Intent().extractCurrentUserProfileId()
    assertThat(currentProfileId).isEqualTo(ProfileId.getDefaultInstance())
  }

  @Test
  fun testDecorateWithUserProfileId_emptyProfileProto_returnsDefaultProfileIdInstance() {
    val profileId = ProfileId.newBuilder().apply {}.build()

    val extractedProfileId = Intent().apply {
      decorateWithUserProfileId(profileId)
    }.extractCurrentUserProfileId()
    assertThat(extractedProfileId).isEqualTo(ProfileId.getDefaultInstance())
  }
}
