package org.oppia.domain.onboarding.testing

import dagger.Binds
import dagger.Module
import org.oppia.domain.onboarding.ExpirationMetaDataRetriever

/** Module for providing a fake [ExpirationMetaDataRetriever] implementation. */
@Module
interface ExpirationMetaDataRetrieverTestModule {
  @Binds
  fun bindExpirationMetadataRetriever(
    impl: FakeExpirationMetaDataRetriever
  ): ExpirationMetaDataRetriever
}
