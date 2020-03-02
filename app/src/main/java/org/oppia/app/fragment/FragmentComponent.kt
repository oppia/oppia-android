package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.app.help.HelpFragment
import org.oppia.app.home.HomeFragment
import org.oppia.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.app.onboarding.OnboardingFragment
import org.oppia.app.mydownloads.DownloadsTabFragment
import org.oppia.app.mydownloads.MyDownloadsFragment
import org.oppia.app.mydownloads.UpdatesTabFragment
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.exploration.ExplorationFragment
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.app.profile.AdminSettingsDialogFragment
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.app.profile.ResetPinDialogFragment
import org.oppia.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.app.story.StoryFragment
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.app.topic.lessons.TopicLessonsFragment
import org.oppia.app.topic.practice.TopicPracticeFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.reviewcard.ReviewCardFragment
import org.oppia.app.view.ViewComponent
import org.oppia.app.walkthrough.WalkthroughFragment
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

  fun inject(administratorControlsFragment: AdministratorControlsFragment)
  fun inject(adminSettingsDialogFragment: AdminSettingsDialogFragment)
  fun inject(audioFragment: AudioFragment)
  fun inject(bindableAdapterTestFragment: BindableAdapterTestFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(downloadsTabFragment: DownloadsTabFragment)
  fun inject(explorationFragment: ExplorationFragment)
  fun inject(helpFragment: HelpFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(myDownloadsFragment: MyDownloadsFragment)
  fun inject(navigationDrawerFragment: NavigationDrawerFragment)
  fun inject(onboardingFragment: OnboardingFragment)
  fun inject(profileChooserFragment: ProfileChooserFragment)
  fun inject(questionPlayerFragment: QuestionPlayerFragment)
  fun inject(recentlyPlayedFragment: RecentlyPlayedFragment)
  fun inject(resetPinDialogFragment: ResetPinDialogFragment)
  fun inject(reviewCardFragment: ReviewCardFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(storyFragment: StoryFragment)
  fun inject(topicFragment: TopicFragment)
  fun inject(topicInfoFragment: TopicInfoFragment)
  fun inject(topicLessonsFragment: TopicLessonsFragment)
  fun inject(topicReviewFragment: TopicReviewFragment)
  fun inject(topicPracticeFragment: TopicPracticeFragment)
  fun inject(updatesTabFragment: UpdatesTabFragment)
  fun inject(walkthroughFragment: WalkthroughFragment)
}
