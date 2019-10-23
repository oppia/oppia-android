package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.fragment.FragmentComponent
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.audio.testing.AudioFragmentTestActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.conceptcard.testing.ConceptCardFragmentTestActivity
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import javax.inject.Provider

/** Root subcomponent for all activities. */
@Subcomponent(modules = [ActivityModule::class])
@ActivityScope
interface ActivityComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance fun setActivity(appCompatActivity: AppCompatActivity): Builder
    fun build(): ActivityComponent
  }

  fun getFragmentComponentBuilderProvider(): Provider<FragmentComponent.Builder>

  fun inject(bindableAdapterTestActivity: BindableAdapterTestActivity)
  fun inject(explorationActivity: ExplorationActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(conceptCardFragmentTestActivity: ConceptCardFragmentTestActivity)
  fun inject(questionPlayerActivity: QuestionPlayerActivity)
  fun inject(topicActivity: TopicActivity)
  fun inject(audioFragmentTestActivity: AudioFragmentTestActivity)
  fun inject(stateFragmentTestActivity: StateFragmentTestActivity)
  fun inject(profileActivity: ProfileActivity)
}
