package org.oppia.android.domain.onboarding

import dagger.Binds
import dagger.Module

/** Module for providing a real [ExpirationMetaDataRetriever] implementation. */
@Module
interface ExpirationMetaDataRetrieverModule {
  @Binds
  fun bindExpirationMetadataRetriever(
    impl: ExpirationMetaDataRetrieverImpl
  ): ExpirationMetaDataRetriever
}
