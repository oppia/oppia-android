package org.oppia.android.util.parser.html

import android.view.View
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides Html parsing entity type dependencies. */
@Module
class HtmlParserEntityTypeModule {
  @Provides
  @Singleton
  fun provideConceptCardListener(): ConceptCardTagHandler.ConceptCardLinkClickListener {
    return object : ConceptCardTagHandler.ConceptCardLinkClickListener {
      override fun onConceptCardLinkClicked(view: View, skillId: String) {}
    }
  }

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
