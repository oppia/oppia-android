package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.home.HomeFragment
import org.oppia.app.home.continueplaying.ContinuePlayingFragment
import org.oppia.app.mydownloads.DownloadsTabFragment
import org.oppia.app.mydownloads.MyDownloadsFragment
import org.oppia.app.mydownloads.UpdatesTabFragment
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.exploration.ExplorationFragment
import org.oppia.app.player.state.StateFragment
import org.oppia.app.profile.AdminSettingsDialogFragment
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.app.profile.ResetPinDialogFragment
import org.oppia.app.story.StoryFragment
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment
import org.oppia.app.view.ViewComponent
import javax.inject.Provider

/** Root subcomponent for all fragments. */
@Subcomponent(modules = [FragmentModule::class, InteractionViewModelModule::class])
@FragmentScope
interface FragmentComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setFragment(fragment: Fragment): Builder

    fun build(): FragmentComponent
  }

  fun getViewComponentBuilderProvider(): Provider<ViewComponent.Builder>

  fun inject(adminSettingsDialogFragment: AdminSettingsDialogFragment)
  fun inject(audioFragment: AudioFragment)
  fun inject(bindableAdapterTestFragment: BindableAdapterTestFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(continuePlayingFragment: ContinuePlayingFragment)
  fun inject(downloadsTabFragment: DownloadsTabFragment)
  fun inject(explorationFragment: ExplorationFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(myDownloadsFragment: MyDownloadsFragment)
  fun inject(profileChooserFragment: ProfileChooserFragment)
  fun inject(questionPlayerFragment: QuestionPlayerFragment)
  fun inject(resetPinDialogFragment: ResetPinDialogFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(storyFragment: StoryFragment)
  fun inject(topicFragment: TopicFragment)
  fun inject(topicOverviewFragment: TopicOverviewFragment)
  fun inject(topicPlayFragment: TopicPlayFragment)
  fun inject(topicReviewFragment: TopicReviewFragment)
  fun inject(topicTrainFragment: TopicTrainFragment)
  fun inject(updatesTabFragment: UpdatesTabFragment)
}
