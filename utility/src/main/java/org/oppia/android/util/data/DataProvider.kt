package org.oppia.android.util.data

/**
 * Represents a provider of data that can be delivered and changed asynchronously.
 *
 * @param <T> The type of data being provided.
 */
interface DataProvider<T> {
  // TODO(#6): Finalize the interfaces for this API beyond a basic prototype for the initial project intro.

  /**
   * Returns a unique identifier that corresponds to this data provider. This should be a trivially copyable and
   * immutable object. This ID is used to determine which data provider subscribers should be notified of changes to the
   * data.
   */
  fun getId(): Any

  /**
   * Returns the latest copy of data available by the provider, potentially performing a blocking call in order to
   * retrieve the data. It's up to the implementation to decide how caching should work. Implementations should remain
   * agnostic of the specific subscribers associated with them (ie, they should not perform logic corresponding to a
   * particular subscription since this can be highly error-prone when considering that subscribers may be bound to
   * Android UI component lifecycles).
   */
  suspend fun retrieveData(): AsyncResult<T>
}
