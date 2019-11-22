package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.fragment.FragmentComponent
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.home.continueplaying.ContinuePlayingActivity
import org.oppia.app.player.audio.testing.AudioFragmentTestActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.settings.profile.ProfileRenameActivity
import org.oppia.app.settings.profile.ProfileResetPinActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.story.testing.StoryFragmentTestActivity
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.ContentCardTestActivity
import org.oppia.app.testing.ContinuePlayingFragmentTestActivity
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.app.testing.TopicTestActivity
import org.oppia.app.testing.TopicTestActivityForStory
import org.oppia.app.topic.TopicActivity
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import javax.inject.Provider

/** Root subcomponent for all activities. */
@Subcomponent(modules = [ActivityModule::class])
@ActivityScope
interface ActivityComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setActivity(appCompatActivity: AppCompatActivity): Builder

    fun build(): ActivityComponent
  }

  fun getFragmentComponentBuilderProvider(): Provider<FragmentComponent.Builder>

  fun inject(audioFragmentTestActivity: AudioFragmentTestActivity)
  fun inject(bindableAdapterTestActivity: BindableAdapterTestActivity)
  fun inject(conceptCardFragmentTestActivity: ConceptCardFragmentTestActivity)
  fun inject(contentCardTestActivity: ContentCardTestActivity)
  fun inject(continuePlayingActivity: ContinuePlayingActivity)
  fun inject(continuePlayingFragmentTestActivity: ContinuePlayingFragmentTestActivity)
  fun inject(explorationActivity: ExplorationActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(helpActivity: HelpActivity)
  fun inject(htmlParserTestActivity: HtmlParserTestActivity)
  fun inject(profileActivity: ProfileActivity)
  fun inject(questionPlayerActivity: QuestionPlayerActivity)
  fun inject(stateFragmentTestActivity: StateFragmentTestActivity)
  fun inject(profileRenameActivity: ProfileRenameActivity)
  fun inject(profileResetPinActivity: ProfileResetPinActivity)
  fun inject(storyActivity: StoryActivity)
  fun inject(topicActivity: TopicActivity)
  fun inject(topicTestActivity: TopicTestActivity)
  fun inject(topicTestActivityForStory: TopicTestActivityForStory)
  fun inject(storyFragmentTestActivity: StoryFragmentTestActivity)
}
