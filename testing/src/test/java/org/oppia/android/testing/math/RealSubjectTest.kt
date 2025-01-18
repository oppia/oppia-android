package org.oppia.android.testing.math

import android.annotation.SuppressLint
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Real

/** Tests for [RealSubject]. */
@SuppressLint("CheckResult")
@RunWith(JUnit4::class)
class RealSubjectTest {

  @Test
  fun testRational_withRationalValue_canAccessRationalSubject() {
    val real = Real.newBuilder().setRational(
      Fraction.newBuilder().setNumerator(1).setDenominator(2)
    ).build()

    val subject = RealSubject.assertThat(real).isRationalThat()

    subject.hasNumeratorThat().isEqualTo(1)
    subject.hasDenominatorThat().isEqualTo(2)
  }

  @Test
  fun testRational_withIrrationalValue_fails() {
    val real = Real.newBuilder().setIrrational(3.14).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isRationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be RATIONAL, not: IRRATIONAL"
    )
  }

  @Test
  fun testRational_withIntegerValue_fails() {
    val real = Real.newBuilder().setInteger(42).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isRationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be RATIONAL, not: INTEGER"
    )
  }

  @Test
  fun testIrrational_withIrrationalValue_canAccessDoubleSubject() {
    val real = Real.newBuilder().setIrrational(3.14159).build()

    val subject = RealSubject.assertThat(real).isIrrationalThat()

    subject.isWithin(0.00001).of(3.14159)
  }

  @Test
  fun testIrrational_withRationalValue_fails() {
    val real = Real.newBuilder().setRational(
      Fraction.newBuilder().setNumerator(1).setDenominator(2)
    ).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIrrationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be IRRATIONAL, not: RATIONAL"
    )
  }

  @Test
  fun testIrrational_withIntegerValue_fails() {
    val real = Real.newBuilder().setInteger(42).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIrrationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be IRRATIONAL, not: INTEGER"
    )
  }

  @Test
  fun testInteger_withIntegerValue_canAccessIntegerSubject() {
    val real = Real.newBuilder().setInteger(42).build()

    val subject = RealSubject.assertThat(real).isIntegerThat()

    subject.isEqualTo(42)
  }

  @Test
  fun testInteger_withRationalValue_fails() {
    val real = Real.newBuilder().setRational(
      Fraction.newBuilder().setNumerator(1).setDenominator(2)
    ).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIntegerThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be INTEGER, not: RATIONAL"
    )
  }

  @Test
  fun testInteger_withIrrationalValue_fails() {
    val real = Real.newBuilder().setIrrational(3.14).build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIntegerThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be INTEGER, not: IRRATIONAL"
    )
  }

  @Test
  fun testNull_fails() {
    val exception = assertThrows(IllegalStateException::class.java) {
      RealSubject.assertThat(null).isRationalThat()
    }

    assertThat(exception).hasMessageThat().contains("Expected real to be non-null")
  }

  @Test
  fun testUnsetType_asRational_fails() {
    val real = Real.newBuilder().build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isRationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be RATIONAL, not: REALTYPE_NOT_SET"
    )
  }

  @Test
  fun testUnsetType_asIrrational_fails() {
    val real = Real.newBuilder().build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIrrationalThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be IRRATIONAL, not: REALTYPE_NOT_SET"
    )
  }

  @Test
  fun testUnsetType_asInteger_fails() {
    val real = Real.newBuilder().build()

    val exception = assertThrows(AssertionError::class.java) {
      RealSubject.assertThat(real).isIntegerThat()
    }

    assertThat(exception).hasMessageThat().contains(
      "Expected real type to be INTEGER, not: REALTYPE_NOT_SET"
    )
  }

  @Test
  fun testInheritedProtoMethods_work() {
    val real = Real.newBuilder().setInteger(42).build()

    RealSubject.assertThat(real).isNotNull()
    RealSubject.assertThat(real).isNotEqualTo(Real.getDefaultInstance())
  }

  private fun <T : Throwable> assertThrows(
    expectedType: Class<T>,
    runnable: () -> Unit
  ): T {
    try {
      runnable()
    } catch (t: Throwable) {
      if (expectedType.isInstance(t)) {
        @Suppress("UNCHECKED_CAST")
        return t as T
      }
      throw AssertionError(
        "Expected ${expectedType.simpleName} but got ${t.javaClass.simpleName}",
        t
      )
    }
    throw AssertionError(
      "Expected ${expectedType.simpleName} to be thrown but nothing was thrown"
    )
  }
}
