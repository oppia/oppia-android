package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.app.completedstorylist.CompletedStoryListFragment
import org.oppia.app.deprecation.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.app.help.HelpFragment
import org.oppia.app.help.faq.FAQListFragment
import org.oppia.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.app.home.HomeFragment
import org.oppia.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.app.mydownloads.DownloadsTabFragment
import org.oppia.app.mydownloads.MyDownloadsFragment
import org.oppia.app.mydownloads.UpdatesTabFragment
import org.oppia.app.onboarding.OnboardingFragment
import org.oppia.app.ongoingtopiclist.OngoingTopicListFragment
import org.oppia.app.options.AppLanguageFragment
import org.oppia.app.options.DefaultAudioFragment
import org.oppia.app.options.OptionsFragment
import org.oppia.app.options.StoryTextSizeFragment
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.exploration.ExplorationFragment
import org.oppia.app.player.exploration.ExplorationManagerFragment
import org.oppia.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.app.profile.AdminSettingsDialogFragment
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.app.profile.ResetPinDialogFragment
import org.oppia.app.profileprogress.ProfileProgressFragment
import org.oppia.app.story.StoryFragment
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.testing.ImageRegionSelectionTestFragment
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.app.topic.lessons.TopicLessonsFragment
import org.oppia.app.topic.practice.TopicPracticeFragment
import org.oppia.app.topic.questionplayer.HintsAndSolutionQuestionManagerFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerFragment
import org.oppia.app.topic.revision.TopicRevisionFragment
import org.oppia.app.topic.revisioncard.RevisionCardFragment
import org.oppia.app.view.ViewComponent
import org.oppia.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.app.walkthrough.welcome.WalkthroughWelcomeFragment
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
  fun inject(appLanguageFragment: AppLanguageFragment)
  fun inject(appVersionFragment: AppVersionFragment)
  fun inject(audioFragment: AudioFragment)
  fun inject(autoAppDeprecationNoticeDialogFragment: AutomaticAppDeprecationNoticeDialogFragment)
  fun inject(bindableAdapterTestFragment: BindableAdapterTestFragment)
  fun inject(completedStoryListFragment: CompletedStoryListFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(defaultAudioFragment: DefaultAudioFragment)
  fun inject(downloadsTabFragment: DownloadsTabFragment)
  fun inject(explorationFragment: ExplorationFragment)
  fun inject(explorationManagerFragment: ExplorationManagerFragment)
  fun inject(faqListFragment: FAQListFragment)
  fun inject(helpFragment: HelpFragment)
  fun inject(hintsAndSolutionDialogFragment: HintsAndSolutionDialogFragment)
  fun inject(hintsAndSolutionExplorationManagerFragment: HintsAndSolutionExplorationManagerFragment)
  fun inject(hintsAndSolutionQuestionManagerFragment: HintsAndSolutionQuestionManagerFragment)
  fun inject(imageRegionSelectionTestFragment: ImageRegionSelectionTestFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(myDownloadsFragment: MyDownloadsFragment)
  fun inject(navigationDrawerFragment: NavigationDrawerFragment)
  fun inject(onboardingFragment: OnboardingFragment)
  fun inject(ongoingTopicListFragment: OngoingTopicListFragment)
  fun inject(optionFragment: OptionsFragment)
  fun inject(profileChooserFragment: ProfileChooserFragment)
  fun inject(profileProgressFragment: ProfileProgressFragment)
  fun inject(questionPlayerFragment: QuestionPlayerFragment)
  fun inject(recentlyPlayedFragment: RecentlyPlayedFragment)
  fun inject(resetPinDialogFragment: ResetPinDialogFragment)
  fun inject(revisionCardFragment: RevisionCardFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(storyFragment: StoryFragment)
  fun inject(storyTextSizeFragment: StoryTextSizeFragment)
  fun inject(topicFragment: TopicFragment)
  fun inject(topicInfoFragment: TopicInfoFragment)
  fun inject(topicLessonsFragment: TopicLessonsFragment)
  fun inject(topicReviewFragment: TopicRevisionFragment)
  fun inject(topicPracticeFragment: TopicPracticeFragment)
  fun inject(updatesTabFragment: UpdatesTabFragment)
  fun inject(walkthroughFinalFragment: WalkthroughFinalFragment)
  fun inject(walkthroughTopicListFragment: WalkthroughTopicListFragment)
  fun inject(walkthroughWelcomeFragment: WalkthroughWelcomeFragment)
}
