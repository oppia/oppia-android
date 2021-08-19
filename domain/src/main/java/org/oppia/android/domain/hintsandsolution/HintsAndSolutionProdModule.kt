package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Module for providing hints & solution related dependencies in production build of the app. */
@Module
interface HintsAndSolutionProdModule {
  @Binds
  fun provideHintHandlerFactoryImpl(impl: HintHandlerProdImpl.FactoryImpl): HintHandler.Factory
}
