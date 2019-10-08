package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.home.HomeFragment
import org.oppia.app.player.exploration.ExplorationFragment
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment

/** Root subcomponent for all fragments. */
@Subcomponent
@FragmentScope
interface FragmentComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance fun setFragment(fragment: Fragment): Builder
    fun build(): FragmentComponent
  }

  fun inject(audioFragment: AudioFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(explorationFragment: ExplorationFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(bindableAdapterTestFragment: BindableAdapterTestFragment)
  fun inject(topicFragment: TopicFragment)
  fun inject(topicOverviewFragment: TopicOverviewFragment)
  fun inject(topicPlayFragment: TopicPlayFragment)
  fun inject(topicReviewFragment: TopicReviewFragment)
  fun inject(topicTrainFragment: TopicTrainFragment)
}
