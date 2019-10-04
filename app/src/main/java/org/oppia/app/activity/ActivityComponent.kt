package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.fragment.FragmentComponent
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.topic.TopicActivity
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

  fun inject(explorationActivity: ExplorationActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(bindableAdapterTestActivity: BindableAdapterTestActivity)
  fun inject(topicActivity: TopicActivity)
}
