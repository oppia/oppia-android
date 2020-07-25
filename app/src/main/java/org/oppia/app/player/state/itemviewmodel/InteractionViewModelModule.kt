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
  // TODO(#300): Use a common source for these interaction IDs to de-duplicate them from other places in the codebase
  //  where they are referenced.
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(fragment: Fragment): InteractionViewModelFactory {
    return { _, hasConversationView, _,interactionAnswerReceiver, _, hasPreviousButton ->
      ContinueInteractionViewModel(
        interactionAnswerReceiver, hasConversationView, hasPreviousButton, fragment as PreviousNavigationButtonListener
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _ ->
      SelectionInteractionViewModel(
        entityId, hasConversationView , interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _ ->
      SelectionInteractionViewModel(
        entityId, hasConversationView, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _ ->
      FractionInteractionViewModel(interaction, context, hasConversationView, interactionAnswerErrorReceiver)
    }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, hasConversationView, _, _, interactionAnswerErrorReceiver, _ ->
      NumericInputViewModel(context, hasConversationView, interactionAnswerErrorReceiver)
    }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _ ->
      TextInputViewModel(
        interaction, hasConversationView, interactionAnswerErrorReceiver
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _ ->
      DragAndDropSortInteractionViewModel(
        entityId, hasConversationView, interaction, interactionAnswerErrorReceiver
      )
    }
  }
}
