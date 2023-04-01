package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.TestMessage
import org.robolectric.annotation.LooperMode

/** Tests for [Comparator] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ComparatorExtensionsTest {
  private companion object {
    private val TEST_MESSAGE_0 = TestMessage.newBuilder().apply { intValue = 0 }.build()
    private val TEST_MESSAGE_1 = TestMessage.newBuilder().apply { intValue = 1 }.build()
    private val TEST_MESSAGE_2 = TestMessage.newBuilder().apply { intValue = 2 }.build()
  }

  private val stringComparator: Comparator<String> by lazy {
    Comparator { o1, o2 -> o1.compareTo(o2) }
  }
  private val protoComparator: Comparator<TestMessage> by lazy {
    compareBy(TestMessage::getIntValue)
  }

  @Test
  fun testCompareIterables_emptyList_emptyList_returnsZero() {
    val leftList = listOf<String>()
    val rightList = listOf<String>()

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterables_singletonList_emptyList_returnsOne() {
    val leftList = listOf("1")
    val rightList = listOf<String>()

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_emptyList_singletonList_returnsNegativeOne() {
    val leftList = listOf<String>()
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_singletonList_singletonList_sameElems_returnsZero() {
    val leftList = listOf("1")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterables_twoItemList_singletonList_commonElem_returnsOne() {
    val leftList = listOf("1", "2")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The first list is larger, therefore "greater".
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_singletonList_twoItemList_commonElem_returnsNegativeOne() {
    val leftList = listOf("1")
    val rightList = listOf("1", "2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_equalSizeLists_sameItems_sameOrder_returnsZero() {
    val leftList = listOf("1", "2")
    val rightList = listOf("1", "2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterables_equalSizeLists_sameItems_differentOrder_returnsZero() {
    val leftList = listOf("1", "2")
    val rightList = listOf("2", "1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // Order shouldn't matter.
    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterables_list223_list123_returnsOne() {
    val leftList = listOf("2", "2", "3")
    val rightList = listOf("1", "2", "3")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The first element is larger in the left list, so it's "greater".
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_list123_list223_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("2", "2", "3")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_list123_list11_returnsOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1", "1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The second item is bigger in the first list, so it's "greater".
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_list123_list13_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1", "3")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The second item is bigger in the second list, so the first one is "lesser".
    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_list223_list1_returnsOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_list123_list2_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_list22_list2_returnsOne() {
    val leftList = listOf("2", "2")
    val rightList = listOf("2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The first list has an extra element. This also verifies that duplicates are correctly
    // considered during comparison.
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterables_list2_list22_returnsNegativeOne() {
    val leftList = listOf("2")
    val rightList = listOf("2", "2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    // The second list has an extra element.
    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterables_list22_list22_returnsZero() {
    val leftList = listOf("2", "2")
    val rightList = listOf("2", "2")

    val compareResult = stringComparator.compareIterables(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterablesReversed_emptyList_emptyList_returnsZero() {
    val leftList = listOf<String>()
    val rightList = listOf<String>()

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterablesReversed_singletonList_emptyList_returnsNegativeOne() {
    val leftList = listOf("1")
    val rightList = listOf<String>()

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_emptyList_singletonList_returnsOne() {
    val leftList = listOf<String>()
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterablesReversed_singletonList_singletonList_sameElems_returnsZero() {
    val leftList = listOf("1")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterablesReversed_twoItemList_singletonList_commonElem_returnsNegativeOne() {
    val leftList = listOf("1", "2")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_singletonList_twoItemList_commonElem_returnsOne() {
    val leftList = listOf("1")
    val rightList = listOf("1", "2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterablesReversed_equalSizeLists_sameItems_sameOrder_returnsZero() {
    val leftList = listOf("1", "2")
    val rightList = listOf("1", "2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterablesReversed_equalSizeLists_sameItems_differentOrder_returnsZero() {
    val leftList = listOf("1", "2")
    val rightList = listOf("2", "1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    // Order shouldn't matter.
    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareIterablesReversed_list223_list123_returnsNegativeOne() {
    val leftList = listOf("2", "2", "3")
    val rightList = listOf("1", "2", "3")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list123_list223_returnsOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("2", "2", "3")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterablesReversed_list123_list11_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1", "1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list123_list13_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1", "3")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list223_list1_returnsNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("1")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list123_list2_returnsNegativeNegativeOne() {
    val leftList = listOf("1", "2", "3")
    val rightList = listOf("2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list22_list2_returnsNegativeOne() {
    val leftList = listOf("2", "2")
    val rightList = listOf("2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    // The first list has an extra element. This also verifies that duplicates are correctly
    // considered during comparison.
    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareIterablesReversed_list2_list22_returnsOne() {
    val leftList = listOf("2")
    val rightList = listOf("2", "2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    // The second list has an extra element.
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareIterablesReversed_list22_list22_returnsZero() {
    val leftList = listOf("2", "2")
    val rightList = listOf("2", "2")

    val compareResult = stringComparator.compareIterablesReversed(leftList, rightList)

    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareProtos_defaultAndDefault_returnsZero() {
    val leftProto = TestMessage.newBuilder().build()
    val rightProto = TestMessage.newBuilder().build()

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // Two default instances are equal.
    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareProtos_nonDefaultZeroAndDefault_returnsZero() {
    val leftProto = TEST_MESSAGE_0
    val rightProto = TestMessage.newBuilder().build()

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // Even though the left proto is defined, the value of 0 for its int field makes it the same as
    // a default (per proto3 spec).
    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareProtos_nonDefaultAndDefault_returnsOne() {
    val leftProto = TEST_MESSAGE_1
    val rightProto = TestMessage.newBuilder().build()

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // The left proto is actually defined.
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareProtos_defaultAndNonDefault_returnsNegativeOne() {
    val leftProto = TestMessage.newBuilder().build()
    val rightProto = TEST_MESSAGE_1

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // The right proto is actually defined.
    assertThat(compareResult).isEqualTo(-1)
  }

  @Test
  fun testCompareProtos_twoNonDefaults_sameProtoValues_returnsZero() {
    val leftProto = TEST_MESSAGE_1
    val rightProto = TEST_MESSAGE_1

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // The protos are equal per protoComparator.
    assertThat(compareResult).isEqualTo(0)
  }

  @Test
  fun testCompareProtos_twoNonDefaults_leftIsLarger_returnsOne() {
    val leftProto = TEST_MESSAGE_2
    val rightProto = TEST_MESSAGE_1

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // The left proto is larger per protoComparator.
    assertThat(compareResult).isEqualTo(1)
  }

  @Test
  fun testCompareProtos_twoNonDefaults_leftIsSmaller_returnsNegativeOne() {
    val leftProto = TEST_MESSAGE_1
    val rightProto = TEST_MESSAGE_2

    val compareResult = protoComparator.compareProtos(leftProto, rightProto)

    // The right proto is larger per protoComparator.
    assertThat(compareResult).isEqualTo(-1)
  }
}
