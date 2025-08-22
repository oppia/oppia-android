package org.oppia.android.util.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/** Tests for [IterableExtensions]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class IterableExtensionsTest {

  @Test
  fun testSafeForEach_withNonEmptyList_executesActionForEachElement() {
    val list = listOf(1, 2, 3)
    val collected = mutableListOf<Int>()

    list.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(1, 2, 3).inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyList_executesNoActions() {
    val list = emptyList<Int>()
    var actionExecuted = false

    list.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_canAccumulateValuesCorrectly() {
    val list = listOf(1, 2, 3)
    var sum = 0

    list.safeForEach { sum += it }

    assertThat(sum).isEqualTo(6)
  }

  @Test
  fun testSafeForEach_withSet_executesActionForEachElement() {
    val set = setOf("a", "b", "c")
    val collected = mutableListOf<String>()

    set.safeForEach { collected.add(it) }

    assertThat(collected).containsExactlyElementsIn(set)
  }

  @Test
  fun testSafeForEach_withEmptySet_executesNoActions() {
    val set = emptySet<String>()
    var actionExecuted = false

    set.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withCollection_executesActionForEachElement() {
    val collection: Collection<Int> = listOf(5, 10, 15)
    val collected = mutableListOf<Int>()

    collection.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(5, 10, 15).inOrder()
  }

  @Test
  fun testSafeForEach_withIterable_executesActionForEachElement() {
    val iterable: Iterable<String> = listOf("x", "y", "z")
    val collected = mutableListOf<String>()

    iterable.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly("x", "y", "z").inOrder()
  }

  @Test
  fun testSafeForEach_withSingleElement_executesActionOnce() {
    val list = listOf(42)
    var executionCount = 0

    list.safeForEach { executionCount++ }

    assertThat(executionCount).isEqualTo(1)
  }

  @Test
  fun testSafeForEach_withNullElements_handlesNullsCorrectly() {
    val list = listOf("a", null, "c")
    val collected = mutableListOf<String?>()

    list.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly("a", null, "c").inOrder()
  }

  @Test
  fun testSafeForEach_actionCanModifyExternalState() {
    val numbers = listOf(1, 2, 3, 4, 5)
    var evenCount = 0
    var oddCount = 0

    numbers.safeForEach { num ->
      if (num % 2 == 0) evenCount++ else oddCount++
    }

    assertThat(evenCount).isEqualTo(2)
    assertThat(oddCount).isEqualTo(3)
  }
}
