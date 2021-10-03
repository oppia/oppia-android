package org.oppia.android.app.player.state.itemviewmodel

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler

/**
 * Module to provide interaction view model-specific dependencies for interactions that should be
 * explicitly displayed to the user.
 */
@Module
class InteractionViewModelModule {
  companion object {
    val splitScreenInteractionIdsPool = listOf("DragAndDropSortInput", "ImageClickInput")
  }

  // TODO(#300): Use a common source for these interaction IDs to de-duplicate them from
  //  other places in the codebase where they are referenced.
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(fragment: Fragment): InteractionViewModelFactory {
    return { _, hasConversationView, _, interactionAnswerReceiver, _, hasPreviousButton,
      isSplitView ->
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
    return { entityId, hasConversationView, interaction, interactionAnswerReceiver,
      interactionAnswerErrorReceiver, _, isSplitView ->
      SelectionInteractionViewModel(
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
    return { entityId, hasConversationView, interaction, interactionAnswerReceiver,
      interactionAnswerErrorReceiver, _, isSplitView ->
      SelectionInteractionViewModel(
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
  fun provideFractionInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView ->
      FractionInteractionViewModel(
        interaction,
        hasConversationView,
        isSplitView,
        interactionAnswerErrorReceiver,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { _, hasConversationView, _, _, interactionAnswerErrorReceiver, _, isSplitView ->
      NumericInputViewModel(
        hasConversationView,
        interactionAnswerErrorReceiver,
        isSplitView,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView ->
      TextInputViewModel(
        interaction, hasConversationView, interactionAnswerErrorReceiver, isSplitView
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView ->
      DragAndDropSortInteractionViewModel(
        entityId, hasConversationView, interaction, interactionAnswerErrorReceiver, isSplitView,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ImageClickInput")
  fun provideImageClickInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _, answerErrorReceiver, _, isSplitView ->
      ImageRegionSelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("RatioExpressionInput")
  fun provideRatioExpressionInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, answerErrorReceiver, _, isSplitView ->
      RatioExpressionInputInteractionViewModel(
        interaction,
        hasConversationView,
        isSplitView,
        answerErrorReceiver,
        resourceHandler
      )
    }
  }
}
