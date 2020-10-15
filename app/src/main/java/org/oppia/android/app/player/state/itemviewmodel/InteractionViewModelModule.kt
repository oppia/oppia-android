package org.oppia.android.app.player.state.itemviewmodel

import android.content.Context
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener

/**
 * Module to provide interaction view model-specific dependencies for interactions that should be
 * explicitly displayed to the user.
 */
@Module
class InteractionViewModelModule {
  // TODO: move to Dagger graph rather than directly referencing modules.
  companion object {
    val splitScreenInteractionIdsPool = listOf("DragAndDropSortInput", "ImageClickInput")
  }

  // TODO(#300): Use a common source for these interaction IDs to de-duplicate them from
  //  other places in the codebase where they are referenced.
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(fragment: Fragment): InteractionViewModelFactory {
    return { _, _, _, hasConversationView, _, interactionAnswerReceiver, _, hasPreviousButton, isSplitView -> // ktlint-disable max-line-length
      ContinueInteractionViewModel(
        interactionAnswerReceiver,
        hasConversationView,
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
    return { gcsResourceName, gcsEntityType, entityId, hasConversationView, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _, isSplitView -> // ktlint-disable max-line-length
      SelectionInteractionViewModel(
        gcsResourceName,
        gcsEntityType,
        entityId,
        hasConversationView,
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
    return { gcsResourceName, gcsEntityType, entityId, hasConversationView, interaction, interactionAnswerReceiver, interactionAnswerErrorReceiver, _, isSplitView -> // ktlint-disable max-line-length
      SelectionInteractionViewModel(
        gcsResourceName,
        gcsEntityType,
        entityId,
        hasConversationView,
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
    return { _, _, _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _, isSplitView -> // ktlint-disable max-line-length
      FractionInteractionViewModel(
        interaction,
        context,
        hasConversationView,
        isSplitView,
        interactionAnswerErrorReceiver
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, _, _, hasConversationView, _, _, interactionAnswerErrorReceiver, _, isSplitView ->
      NumericInputViewModel(
        context,
        hasConversationView,
        interactionAnswerErrorReceiver,
        isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(): InteractionViewModelFactory {
    return { _, _, _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _, isSplitView -> // ktlint-disable max-line-length
      TextInputViewModel(
        interaction, hasConversationView, interactionAnswerErrorReceiver, isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(): InteractionViewModelFactory {
    return { gcsResourceName, gcsEntityType, entityId, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _, isSplitView -> // ktlint-disable max-line-length
      DragAndDropSortInteractionViewModel(
        gcsResourceName, gcsEntityType, entityId, hasConversationView, interaction, interactionAnswerErrorReceiver, isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ImageClickInput")
  fun provideImageClickInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, _, entityId, _, interaction, _, interactionAnswerErrorReceiver, _, _ ->
      ImageRegionSelectionInteractionViewModel(
        entityId,
        interaction,
        interactionAnswerErrorReceiver,
        context
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("RatioExpressionInput")
  fun provideRatioExpressionInputViewModelFactory(context: Context): InteractionViewModelFactory {
    return { _, _, _, hasConversationView, interaction, _, answerErrorReceiver, _, isSplitView ->
      RatioExpressionInputInteractionViewModel(
        interaction,
        context,
        hasConversationView,
        isSplitView,
        answerErrorReceiver
      )
    }
  }
}
