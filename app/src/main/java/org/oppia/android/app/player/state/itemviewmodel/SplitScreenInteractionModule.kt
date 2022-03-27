package org.oppia.android.app.player.state.itemviewmodel

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/** Module to define which interactions support split-screen versions. */
@Module
class SplitScreenInteractionModule {
  @Provides
  @IntoSet
  @SplitScreenInteractionIds
  fun provideDragAndDropSortInputSplitScreenSupportIndication(): String = "DragAndDropSortInput"

  @Provides
  @IntoSet
  @SplitScreenInteractionIds
  fun provideImageClickInputSplitScreenSupportIndication(): String = "ImageClickInput"
}
