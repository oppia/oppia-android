package org.oppia.android.util.namevalidator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NameValidatorTest {

  private val allowedNamesList = listOf<String>("जिष्णु", "Ben-Henning", "Rajat.T", "جيشنو")

  private val unAllowedNamesList =
    listOf<String>("जिष्णु7", "Ben_Henning", "Rajat..T", "جيشنو^&&", " ", ".")

  @Test
  fun testNameValidator_addUnAllowedName_returnFalse() {
    unAllowedNamesList.forEach {
      assertFalse(NameValidator.validate(it))
    }
  }

  @Test
  fun testNameValidator_addAllowedName_returnTrue() {
    allowedNamesList.forEach {
      assertTrue(NameValidator.validate(it))
    }
  }
}
