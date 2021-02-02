package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.completedstorylist.CompletedStoryListFragment
import org.oppia.android.app.deprecation.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.help.HelpFragment
import org.oppia.android.app.help.faq.FAQListFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.home.HomeFragment
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.android.app.mydownloads.DownloadsTabFragment
import org.oppia.android.app.mydownloads.MyDownloadsFragment
import org.oppia.android.app.mydownloads.UpdatesTabFragment
import org.oppia.android.app.onboarding.OnboardingFragment
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListFragment
import org.oppia.android.app.options.AppLanguageFragment
import org.oppia.android.app.options.AudioLanguageFragment
import org.oppia.android.app.options.OptionsFragment
import org.oppia.android.app.options.ReadingTextSizeFragment
import org.oppia.android.app.player.audio.AudioFragment
import org.oppia.android.app.player.exploration.ExplorationFragment
import org.oppia.android.app.player.exploration.ExplorationManagerFragment
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.android.app.profile.AdminSettingsDialogFragment
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.app.profile.ResetPinDialogFragment
import org.oppia.android.app.profileprogress.ProfileProgressFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.story.StoryFragment
import org.oppia.android.app.testing.ImageRegionSelectionTestFragment
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.info.TopicInfoFragment
import org.oppia.android.app.topic.lessons.TopicLessonsFragment
import org.oppia.android.app.topic.practice.TopicPracticeFragment
import org.oppia.android.app.topic.questionplayer.HintsAndSolutionQuestionManagerFragment
import org.oppia.android.app.topic.questionplayer.QuestionPlayerFragment
import org.oppia.android.app.topic.revision.TopicRevisionFragment
import org.oppia.android.app.topic.revisioncard.RevisionCardFragment
import org.oppia.android.app.view.ViewComponent
import org.oppia.android.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.android.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.android.app.walkthrough.welcome.WalkthroughWelcomeFragment
import javax.inject.Provider

/** Root subcomponent for all fragments. */
@Subcomponent(
  modules = [
    FragmentModule::class, InteractionViewModelModule::class, IntentFactoryShimModule::class,
    ViewBindingShimModule::class
  ]
)
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
  fun inject(completedStoryListFragment: CompletedStoryListFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(audioLanguageFragment: AudioLanguageFragment)
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
  fun inject(profileListFragment: ProfileListFragment)
  fun inject(profileProgressFragment: ProfileProgressFragment)
  fun inject(questionPlayerFragment: QuestionPlayerFragment)
  fun inject(recentlyPlayedFragment: RecentlyPlayedFragment)
  fun inject(resetPinDialogFragment: ResetPinDialogFragment)
  fun inject(revisionCardFragment: RevisionCardFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(storyFragment: StoryFragment)
  fun inject(readingTextSizeFragment: ReadingTextSizeFragment)
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
