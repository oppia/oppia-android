package org.oppia.android.util.properties

import dagger.Binds
import dagger.Module

/** Provides the production-specific implementation of [CustomPropertyRetriever]. */
@Module
interface CustomPropertyRetrieverProdModule {
  @Binds
  fun bindPropertyRetriever(impl: CustomPropertyRetrieverProdImpl): CustomPropertyRetriever
}
