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
}
