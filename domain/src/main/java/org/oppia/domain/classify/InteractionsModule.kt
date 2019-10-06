package org.oppia.domain.classify

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.rules.TextInputRules

/** Module that provides a map of [InteractionClassifier]s. */
@Module
class InteractionsModule {
  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputInteractionClassifier(
    @TextInputRules textInputRules: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(textInputRules)
  }
}
