package org.oppia.android.app.player.state.itemviewmodel

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController

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
      isSplitView, writtenTranslationContext ->
      ContinueInteractionViewModel(
        interactionAnswerReceiver,
        hasConversationView,
        hasPreviousButton,
        fragment as PreviousNavigationButtonListener,
        isSplitView,
        writtenTranslationContext
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputViewModelFactory(
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _,
      interactionAnswerErrorReceiver, _, isSplitView, writtenTranslationContext ->
      SelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        interactionAnswerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        translationController
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputViewModelFactory(
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _,
      interactionAnswerErrorReceiver, _, isSplitView, writtenTranslationContext ->
      SelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        interactionAnswerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        translationController
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler,
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView, writtenTranslationContext ->
      FractionInteractionViewModel(
        interaction,
        hasConversationView,
        isSplitView,
        interactionAnswerErrorReceiver,
        writtenTranslationContext,
        resourceHandler,
        translationController
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { _, hasConversationView, _, _, interactionAnswerErrorReceiver, _, isSplitView,
      writtenTranslationContext ->
      NumericInputViewModel(
        hasConversationView,
        interactionAnswerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView, writtenTranslationContext ->
      TextInputViewModel(
        interaction, hasConversationView, interactionAnswerErrorReceiver, isSplitView,
        writtenTranslationContext, translationController
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler,
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _, interactionAnswerErrorReceiver, _,
      isSplitView, writtenTranslationContext ->
      DragAndDropSortInteractionViewModel(
        entityId, hasConversationView, interaction, interactionAnswerErrorReceiver, isSplitView,
        writtenTranslationContext, resourceHandler, translationController
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("ImageClickInput")
  fun provideImageClickInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler
  ): InteractionViewModelFactory {
    return { entityId, hasConversationView, interaction, _, answerErrorReceiver, _, isSplitView,
      writtenTranslationContext ->
      ImageRegionSelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler
      )
    }
  }

  @Provides
  @IntoMap
  @StringKey("RatioExpressionInput")
  fun provideRatioExpressionInputViewModelFactory(
    resourceHandler: AppLanguageResourceHandler,
    translationController: TranslationController
  ): InteractionViewModelFactory {
    return { _, hasConversationView, interaction, _, answerErrorReceiver, _, isSplitView,
      writtenTranslationContext ->
      RatioExpressionInputInteractionViewModel(
        interaction,
        hasConversationView,
        isSplitView,
        answerErrorReceiver,
        writtenTranslationContext,
        resourceHandler,
        translationController
      )
    }
  }
}
