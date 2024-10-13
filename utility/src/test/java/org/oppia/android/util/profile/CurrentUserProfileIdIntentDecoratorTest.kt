package org.oppia.android.util.profile

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
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
          loggedInInternalProfileId = 1
        }.build()
      )
    }
    val currentProfileId = intent.extractCurrentUserProfileId()
    assertThat(currentProfileId.loggedInInternalProfileId).isEqualTo(1)
  }

  @Test
  fun testDecorator_withoutProfileId_returnsIntentWithDefaultProfileId() {
    val currentProfileId = Intent().extractCurrentUserProfileId()
    assertThat(currentProfileId).isEqualToDefaultInstance()
  }

  @Test
  fun testDecorateIntentWithUserProfileId_emptyProfileId_returnsDefaultProfileIdInstance() {
    val profileId = ProfileId.newBuilder().apply {}.build()

    val extractedProfileId = Intent().apply {
      decorateWithUserProfileId(profileId)
    }.extractCurrentUserProfileId()
    assertThat(extractedProfileId).isEqualToDefaultInstance()
  }

  @Test
  fun testDecorator_decorateBundleWithProfileId_returnsBundleWithCorrectProfileId() {
    val profileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = 1 }.build()
    val bundle = Bundle().apply {
      decorateWithUserProfileId(profileId)
    }

    val currentUserProfileId = bundle.extractCurrentUserProfileId()
    assertThat(currentUserProfileId).isEqualTo(profileId)
  }

  @Test
  fun testDecorator_withoutProfileId_returnsBundleWithDefaultProfileId() {
    val currentProfileId = Bundle().extractCurrentUserProfileId()
    assertThat(currentProfileId).isEqualToDefaultInstance()
  }

  @Test
  fun testDecorateBundleWithUserProfileId_emptyProfileId_returnsDefaultProfileIdInstance() {
    val profileId = ProfileId.newBuilder().apply {}.build()
    val extractedProfileId = Bundle().apply {
      decorateWithUserProfileId(profileId)
    }.extractCurrentUserProfileId()
    assertThat(extractedProfileId).isEqualToDefaultInstance()
  }
}
