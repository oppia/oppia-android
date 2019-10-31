package org.oppia.app.player.state.itemviewmodel

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/** Module to provide interaction view model-specific dependencies. */
@Module
class InteractionViewModelModule {
  // TODO(BenHenning): Use a common source for these interaction IDs to de-duplicate them from other places in the
  //  codebase where they are referenced.
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(): InteractionViewModelFactory {
    return { _, _, interactionAnswerReceiver, existingAnswer, isReadOnly ->
      ContinueInteractionViewModel(interactionAnswerReceiver, existingAnswer, isReadOnly)
    }
  }

  @Provides
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputViewModelFactory(): InteractionViewModelFactory {
    return ::SelectionInteractionViewModel
  }

  @Provides
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputViewModelFactory(): InteractionViewModelFactory {
    return ::SelectionInteractionViewModel
  }

  @Provides
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputViewModelFactory(): InteractionViewModelFactory {
    return { _, _, _, existingAnswer, isReadOnly -> FractionInteractionViewModel(existingAnswer, isReadOnly) }
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(): InteractionViewModelFactory {
    return { _, _, _, existingAnswer, isReadOnly -> NumericInputViewModel(existingAnswer, isReadOnly) }
  }

  @Provides
  @IntoMap
  @StringKey("NumberWithUnits")
  fun provideNumberWithUnitsViewModelFactory(): InteractionViewModelFactory {
    return { _, _, _, existingAnswer, isReadOnly -> NumberWithUnitsViewModel(existingAnswer, isReadOnly) }
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(): InteractionViewModelFactory {
    return { _, _, _, existingAnswer, isReadOnly -> TextInputViewModel(existingAnswer, isReadOnly) }
  }
}
