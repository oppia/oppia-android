package org.oppia.android.domain.classify.rules.imageClickInput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.ImageClickInputRules

/** Module that binds rule classifiers corresponding to the image click input interaction. */
@Module
class ImageClickInputModule {
  @Provides
  @IntoMap
  @StringKey("IsInRegion")
  @ImageClickInputRules
  internal fun provideImageClickInputIsInRegionRuleClassifier(
    classifierProvider: ImageClickInputIsInRegionRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
