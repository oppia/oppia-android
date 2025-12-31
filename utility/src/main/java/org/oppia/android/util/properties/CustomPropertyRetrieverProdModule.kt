package org.oppia.android.util.properties

import dagger.Binds
import dagger.Module

@Module
interface CustomPropertyRetrieverProdModule {
  @Binds
  fun bindPropertyRetriever(impl: CustomPropertyRetrieverProdImpl): CustomPropertyRetriever
}
