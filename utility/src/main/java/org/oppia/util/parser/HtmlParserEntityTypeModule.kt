package org.oppia.util.parser

import dagger.Module
import dagger.Provides

/** Provides Html parsing entity type dependencies. */
@Module
class HtmlParserEntityTypeModule {
  @Provides
  @ExplorationHtmlParserEntityType
  fun provideExplorationHtmlParserEntityType(): String {
    return "exploration"
  }

  @Provides
  @ConceptCardHtmlParserEntityType
  fun provideConceptCardHtmlParserEntityType(): String {
    return "skill"
  }

  @Provides
  @RevisionCardHtmlParserEntityType
  fun provideReviewCardHtmlParserEntityType(): String {
    return "subtopic"
  }
}
