package org.oppia.android.util.math

class PeekableIterator<T : Any>(private val backingIterator: Iterator<T>) : Iterator<T> {
  private var next: T? = null
  private var count: Int = 0

  override fun hasNext(): Boolean = next != null || backingIterator.hasNext()

  override fun next(): T = next?.also {
    next = null
    count++
  } ?: retrieveNext()

  fun peek(): T? {
    return when {
      next != null -> next
      hasNext() -> retrieveNext().also { next = it }
      else -> null
    }
  }

  fun expectNextValue(expected: () -> T): T? = expectNextMatches { it == expected() }

  fun expectNextMatches(predicate: (T) -> Boolean): T? {
    // Only call the predicate if not at the end of the stream, and only call next() if the next
    // value matches.
    return peek()?.takeIf(predicate)?.also { next() }
  }

  fun getRetrievalCount(): Int = count

  private fun retrieveNext(): T = backingIterator.next()

  companion object {
    fun <T : Any> fromSequence(sequence: Sequence<T>): PeekableIterator<T> =
      PeekableIterator(sequence.iterator())
  }
}
