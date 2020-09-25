package org.oppia.android.domain.onboarding.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetriever

/** Module for providing a fake [ExpirationMetaDataRetriever] implementation. */
@Module
interface ExpirationMetaDataRetrieverTestModule {
  @Binds
  fun bindExpirationMetadataRetriever(
    impl: FakeExpirationMetaDataRetriever
  ): ExpirationMetaDataRetriever
}
