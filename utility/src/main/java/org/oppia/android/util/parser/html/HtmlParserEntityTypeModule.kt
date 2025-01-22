package org.oppia.android.util.parser.html

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
  @ClassroomHtmlParserEntityType
  fun provideClassroomCardHtmlParserEntityType(): String {
    return "classroom"
  }

  @Provides
  @TopicHtmlParserEntityType
  fun provideReviewCardHtmlParserEntityType(): String {
    return "topic"
  }

  @Provides
  @StoryHtmlParserEntityType
  fun provideStoryHtmlParserEntityType(): String {
    return "story"
  }
}
