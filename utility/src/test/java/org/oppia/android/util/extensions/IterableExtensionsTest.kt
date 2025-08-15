package org.oppia.android.util.extensions

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
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
  fun testSafeForEach_withSequence_executesActionForEachElement() {
    val sequence = sequenceOf(100, 200, 300)
    val collected = mutableListOf<Int>()

    sequence.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(100, 200, 300).inOrder()
  }

  @Test
  fun testSafeForEach_withEmptySequence_executesNoActions() {
    val sequence = emptySequence<Int>()
    var actionExecuted = false

    sequence.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withArray_executesActionForEachElement() {
    val array = arrayOf("apple", "banana", "cherry")
    val collected = mutableListOf<String>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly("apple", "banana", "cherry").inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyArray_executesNoActions() {
    val array = emptyArray<String>()
    var actionExecuted = false

    array.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withMap_executesActionForEachEntry() {
    val map = mapOf("key1" to "value1", "key2" to "value2")
    val collectedKeys = mutableListOf<String>()
    val collectedValues = mutableListOf<String>()

    map.safeForEach { entry ->
      collectedKeys.add(entry.key)
      collectedValues.add(entry.value)
    }

    assertThat(collectedKeys).containsExactlyElementsIn(map.keys)
    assertThat(collectedValues).containsExactlyElementsIn(map.values)
  }

  @Test
  fun testSafeForEach_withEmptyMap_executesNoActions() {
    val map = emptyMap<String, String>()
    var actionExecuted = false

    map.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withByteArray_executesActionForEachElement() {
    val array = byteArrayOf(1, 2, 3)
    val collected = mutableListOf<Byte>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(1.toByte(), 2.toByte(), 3.toByte()).inOrder()
  }

  @Test
  fun testSafeForEach_withShortArray_executesActionForEachElement() {
    val array = shortArrayOf(10, 20, 30)
    val collected = mutableListOf<Short>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(10.toShort(), 20.toShort(), 30.toShort()).inOrder()
  }

  @Test
  fun testSafeForEach_withIntArray_executesActionForEachElement() {
    val array = intArrayOf(100, 200, 300)
    val collected = mutableListOf<Int>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(100, 200, 300).inOrder()
  }

  @Test
  fun testSafeForEach_withLongArray_executesActionForEachElement() {
    val array = longArrayOf(1000L, 2000L, 3000L)
    val collected = mutableListOf<Long>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(1000L, 2000L, 3000L).inOrder()
  }

  @Test
  fun testSafeForEach_withFloatArray_executesActionForEachElement() {
    val array = floatArrayOf(1.1f, 2.2f, 3.3f)
    val collected = mutableListOf<Float>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(1.1f, 2.2f, 3.3f).inOrder()
  }

  @Test
  fun testSafeForEach_withDoubleArray_executesActionForEachElement() {
    val array = doubleArrayOf(1.11, 2.22, 3.33)
    val collected = mutableListOf<Double>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(1.11, 2.22, 3.33).inOrder()
  }

  @Test
  fun testSafeForEach_withBooleanArray_executesActionForEachElement() {
    val array = booleanArrayOf(true, false, true)
    val collected = mutableListOf<Boolean>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(true, false, true).inOrder()
  }

  @Test
  fun testSafeForEach_withCharArray_executesActionForEachElement() {
    val array = charArrayOf('a', 'b', 'c')
    val collected = mutableListOf<Char>()

    array.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly('a', 'b', 'c').inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyPrimitiveArrays_executesNoActions() {
    var actionExecuted = false

    byteArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    shortArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    intArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    longArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    floatArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    doubleArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    booleanArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()

    charArrayOf().safeForEach { actionExecuted = true }
    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withString_executesActionForEachCharacter() {
    val string = "hello"
    val collected = mutableListOf<Char>()

    string.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly('h', 'e', 'l', 'l', 'o').inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyString_executesNoActions() {
    val string = ""
    var actionExecuted = false

    string.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withMenu_executesActionForEachMenuItem() {
    val menu = mock(Menu::class.java)
    val menuItem1 = mock(MenuItem::class.java)
    val menuItem2 = mock(MenuItem::class.java)

    `when`(menu.size()).thenReturn(2)
    `when`(menu.getItem(0)).thenReturn(menuItem1)
    `when`(menu.getItem(1)).thenReturn(menuItem2)

    val collected = mutableListOf<MenuItem>()

    menu.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(menuItem1, menuItem2).inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyMenu_executesNoActions() {
    val menu = mock(Menu::class.java)
    `when`(menu.size()).thenReturn(0)

    var actionExecuted = false

    menu.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
  }

  @Test
  fun testSafeForEach_withViewGroup_executesActionForEachChild() {
    val viewGroup = mock(ViewGroup::class.java)
    val view1 = mock(View::class.java)
    val view2 = mock(View::class.java)
    val view3 = mock(View::class.java)

    `when`(viewGroup.childCount).thenReturn(3)
    `when`(viewGroup.getChildAt(0)).thenReturn(view1)
    `when`(viewGroup.getChildAt(1)).thenReturn(view2)
    `when`(viewGroup.getChildAt(2)).thenReturn(view3)

    val collected = mutableListOf<View>()

    viewGroup.safeForEach { collected.add(it) }

    assertThat(collected).containsExactly(view1, view2, view3).inOrder()
  }

  @Test
  fun testSafeForEach_withEmptyViewGroup_executesNoActions() {
    val viewGroup = mock(ViewGroup::class.java)
    `when`(viewGroup.childCount).thenReturn(0)

    var actionExecuted = false

    viewGroup.safeForEach { actionExecuted = true }

    assertThat(actionExecuted).isFalse()
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
