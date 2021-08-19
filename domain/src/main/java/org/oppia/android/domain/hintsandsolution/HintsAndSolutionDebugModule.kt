package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module

/** Module for providing hints & solution related dependencies in debug build of the app. */
@Module
interface HintsAndSolutionDebugModule {
  @Binds
  fun provideHintHandlerFactoryImpl(
    impl: HintHandlerDebugImpl.FactoryDebugImpl
  ): HintHandler.Factory
}
