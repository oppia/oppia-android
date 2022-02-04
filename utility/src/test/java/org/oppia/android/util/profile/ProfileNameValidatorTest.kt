package org.oppia.android.util.profile

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class ProfileNameValidatorTest {

  @Inject
  lateinit var profileNameValidator: ProfileNameValidator

  private val allowedNames = listOf<String>("जिष्णु", "Ben-Henning", "Rajat.T", "جيشنو")

  private val disallowedNames =
    listOf<String>("जिष्णु7", "Ben_Henning", "Rajat..T", "جيشنو^&&", " ", ".", "Ben Henning")

  @Before
  fun setup() {
    profileNameValidator = ProfileNameValidator()
  }

  @Test
  fun testNameValidator_addDisallowedName_returnFalse() {
    disallowedNames.forEach {
      assertThat(profileNameValidator.isNameValid(it)).isFalse()
    }
  }

  @Test
  fun testNameValidator_addAllowedName_returnTrue() {
    allowedNames.forEach {
      assertThat(profileNameValidator.isNameValid(it)).isTrue()
    }
  }
}
