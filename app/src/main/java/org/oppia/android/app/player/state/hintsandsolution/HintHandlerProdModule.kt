package org.oppia.android.app.player.state.hintsandsolution

import dagger.Binds
import dagger.Module

/** Provides production implementation of [HintHandler] */
@Module
interface HintHandlerProdModule {
  @Binds
  fun bindsHintHandler(hintHandlerProdImpl: HintHandlerProdImpl): HintHandler
}
