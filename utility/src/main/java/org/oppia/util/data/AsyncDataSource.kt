package org.oppia.util.data

/**
 * Represents a source of data that can be delivered and changed asynchronously.
 *
 * @param <T> The type of data being provided by this data source.
 */
interface AsyncDataSource<T> {
  // TODO(BenHenning): Finalize the interfaces for this API beyond a basic prototype for the initial project intro.

  suspend fun executePendingOperation(): T
}
