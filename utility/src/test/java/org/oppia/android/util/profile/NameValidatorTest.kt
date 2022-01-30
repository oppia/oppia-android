package org.oppia.android.util.profile

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NameValidatorTest {

  private val allowedNames = listOf<String>("जिष्णु", "Ben-Henning", "Rajat.T", "جيشنو")

  private val disallowedNames =
    listOf<String>("जिष्णु7", "Ben_Henning", "Rajat..T", "جيشنو^&&", " ", ".", "Ben Henning")

  @Test
  fun testNameValidator_addDisallowedName_returnFalse() {
    disallowedNames.forEach {
      assertThat(NameValidator.isNameValid(it)).isEqualTo(false)
    }
  }

  @Test
  fun testNameValidator_addAllowedName_returnTrue() {
    allowedNames.forEach {
      assertThat(NameValidator.isNameValid(it)).isEqualTo(true)
    }
  }
}
