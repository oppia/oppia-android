package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Module for providing production-only hints & solution related dependencies. */
@Module
interface HintsAndSolutionProdModule {
  @Binds
  fun provideHintHandlerFactoryImpl(impl: HintHandlerProdImpl.FactoryProdImpl): HintHandler.Factory
}
