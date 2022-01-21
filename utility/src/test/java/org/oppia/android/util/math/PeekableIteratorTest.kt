package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.util.function.Supplier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.stubbing.Answer
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator
import org.robolectric.annotation.LooperMode

/** Tests for [PeekableIterator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PeekableIteratorTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockSequenceSupplier: Supplier<String?>

  @Test
  fun testHasNext_emptySequence_returnsFalse() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val hasNext = iterator.hasNext()

    assertThat(hasNext).isFalse()
  }

  @Test
  fun testHasNext_singletonSequence_returnsTrue() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val hasNext = iterator.hasNext()

    assertThat(hasNext).isTrue()
  }

  @Test
  fun testNext_emptySequence_throwsException() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    assertThrows(NoSuchElementException::class) { iterator.next()  }
  }

  @Test
  fun testNext_singletonSequence_returnsValue() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val value = iterator.next()

    assertThat(value).isEqualTo("element")
  }

  @Test
  fun testNext_multipleCalls_multiElementSequence_returnsAllValues() {
    val sequence = sequenceOf("first", "second", "third")
    val iterator = sequence.toPeekableIterator()

    val value1 = iterator.next()
    val value2 = iterator.next()
    val value3 = iterator.next()

    assertThat(value1).isEqualTo("first")
    assertThat(value2).isEqualTo("second")
    assertThat(value3).isEqualTo("third")
  }

  @Test
  fun testHasNext_singletonSequence_afterNext_returnsFalse() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.next()

    val hasNext = iterator.hasNext()

    assertThat(hasNext).isFalse()
  }

  @Test
  fun testHasNext_multiElementSequence_afterNext_returnsTrue() {
    val sequence = sequenceOf("first", "second", "third")
    val iterator = sequence.toPeekableIterator()
    iterator.next()

    val hasNext = iterator.hasNext()

    assertThat(hasNext).isTrue()
  }

  @Test
  fun testAsIterator_emptySequence_convertsToEmptyList() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val list = iterator.toList()

    assertThat(list).isEmpty()
  }

  @Test
  fun testAsIterator_singletonSequence_convertsToSingletonList() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val list = iterator.toList()

    assertThat(list).containsExactly("element")
  }

  @Test
  fun testAsIterator_multiElementSequence_convertsToList() {
    val sequence = sequenceOf("first", "second", "third")
    val iterator = sequence.toPeekableIterator()

    val list = iterator.toList()

    assertThat(list).containsExactly("first", "second", "third").inOrder()
  }

  @Test
  fun testHasNext_multiElementSequence_convertedToList_returnsFalse() {
    val sequence = sequenceOf("first", "second", "third")
    val iterator = sequence.toPeekableIterator()
    iterator.toList()

    val hasNext = iterator.hasNext()

    // No elements remain after converting the iterator to a list (since it should be fully
    // consumed).
    assertThat(hasNext).isFalse()
  }

  @Test
  fun testPeek_emptySequence_returnsNull() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val nextElement = iterator.peek()

    assertThat(nextElement).isNull()
  }

  @Test
  fun testPeek_emptySequence_twice_returnsNull() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()
    iterator.peek()

    // Peek a second time.
    val nextElement = iterator.peek()

    assertThat(nextElement).isNull()
  }

  @Test
  fun testPeek_singletonSequence_returnsElement() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val nextElement = iterator.peek()

    assertThat(nextElement).isEqualTo("element")
  }

  @Test
  fun testPeek_singletonSequence_twice_returnsElement() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.peek()

    // Peek a second time.
    val nextElement = iterator.peek()

    assertThat(nextElement).isEqualTo("element")
  }

  @Test
  fun testPeek_singletonSequence_afterNext_returnsNull() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.next()

    val nextElement = iterator.peek()

    // There is no longer a next element since it was consumed.
    assertThat(nextElement).isNull()
  }

  @Test
  fun testPeek_singletonSequence_afterNext_twice_returnsNull() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.next()
    iterator.peek()

    // Peek a second time after consuming the element.
    val nextElement = iterator.peek()

    // It's still missing.
    assertThat(nextElement).isNull()
  }

  @Test
  fun testPeek_singletonSequence_peekThenNext_returnsElementFromBoth() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val nextElement = iterator.peek()
    val consumedElement = iterator.next()

    // Both functions should return the same value in this order.
    assertThat(nextElement).isEqualTo("element")
    assertThat(consumedElement).isEqualTo("element")
  }

  @Test
  fun testExpectNextValue_emptySequence_returnsNull() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextValue { "does not match" }

    // No values to match.
    assertThat(matchedValue).isNull()
  }

  @Test
  fun testExpectNextValue_valueMatches_returnsValue() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextValue { "matches" }

    assertThat(matchedValue).isEqualTo("matches")
  }

  @Test
  fun testHasNext_afterExpectNextValue_valueMatches_returnsFalse() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "matches" }

    val hasNext = iterator.hasNext()

    // No other elements since the only one was consumed.
    assertThat(hasNext).isFalse()
  }

  @Test
  fun testPeek_afterExpectNextValue_valueMatches_returnsNull() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "matches" }

    val nextElement = iterator.peek()

    // No other elements since the only one was consumed.
    assertThat(nextElement).isNull()
  }

  @Test
  fun testExpectNextValue_valueDoesNotMatch_returnsNull() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextValue { "does not match" }

    // No values to match.
    assertThat(matchedValue).isNull()
  }

  @Test
  fun testHasNext_afterExpectNextValue_valueDoesNotMatch_returnsTrue() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "does not match" }

    val hasNext = iterator.hasNext()

    // The element is still present.
    assertThat(hasNext).isTrue()
  }

  @Test
  fun testPeek_afterExpectNextValue_valueDoesNotMatch_returnsElement() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "does not match" }

    val nextElement = iterator.next()

    // The element is still present.
    assertThat(nextElement).isEqualTo("matches")
  }

  @Test
  fun testExpectNextMatches_emptySequence_returnsNull() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextMatches { true }

    // No values to match.
    assertThat(matchedValue).isNull()
  }

  @Test
  fun testExpectNextMatches_valueMatches_returnsValue() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextMatches { true }

    assertThat(matchedValue).isEqualTo("matches")
  }

  @Test
  fun testHasNext_afterExpectNextMatches_valueMatches_returnsFalse() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { true }

    val hasNext = iterator.hasNext()

    // No other elements since the only one was consumed.
    assertThat(hasNext).isFalse()
  }

  @Test
  fun testPeek_afterExpectNextMatches_valueMatches_returnsNull() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { true }

    val nextElement = iterator.peek()

    // No other elements since the only one was consumed.
    assertThat(nextElement).isNull()
  }

  @Test
  fun testExpectNextMatches_valueDoesNotMatch_returnsNull() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()

    val matchedValue = iterator.expectNextMatches { false }

    // The predicate didn't match.
    assertThat(matchedValue).isNull()
  }

  @Test
  fun testHasNext_afterExpectNextMatches_valueDoesNotMatch_returnsTrue() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { false }

    val hasNext = iterator.hasNext()

    // The element is still present.
    assertThat(hasNext).isTrue()
  }

  @Test
  fun testPeek_afterExpectNextMatches_valueDoesNotMatch_returnsElement() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { false }

    val nextElement = iterator.peek()

    // The element is still present.
    assertThat(nextElement).isEqualTo("matches")
  }

  @Test
  fun testGetRetrievalCount_emptySequence_returnsZero() {
    val sequence = sequenceOf<String>()
    val iterator = sequence.toPeekableIterator()

    val retrievalCount = iterator.getRetrievalCount()

    assertThat(retrievalCount).isEqualTo(0)
  }

  @Test
  fun testGetRetrievalCount_singletonSequence_returnsZero() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()

    val retrievalCount = iterator.getRetrievalCount()

    assertThat(retrievalCount).isEqualTo(0)
  }

  @Test
  fun testGetRetrievalCount_afterNext_singletonSequence_returnsOne() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.next()

    val retrievalCount = iterator.getRetrievalCount()

    // One element was removed.
    assertThat(retrievalCount).isEqualTo(1)
  }

  @Test
  fun testGetRetrievalCount_afterPeek_singletonSequence_returnsZero() {
    val sequence = sequenceOf("element")
    val iterator = sequence.toPeekableIterator()
    iterator.peek()

    val retrievalCount = iterator.getRetrievalCount()

    // Peek does not remove the element.
    assertThat(retrievalCount).isEqualTo(0)
  }

  @Test
  fun testGetRetrievalCount_afterMatchingExpectNextValue_returnsOne() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "matches" }

    val retrievalCount = iterator.getRetrievalCount()

    // One element was removed due to the match.
    assertThat(retrievalCount).isEqualTo(1)
  }

  @Test
  fun testGetRetrievalCount_afterFailingExpectNextValue_returnsZero() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextValue { "does not match" }

    val retrievalCount = iterator.getRetrievalCount()

    // No elements were removed since nothing matched.
    assertThat(retrievalCount).isEqualTo(0)
  }

  @Test
  fun testGetRetrievalCount_afterMatchingExpectNextMatches_returnsOne() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { true }

    val retrievalCount = iterator.getRetrievalCount()

    // One element was removed due to the match.
    assertThat(retrievalCount).isEqualTo(1)
  }

  @Test
  fun testGetRetrievalCount_afterFailingExpectNextMatches_returnsZero() {
    val sequence = sequenceOf("matches")
    val iterator = sequence.toPeekableIterator()
    iterator.expectNextMatches { false }

    val retrievalCount = iterator.getRetrievalCount()

    // No elements were removed since nothing matched.
    assertThat(retrievalCount).isEqualTo(0)
  }

  @Test
  fun testGetRetrievalCount_afterMultipleNext_returnsNextCount() {
    val sequence = sequenceOf("first", "second", "third")
    val iterator = sequence.toPeekableIterator()
    // Call next() twice.
    iterator.next()
    iterator.next()

    val retrievalCount = iterator.getRetrievalCount()

    // The number of consumed elements from the iterator should be returned.
    assertThat(retrievalCount).isEqualTo(2)
  }

  @Test
  fun testGetRetrievalCount_afterConvertingToList_returnsListSize() {
    val sequence = sequenceOf("first", "second", "third", "fourth", "fifth")
    val iterator = sequence.toPeekableIterator()
    val elements = iterator.toList()

    val retrievalCount = iterator.getRetrievalCount()

    // Since the iterator was fully consumed, the retrieval count should be the same as the list
    // size.
    assertThat(retrievalCount).isEqualTo(5)
    assertThat(elements.size).isEqualTo(retrievalCount)
  }

  @Test
  fun testCreateIterator_doesNotConsumeElementsFromSequence() {
    val generatedSequence = createGeneratingSequence()

    generatedSequence.toPeekableIterator()

    // The sequence is never called just upon iterator creation.
    verifyNoMoreInteractions(mockSequenceSupplier)
  }

  @Test
  fun testPeek_consumesElementFromSequence() {
    val iterator = createGeneratingSequence().toPeekableIterator()

    iterator.peek()

    // The first peek must consume one element in order to populate it.
    verify(mockSequenceSupplier).get()
  }

  @Test
  fun testPeek_twice_doesNotConsumeAdditionalElementFromSequence() {
    val iterator = createGeneratingSequence().toPeekableIterator()
    iterator.peek()
    reset(mockSequenceSupplier)

    // Peek a second time.
    iterator.peek()

    // The second peek doesn't consume an element (since the iterator's contract is to never look
    // more than 1 element ahead).
    verifyNoMoreInteractions(mockSequenceSupplier)
  }

  @Test
  fun testNext_consumesOneElementFromSequence() {
    val iterator = createGeneratingSequence().toPeekableIterator()

    iterator.next()

    // The sequence should have only one value retrieved due to the next() call.
    verify(mockSequenceSupplier).get()
  }

  @Test
  fun testNext_twice_consumesTwoElementsFromSequence() {
    val iterator = createGeneratingSequence().toPeekableIterator()

    // Iterate two items.
    iterator.next()
    iterator.next()

    // One value should be retrieved from the sequence for each next() call.
    verify(mockSequenceSupplier, times(2)).get()
  }

  @Test
  fun testConvertToList_consumesAllElementsFromSequence() {
    val generatedSequence = createGeneratingSequence()
    val iterator = generatedSequence.toPeekableIterator()

    val list = iterator.toList()

    // The whole sequence should be consumed through the iterator when converting it to a list. Note
    // the extra call to get() is for the final element that indicates the sequence has ended per
    // generateSequence.
    verify(mockSequenceSupplier, times(list.size + 1)).get()
    assertThat(list).isNotEmpty()
  }

  private fun createGeneratingSequence(): Sequence<String> {
    `when`(mockSequenceSupplier.get()).thenReturn("string0", "string1", "string2", "string3", null)
    return generateSequence { mockSequenceSupplier.get() }
  }

  private companion object {
    /**
     * Returns a [List] that contains all elements from the [Iterator] (i.e. the iterator is fully
     * consumed).
     */
    private fun <T> Iterator<T>.toList(): List<T> {
      return mutableListOf<T>().apply {
        this@toList.forEach(this::add)
      }
    }
  }
}
