package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener

/**
 * Module to provide interaction view model-specific dependencies for intreactions that should be explicitly displayed
 * to the user.
 */
@Module
class InteractionViewModelModule {
  companion object {
    val splitScreenInteractionIdsPool = listOf("DragAndDropSortInput", "ImageClickInput")
  }

  // TODO(#300): Use a common source for these interaction IDs to de-duplicate them from other places in the codebase
  //  where they are referenced.
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(fragment: Fragment): InteractionViewModelFactory {
    return { _, _, interactionAnswerReceiver, _, hasPreviousButton, isSplitView ->
      ContinueInteractionViewModel(
        interactionAnswerReceiver,
        hasPreviousButton,
        fragment as PreviousNavigationButtonListener,
        isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _, isSplitView ->
      SelectionInteractionViewModel(
        entityId,
        interaction,
        interactionAnswerReceiver,
        interactionAnswerErrorReceiver,
        isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _, isSplitView ->
      SelectionInteractionViewModel(
        entityId,
        interaction,
        interactionAnswerReceiver,
        interactionAnswerErrorReceiver,
        isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, interaction, _, interactionAnswerErrorReceiver, _, isSplitView ->
      FractionInteractionViewModel(
        interaction,
        context,
        interactionAnswerErrorReceiver,
        isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, _, _, interactionAnswerErrorReceiver, _, isSplitView ->
      NumericInputViewModel(context, interactionAnswerErrorReceiver, isSplitView)
    }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(): InteractionViewModelFactory {
    return { _, interaction, _, interactionAnswerErrorReceiver, _, isSplitView ->
      TextInputViewModel(
        interaction, interactionAnswerErrorReceiver, isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, interaction, _, interactionAnswerErrorReceiver, _, isSplitView ->
      DragAndDropSortInteractionViewModel(
        entityId, interaction, interactionAnswerErrorReceiver, isSplitView
      )
    }
  }
}
