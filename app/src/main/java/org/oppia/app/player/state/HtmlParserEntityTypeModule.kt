package org.oppia.app.player.state

import dagger.Module
import dagger.Provides

@Module
class HtmlParserEntityTypeModule {
  @Provides
  @ExplorationHtmlParserEntityType
  fun provideExplorationHtmlParserEntityType(): String {
    return "exploration"
  }
}
