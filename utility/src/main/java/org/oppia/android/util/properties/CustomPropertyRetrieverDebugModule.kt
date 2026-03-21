package org.oppia.android.util.properties

import dagger.Binds
import dagger.Module

/** Provides the debug-specific implementation of [CustomPropertyRetriever]. */
@Module
interface CustomPropertyRetrieverDebugModule {
  @Binds
  fun bindPropertyRetriever(impl: CustomPropertyRetrieverDebugImpl): CustomPropertyRetriever
}
