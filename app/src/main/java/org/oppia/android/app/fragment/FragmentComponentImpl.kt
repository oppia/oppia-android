package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsFragment
import org.oppia.android.app.administratorcontrols.LogoutDialogFragment
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileAndDeviceIdFragment
import org.oppia.android.app.classroom.ClassroomListFragment
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
import org.oppia.android.app.notice.ForcedAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.notice.OptionalAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.OsDeprecationNoticeDialogFragment
import org.oppia.android.app.onboarding.CreateProfileFragment
import org.oppia.android.app.onboarding.IntroFragment
import org.oppia.android.app.onboarding.OnboardingFragment
import org.oppia.android.app.onboarding.OnboardingProfileTypeFragment
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
import org.oppia.android.app.survey.ExitSurveyConfirmationDialogFragment
import org.oppia.android.app.survey.SurveyFragment
import org.oppia.android.app.survey.SurveyOutroDialogFragment
import org.oppia.android.app.survey.SurveyWelcomeDialogFragment
import org.oppia.android.app.testing.DragDropTestFragment
import org.oppia.android.app.testing.ExplorationTestActivityPresenter
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
interface FragmentComponentImpl : FragmentComponent, ViewComponentBuilderInjector {
  /** Implementation of [FragmentComponent.Builder]. */
  @Subcomponent.Builder
  interface Builder : FragmentComponent.Builder {
    @BindsInstance
    override fun setFragment(fragment: Fragment): Builder

    override fun build(): FragmentComponentImpl
  }

