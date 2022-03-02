package org.oppia.android.domain.hintsandsolution

import dagger.Binds
import dagger.Module
import kotlinx.coroutines.ObsoleteCoroutinesApi

/** Module for providing debug-only hints & solution related dependencies. */
@Module
interface HintsAndSolutionDebugModule {
  @Binds
  @ObsoleteCoroutinesApi
  fun provideHintHandlerFactoryImpl(
    impl: HintHandlerDebugImpl.FactoryDebugImpl
  ): HintHandler.Factory
}
