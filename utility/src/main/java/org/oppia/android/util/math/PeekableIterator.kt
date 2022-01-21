package org.oppia.android.util.math

/**
 * An [Iterator] for [Sequence]s that may have exactly up to 1 element lookahead.
 *
 * This iterator is intended to be used by tokenizers and parsers to force an LL(1) implementation
 * of both (since only one token may be observed before retrieval). Further, this implementation is
 * intentionally limited to sequences for potential performance: since no more than one element
 * requires lookahead, having a backed array is unnecessary which means a potentially expensive and
 * fully dynamic sequence could back the iterator. While especially large inputs aren't expected,
 * they are inherently supported by the implementation with no additional overhead.
 *
 * New implementations can be created using [toPeekableIterator].
 *
 * This class is not safe to use across multiple threads and requires synchronization.
 */
class PeekableIterator<T: Any> private constructor(
  private val backingIterator: Iterator<T>
) : Iterator<T> {
  private var next: T? = null
  private var count: Int = 0

  override fun hasNext(): Boolean = next != null || backingIterator.hasNext()

  override fun next(): T = (next?.also { next = null } ?: retrieveNext()).also { count++ }

  /**
   * Returns the next element to be returned by [next] without actually consuming it, or ``null`` if
   * there isn't one (i.e. [hasNext] returns false).
   *
   * It's safe to call this both at the end of the iterator, and multiple times (at any point in the
   * iteration).
   */
  fun peek(): T? {
    return when {
      next != null -> next
      hasNext() -> retrieveNext().also { next = it }
      else -> null
    }
  }

  /**
   * Consumes and returns the next token if it matches the value provided by [expected].
   *
   * This method essentially behaves the same way as [expectNextMatches].
   */
  fun expectNextValue(expected: () -> T): T? = expectNextMatches { it == expected() }

  /**
   * Consumes and returns the next token (if it's available--see [peek]) if it passes the specified
   * [predicate], otherwise ``null`` is returned.
   *
   * Note that a ``nul`` return value isn't sufficient to determine the iterator has ended
   * ([hasNext] or [peek] should be used for that, instead).
   *
   * Note also that [predicate] will only be called once, but no assumption should be made as to
   * when it will be called.
   */
  fun expectNextMatches(predicate: (T) -> Boolean): T? {
    // Only call the predicate if not at the end of the stream, and only call next() if the next
    // value matches.
    return peek()?.takeIf(predicate)?.also { next() }
  }

  /**
   * Returns the number of elements consumed by this iterator so far via [next].
   *
   * At the beginning of iteration, this will return 0. At the end (i.e. when [hasNext] returns
   * false), this will return the size of the underlying sequence/container (depending on if
   * iteration began at the beginning of the sequence--see the caveat in [toPeekableIterator] for
   * specifics).
   */
  fun getRetrievalCount(): Int = count

  private fun retrieveNext(): T = backingIterator.next()

  companion object {
    /**
     * Returns a new [PeekableIterator] for this [Sequence].
     *
     * Note that iteration begins at the current point of the [Sequence] (since sequences may not
     * retain state and can't be rewinded), so care should be taken when using multiple iterators
     * for the same sequence (including when converting the sequence to another structure, like a
     * [List]). Some sequences do support multiple iteration, so the exact behavior of the returned
     * iterator will be sequence implementation dependent.
     */
    fun <T: Any> Sequence<T>.toPeekableIterator(): PeekableIterator<T> =
      PeekableIterator(iterator())
  }
}
