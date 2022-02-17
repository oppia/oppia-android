package org.oppia.android.app.player.state.itemviewmodel

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey

/**
 * Module to provide interaction view model-specific dependencies for interactions that should be
 * explicitly displayed to the user.
 */
@Module
interface InteractionViewModelModule {
  // TODO(#300): Use a common source for these interaction IDs to de-duplicate them from
  //  other places in the codebase where they are referenced.
  @Binds
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionViewModelFactory(
    factoryImpl: ContinueInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputViewModelFactory(
    factoryImpl: SelectionInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputViewModelFactory(
    factoryImpl: SelectionInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputViewModelFactory(
    factoryImpl: FractionInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputViewModelFactory(
    factoryImpl: NumericInputViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputViewModelFactory(
    factoryImpl: TextInputViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputViewModelFactory(
    factoryImpl: DragAndDropSortInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("ImageClickInput")
  fun provideImageClickInputViewModelFactory(
    factoryImpl: ImageRegionSelectionInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  @Binds
  @IntoMap
  @StringKey("RatioExpressionInput")
  fun provideRatioExpressionInputViewModelFactory(
    factoryImpl: RatioExpressionInputInteractionViewModel.FactoryImpl
  ): StateItemViewModel.InteractionItemFactory

  // Note that Dagger doesn't support mixing binds & provides methods. See
  // https://stackoverflow.com/a/54592300 for the origin of this approach.
  @Module
  companion object {
    @Provides
    @IntoMap
    @StringKey("NumericExpressionInput")
    @JvmStatic
    fun provideNumericExpressionInputViewModelFactory(
      factoryFactoryImpl: MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl
    ): StateItemViewModel.InteractionItemFactory {
      return factoryFactoryImpl.createFactoryForNumericExpression()
    }

    @Provides
    @IntoMap
    @StringKey("AlgebraicExpressionInput")
    @JvmStatic
    fun provideAlgebraicExpressionInputViewModelFactory(
      factoryFactoryImpl: MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl
    ): StateItemViewModel.InteractionItemFactory {
      return factoryFactoryImpl.createFactoryForAlgebraicExpression()
    }

    @Provides
    @IntoMap
    @StringKey("MathEquationInput")
    @JvmStatic
    fun provideMathEquationInputViewModelFactory(
      factoryFactoryImpl: MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl
    ): StateItemViewModel.InteractionItemFactory {
      return factoryFactoryImpl.createFactoryForMathEquation()
    }
  }
}
