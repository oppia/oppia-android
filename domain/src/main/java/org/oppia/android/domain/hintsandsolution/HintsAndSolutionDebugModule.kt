package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Module for providing debug-only hints & solution related dependencies. */
@Module
interface HintsAndSolutionDebugModule {
  @Binds
  fun provideHintHandlerFactoryImpl(
    impl: HintHandlerDebugImpl.FactoryDebugImpl
  ): HintHandler.Factory
}
