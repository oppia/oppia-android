package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.android.app.administratorcontrols.LogoutDialogFragment
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileAndDeviceIdFragment
import org.oppia.android.app.completedstorylist.CompletedStoryListFragment
import org.oppia.android.app.devoptions.DeveloperOptionsFragment
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeFragment
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedFragment
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedFragment
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserFragment
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsFragment
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.help.HelpFragment
import org.oppia.android.app.help.faq.FAQListFragment
import org.oppia.android.app.help.thirdparty.LicenseListFragment
import org.oppia.android.app.help.thirdparty.LicenseTextViewerFragment
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.RevealSolutionDialogFragment
import org.oppia.android.app.home.HomeFragment
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.android.app.mydownloads.DownloadsTabFragment
import org.oppia.android.app.mydownloads.MyDownloadsFragment
import org.oppia.android.app.mydownloads.UpdatesTabFragment
import org.oppia.android.app.notice.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.BetaNoticeDialogFragment
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.onboarding.OnboardingFragment
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListFragment
import org.oppia.android.app.options.AppLanguageFragment
import org.oppia.android.app.options.AudioLanguageFragment
import org.oppia.android.app.options.OptionsFragment
import org.oppia.android.app.options.ReadingTextSizeFragment
import org.oppia.android.app.player.audio.AudioFragment
import org.oppia.android.app.player.audio.CellularAudioDialogFragment
import org.oppia.android.app.player.audio.LanguageDialogFragment
import org.oppia.android.app.player.exploration.ExplorationFragment
import org.oppia.android.app.player.exploration.ExplorationManagerFragment
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.android.app.player.stopplaying.ProgressDatabaseFullDialogFragment
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.UnsavedExplorationDialogFragment
import org.oppia.android.app.policies.PoliciesFragment
import org.oppia.android.app.profile.AdminSettingsDialogFragment
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.app.profile.ResetPinDialogFragment
import org.oppia.android.app.profileprogress.ProfilePictureEditDialogFragment
import org.oppia.android.app.profileprogress.ProfileProgressFragment
import org.oppia.android.app.resumelesson.ResumeLessonFragment
import org.oppia.android.app.settings.profile.ProfileEditDeletionDialogFragment
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.app.settings.profile.ProfileRenameFragment
import org.oppia.android.app.settings.profile.ProfileResetPinFragment
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.story.StoryFragment
import org.oppia.android.app.testing.DragDropTestFragment
import org.oppia.android.app.testing.ExplorationTestActivityFragment
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
import org.oppia.android.app.view.ViewComponentBuilderInjector
import org.oppia.android.app.view.ViewComponentBuilderModule
import org.oppia.android.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.android.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.android.app.walkthrough.welcome.WalkthroughWelcomeFragment

// TODO(#59): Restrict access to this implementation by introducing injectors in each fragment.

/** Implementation of [FragmentComponent]. */
@Subcomponent(
  modules = [
    FragmentModule::class, InteractionViewModelModule::class, IntentFactoryShimModule::class,
    ViewBindingShimModule::class, ViewComponentBuilderModule::class
  ]
)
@FragmentScope
interface FragmentComponentImpl :
  FragmentComponent,
  ViewComponentBuilderInjector,
  AdministratorControlsFragment.Injector,
  AdminSettingsDialogFragment.Injector,
  AppLanguageFragment.Injector,
  AppVersionFragment.Injector,
  AudioFragment.Injector,
  AudioLanguageFragment.Injector,
  AutomaticAppDeprecationNoticeDialogFragment.Injector,
  BetaNoticeDialogFragment.Injector,
  CellularAudioDialogFragment.Injector,
  CompletedStoryListFragment.Injector,
  ConceptCardFragment.Injector,
  DeveloperOptionsFragment.Injector,
  DownloadsTabFragment.Injector,
  DragDropTestFragment.Injector,
  ExitProfileDialogFragment.Injector,
  ExplorationFragment.Injector,
  ExplorationManagerFragment.Injector,
  ExplorationTestActivityFragment.Injector,
  FAQListFragment.Injector,
  ForceNetworkTypeFragment.Injector,
  GeneralAvailabilityUpgradeNoticeDialogFragment.Injector,
  HelpFragment.Injector,
  HintsAndSolutionDialogFragment.Injector,
  HintsAndSolutionExplorationManagerFragment.Injector,
  HintsAndSolutionQuestionManagerFragment.Injector,
  HomeFragment.Injector,
  ImageRegionSelectionTestFragment.Injector,
  LanguageDialogFragment.Injector,
  LicenseListFragment.Injector,
  LicenseTextViewerFragment.Injector,
  LogoutDialogFragment.Injector,
  MarkChaptersCompletedFragment.Injector,
  MarkStoriesCompletedFragment.Injector,
  MarkTopicsCompletedFragment.Injector,
  MathExpressionParserFragment.Injector,
  MyDownloadsFragment.Injector,
  NavigationDrawerFragment.Injector,
  OnboardingFragment.Injector,
  OngoingTopicListFragment.Injector,
  OptionsFragment.Injector,
  PoliciesFragment.Injector,
  ProfileAndDeviceIdFragment.Injector,
  ProfileChooserFragment.Injector,
  ProfileEditDeletionDialogFragment.Injector,
  ProfileEditFragment.Injector,
  ProfileListFragment.Injector,
  ProfileRenameFragment.Injector,
  ProfilePictureEditDialogFragment.Injector,
  ProfileProgressFragment.Injector,
  ProfileResetPinFragment.Injector,
  ProgressDatabaseFullDialogFragment.Injector,
  QuestionPlayerFragment.Injector,
  ReadingTextSizeFragment.Injector,
  RecentlyPlayedFragment.Injector,
  ResetPinDialogFragment.Injector,
  ResumeLessonFragment.Injector,
  RevealSolutionDialogFragment.Injector,
  RevisionCardFragment.Injector,
  SpotlightFragment.Injector,
  StateFragment.Injector,
  StopExplorationDialogFragment.Injector,
  StoryFragment.Injector,
  ThirdPartyDependencyListFragment.Injector,
  TopicFragment.Injector,
  TopicInfoFragment.Injector,
  TopicLessonsFragment.Injector,
  TopicPracticeFragment.Injector,
  TopicRevisionFragment.Injector,
  UnsavedExplorationDialogFragment.Injector,
  UpdatesTabFragment.Injector,
  ViewEventLogsFragment.Injector,
  WalkthroughFinalFragment.Injector,
  WalkthroughTopicListFragment.Injector,
  WalkthroughWelcomeFragment.Injector {
  /** Implementation of [FragmentComponent.Builder]. */
  @Subcomponent.Builder
  interface Builder : FragmentComponent.Builder {
    @BindsInstance
    override fun setFragment(fragment: Fragment): Builder

    override fun build(): FragmentComponentImpl
  }
}
