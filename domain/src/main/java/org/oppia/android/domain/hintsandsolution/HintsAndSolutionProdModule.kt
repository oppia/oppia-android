package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module
import kotlinx.coroutines.ObsoleteCoroutinesApi

/** Module for providing production-only hints & solution related dependencies. */
@Module
interface HintsAndSolutionProdModule {
  @Binds
  @ObsoleteCoroutinesApi
  fun provideHintHandlerFactoryImpl(impl: HintHandlerProdImpl.FactoryProdImpl): HintHandler.Factory
}
