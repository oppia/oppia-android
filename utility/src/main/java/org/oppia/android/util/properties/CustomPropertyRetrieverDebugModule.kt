package org.oppia.android.util.properties

import dagger.Binds
import dagger.Module

@Module
interface CustomPropertyRetrieverDebugModule {
  @Binds
  fun bindPropertyRetriever(impl: CustomPropertyRetrieverDebugImpl): CustomPropertyRetriever
}
