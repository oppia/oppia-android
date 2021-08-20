package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Production module for providing hints & solution related dependencies. */
@Module
interface HintsAndSolutionProdModule {
  @Binds
  fun provideHintHandlerFactoryImpl(impl: HintHandlerImpl.FactoryImpl): HintHandler.Factory
}
