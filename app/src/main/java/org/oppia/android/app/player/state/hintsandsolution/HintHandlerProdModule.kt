package org.oppia.android.app.player.state.hintsandsolution

import dagger.Binds
import dagger.Module

@Module
interface HintHandlerProdModule {
  @Binds
  fun bindsHintHandler(prodHintHandler: ProdHintHandler): HintHandler
}
