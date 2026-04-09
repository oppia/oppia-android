package org.oppia.android.app.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [ForcedActivityLanguageMode]. */
class ForcedActivityLanguageModeTest {
  @Test
  fun testValues_containsExpectedModes() {
    assertThat(ForcedActivityLanguageMode.values().asList()).containsExactly(
      ForcedActivityLanguageMode.USE_APP_LANGUAGE,
      ForcedActivityLanguageMode.USE_SYSTEM_LANGUAGE,
      ForcedActivityLanguageMode.USE_ENGLISH
    ).inOrder()
  }
}
