package org.oppia.android.app.player.state.hintsandsolution

import dagger.Binds
import dagger.Module

/** Provides debug implementation of [HintHandler] */
@Module
interface HintHandlerDebugModule {
  @Binds
  fun bindsHintHandler(debugHintHandler: DebugHintHandler): HintHandler
}
