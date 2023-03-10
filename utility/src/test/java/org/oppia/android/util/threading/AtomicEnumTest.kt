package org.oppia.android.util.threading

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner

/** Tests for [AtomicEnum]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
class AtomicEnumTest {
  @Parameter lateinit var enumToSet: String

  private val largeEnumValue by lazy { LargeEnum.valueOf(enumToSet) }

  @Test
  fun testGetValue_simpleAtomicEnum_returnsInitialValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)

    val currentValue = atomicEnum.value

    assertThat(currentValue).isEqualTo(SimpleEnum.FIRST)
  }

  @Test
  fun testGetValue_simpleAtomicEnum_afterSetValue_returnsUpdatedValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)
    atomicEnum.value = SimpleEnum.SECOND

    val currentValue = atomicEnum.value

    assertThat(currentValue).isEqualTo(SimpleEnum.SECOND)
  }

  @Test
  @RunParameterized(
    Iteration("a", "enumToSet=A"),
    Iteration("b", "enumToSet=B"),
    Iteration("c", "enumToSet=C"),
    Iteration("d", "enumToSet=D"),
    Iteration("e", "enumToSet=E"),
    Iteration("f", "enumToSet=F"),
    Iteration("g", "enumToSet=G"),
    Iteration("h", "enumToSet=H"),
    Iteration("i", "enumToSet=I"),
    Iteration("j", "enumToSet=J")
  )
  fun testGetValue_largeAtomicEnum_setAllPossibleValues_returnsUpdatedValue() {
    val atomicEnum = AtomicEnum.create(initial = LargeEnum.A)
    atomicEnum.value = largeEnumValue

    val currentValue = atomicEnum.value

    assertThat(currentValue).isEqualTo(largeEnumValue)
  }

  @Test
  fun testGetAndSetValue_simpleAtomicEnum_returnsInitialValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)

    val currentValue = atomicEnum.getAndSet(SimpleEnum.SECOND)

    // The initial value should be returned.
    assertThat(currentValue).isEqualTo(SimpleEnum.FIRST)
  }

  @Test
  fun testGetAndSetValue_simpleAtomicEnum_afterSetValue_returnsFirstUpdatedValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)
    atomicEnum.value = SimpleEnum.SECOND

    val currentValue = atomicEnum.getAndSet(SimpleEnum.THIRD)

    // The previous value should be returned.
    assertThat(currentValue).isEqualTo(SimpleEnum.SECOND)
  }

  @Test
  fun testGetValue_simpleAtomicEnum_afterGetAndSetValue_returnsUpdatedValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)
    atomicEnum.getAndSet(SimpleEnum.SECOND)

    val currentValue = atomicEnum.value

    // getAndSet should have a side effect of changing the enum's value.
    assertThat(currentValue).isEqualTo(SimpleEnum.SECOND)
  }

  @Test
  fun testGetValue_simpleAtomicEnum_afterGetAndSetValue_afterEarlierSetValue_returnsLatestValue() {
    val atomicEnum = AtomicEnum.create(initial = SimpleEnum.FIRST)
    atomicEnum.value = SimpleEnum.SECOND
    atomicEnum.getAndSet(SimpleEnum.THIRD)

    val currentValue = atomicEnum.value

    // getAndSet should have a side effect of changing the enum's value, overwriting previous sets.
    assertThat(currentValue).isEqualTo(SimpleEnum.THIRD)
  }

  @Test
  fun testCreate_enumWithNoConstants_failsSinceInitialIsMissing() {
    val exception = assertThrows(IllegalArgumentException::class) {
      AtomicEnum.create(SimpleEnum.FIRST, possibilities = emptyList())
    }

    assertThat(exception).hasMessageThat().contains("Possibilities don't include initial value")
  }

  @Test
  fun testGetValue_enumWithOnlyInitialConstant_returnsInitialValue() {
    val atomicEnum = AtomicEnum.create(SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST))

    val currentValue = atomicEnum.value

    assertThat(currentValue).isEqualTo(SimpleEnum.FIRST)
  }

  @Test
  fun testGetValue_enumWithOnlyInitialConstant_afterSettingSameValue_returnsInitialValue() {
    val atomicEnum = AtomicEnum.create(SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST))
    atomicEnum.value = SimpleEnum.FIRST // The initial value is accepted, so it can be set.

    val currentValue = atomicEnum.value

    assertThat(currentValue).isEqualTo(SimpleEnum.FIRST)
  }

  @Test
  fun testSetValue_enumWithOnlyInitialConstant_failsSinceValueIsMissing() {
    val atomicEnum = AtomicEnum.create(SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST))

    val exception = assertThrows(IllegalArgumentException::class) {
      atomicEnum.value = SimpleEnum.SECOND
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Constant is not in the supported list of constants")
  }

  @Test
  fun testGetValue_enumWithTwoConstants_afterSetToOtherValue_returnsUpdatedValue() {
    val atomicEnum =
      AtomicEnum.create(
        SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST, SimpleEnum.SECOND)
      )
    atomicEnum.value = SimpleEnum.SECOND

    val currentValue = atomicEnum.value

    // The second enum value is valid.
    assertThat(currentValue).isEqualTo(SimpleEnum.SECOND)
  }

  @Test
  fun testSetValue_enumWithTwoConstants_toUnsupportedValue_failsSinceValueIsMissing() {
    val atomicEnum =
      AtomicEnum.create(
        SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST, SimpleEnum.SECOND)
      )

    val exception = assertThrows(IllegalArgumentException::class) {
      // The third constant isn't among the supported constants.
      atomicEnum.value = SimpleEnum.THIRD
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Constant is not in the supported list of constants")
  }

  @Test
  fun testGetAndSetValue_enumWithOnlyInitialConstant_failsSinceValueIsMissing() {
    val atomicEnum = AtomicEnum.create(SimpleEnum.FIRST, possibilities = listOf(SimpleEnum.FIRST))

    val exception = assertThrows(IllegalArgumentException::class) {
      atomicEnum.getAndSet(SimpleEnum.SECOND)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Constant is not in the supported list of constants")
  }

  private enum class SimpleEnum {
    FIRST,
    SECOND,
    THIRD
  }

  private enum class LargeEnum {
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J
  }
}