  fun inject(administratorControlsFragment: AdministratorControlsFragment)
  fun inject(adminSettingsDialogFragment: AdminSettingsDialogFragment)
  fun inject(appLanguageFragment: AppLanguageFragment)
  fun inject(appVersionFragment: AppVersionFragment)
  fun inject(audioFragment: AudioFragment)
  fun inject(audioLanguageFragment: AudioLanguageFragment)
  fun inject(
    automaticAppDeprecationNoticeDialogFragment:
      AutomaticAppDeprecationNoticeDialogFragment
  )
  fun inject(betaNoticeDialogFragment: BetaNoticeDialogFragment)
  fun inject(cellularAudioDialogFragment: CellularAudioDialogFragment)
  fun inject(completedStoryListFragment: CompletedStoryListFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
  fun inject(developerOptionsFragment: DeveloperOptionsFragment)
  fun inject(downloadsTabFragment: DownloadsTabFragment)
  fun inject(dragDropTestFragment: DragDropTestFragment)
  fun inject(exitProfileDialogFragment: ExitProfileDialogFragment)
  fun inject(explorationFragment: ExplorationFragment)
  fun inject(explorationManagerFragment: ExplorationManagerFragment)
  fun inject(explorationTestActivityTestFragment: ExplorationTestActivityPresenter.TestFragment)
  fun inject(faqListFragment: FAQListFragment)
  fun inject(forceNetworkTypeFragment: ForceNetworkTypeFragment)
  fun inject(forcedAppDeprecationNoticeDialogFragment: ForcedAppDeprecationNoticeDialogFragment)
  fun inject(fragment: GeneralAvailabilityUpgradeNoticeDialogFragment)
  fun inject(helpFragment: HelpFragment)
  fun inject(hintsAndSolutionDialogFragment: HintsAndSolutionDialogFragment)
  fun inject(hintsAndSolutionExplorationManagerFragment: HintsAndSolutionExplorationManagerFragment)
  fun inject(hintsAndSolutionQuestionManagerFragment: HintsAndSolutionQuestionManagerFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(imageRegionSelectionTestFragment: ImageRegionSelectionTestFragment)
  fun inject(languageDialogFragment: LanguageDialogFragment)
  fun inject(licenseListFragment: LicenseListFragment)
  fun inject(licenseTextViewerFragment: LicenseTextViewerFragment)
  fun inject(logoutDialogFragment: LogoutDialogFragment)
  fun inject(markChapterCompletedFragment: MarkChaptersCompletedFragment)
  fun inject(markStoriesCompletedFragment: MarkStoriesCompletedFragment)
  fun inject(markTopicsCompletedFragment: MarkTopicsCompletedFragment)
  fun inject(mathExpressionParserFragment: MathExpressionParserFragment)
  fun inject(myDownloadsFragment: MyDownloadsFragment)
  fun inject(navigationDrawerFragment: NavigationDrawerFragment)
  fun inject(onboardingFragment: OnboardingFragment)
  fun inject(ongoingTopicListFragment: OngoingTopicListFragment)
  fun inject(optionalAppDeprecationNoticeDialogFragment: OptionalAppDeprecationNoticeDialogFragment)
  fun inject(optionFragment: OptionsFragment)
  fun inject(osDeprecationNoticeDialogFragment: OsDeprecationNoticeDialogFragment)
  fun inject(policiesFragment: PoliciesFragment)
  fun inject(profileAndDeviceIdFragment: ProfileAndDeviceIdFragment)
  fun inject(profileChooserFragment: ProfileChooserFragment)
  fun inject(profileEditDeletionDialogFragment: ProfileEditDeletionDialogFragment)
  fun inject(profileEditFragment: ProfileEditFragment)
  fun inject(profileListFragment: ProfileListFragment)
  fun inject(profileRenameFragment: ProfileRenameFragment)
  fun inject(profilePictureEditDialogFragment: ProfilePictureEditDialogFragment)
  fun inject(profileProgressFragment: ProfileProgressFragment)
  fun inject(profileResetPinFragment: ProfileResetPinFragment)
  fun inject(progressDatabaseFullDialogFragment: ProgressDatabaseFullDialogFragment)
  fun inject(questionPlayerFragment: QuestionPlayerFragment)
  fun inject(readingTextSizeFragment: ReadingTextSizeFragment)
  fun inject(recentlyPlayedFragment: RecentlyPlayedFragment)
  fun inject(resetPinDialogFragment: ResetPinDialogFragment)
  fun inject(resumeLessonFragment: ResumeLessonFragment)
  fun inject(revealSolutionDialogFragment: RevealSolutionDialogFragment)
  fun inject(revisionCardFragment: RevisionCardFragment)
  fun inject(spotlightFragment: SpotlightFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(stopExplorationDialogFragment: StopExplorationDialogFragment)
  fun inject(storyFragment: StoryFragment)
  fun inject(thirdPartyDependencyListFragment: ThirdPartyDependencyListFragment)
  fun inject(topicFragment: TopicFragment)
  fun inject(topicInfoFragment: TopicInfoFragment)
  fun inject(topicLessonsFragment: TopicLessonsFragment)
  fun inject(topicPracticeFragment: TopicPracticeFragment)
  fun inject(topicReviewFragment: TopicRevisionFragment)
  fun inject(unsavedExplorationDialogFragment: UnsavedExplorationDialogFragment)
  fun inject(updatesTabFragment: UpdatesTabFragment)
  fun inject(viewEventLogsFragment: ViewEventLogsFragment)
  fun inject(walkthroughFinalFragment: WalkthroughFinalFragment)
  fun inject(walkthroughTopicListFragment: WalkthroughTopicListFragment)
  fun inject(walkthroughWelcomeFragment: WalkthroughWelcomeFragment)
  fun inject(surveyFragment: SurveyFragment)
  fun inject(exitSurveyConfirmationDialogFragment: ExitSurveyConfirmationDialogFragment)
  fun inject(surveyWelcomeDialogFragment: SurveyWelcomeDialogFragment)
  fun inject(surveyOutroDialogFragment: SurveyOutroDialogFragment)
  fun inject(classroomListFragment: ClassroomListFragment)
  fun inject(onboardingProfileTypeFragment: OnboardingProfileTypeFragment)
  fun inject(createProfileFragment: CreateProfileFragment)
  fun inject(introFragment: IntroFragment)
}
