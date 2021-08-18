package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Module for providing hints & solution related dependencies. */
@Module
interface HintsAndSolutionModule {
  @Binds
  fun provideHintHandlerFactoryImpl(impl: HintHandlerImpl.FactoryImpl): HintHandler.Factory
}
