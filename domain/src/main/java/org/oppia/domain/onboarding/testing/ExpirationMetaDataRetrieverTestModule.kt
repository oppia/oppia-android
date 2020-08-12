package org.oppia.domain.onboarding.testing

import org.oppia.domain.onboarding.ExpirationMetaDataRetriever
import dagger.Binds
import dagger.Module

/** Module for providing a fake [ExpirationMetaDataRetriever] implementation. */
@Module
interface ExpirationMetaDataRetrieverTestModule {
  @Binds
  fun bindExpirationMetadataRetriever(
    impl: FakeExpirationMetaDataRetriever
  ): ExpirationMetaDataRetriever
}
