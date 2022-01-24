package org.oppia.android.util.profile

import com.google.common.truth.Truth
import org.junit.Test

class NameValidatorTest {

  private val allowedNamesList = listOf<String>("जिष्णु", "Ben-Henning", "Rajat.T", "جيشنو")

  private val unAllowedNamesList =
    listOf<String>("जिष्णु7", "Ben_Henning", "Rajat..T", "جيشنو^&&", " ", ".", "Ben Henning")

  @Test
  fun testNameValidator_addUnAllowedName_returnFalse() {
    unAllowedNamesList.forEach {
      Truth.assertThat(!NameValidator.isNameValid(it))
    }
  }

  @Test
  fun testNameValidator_addAllowedName_returnTrue() {
    allowedNamesList.forEach {
      Truth.assertThat(NameValidator.isNameValid(it))
    }
  }
}
