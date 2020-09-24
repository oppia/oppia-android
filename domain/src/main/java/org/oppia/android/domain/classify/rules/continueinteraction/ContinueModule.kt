package org.oppia.domain.classify.rules.continueinteraction

import dagger.Module
import dagger.multibindings.Multibinds
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.ContinueRules

/** Module that binds rule classifiers corresponding to the continue interaction. */
@Module
abstract class ContinueModule {
  // No rules are bound since tapping the continue button for this interaction should always succeed.
  @Multibinds
  @ContinueRules
  abstract fun provideContinueInteractionRules(): Map<String, RuleClassifier>
}
