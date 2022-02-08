package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsActivity
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeActivity
import org.oppia.android.app.devoptions.forcenetworktype.testing.ForceNetworkTypeTestActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markchapterscompleted.testing.MarkChaptersCompletedTestActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.testing.MarkStoriesCompletedTestActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.testing.MarkTopicsCompletedTestActivity
import org.oppia.android.app.devoptions.testing.DeveloperOptionsTestActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.devoptions.vieweventlogs.testing.ViewEventLogsTestActivity
import org.oppia.android.app.fragment.FragmentComponentBuilderInjector
import org.oppia.android.app.fragment.FragmentComponentBuilderModule
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.LicenseTextViewerActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.options.AppLanguageActivity
import org.oppia.android.app.options.AudioLanguageActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.options.ReadingTextSizeActivity
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity
import org.oppia.android.app.policies.PoliciesActivity
import org.oppia.android.app.profile.AddProfileActivity
import org.oppia.android.app.profile.AdminAuthActivity
import org.oppia.android.app.profile.AdminPinActivity
import org.oppia.android.app.profile.PinPasswordActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.profileprogress.ProfilePictureActivity
import org.oppia.android.app.profileprogress.ProfileProgressActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.settings.profile.ProfileEditActivity
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.settings.profile.ProfileRenameActivity
import org.oppia.android.app.settings.profile.ProfileResetPinActivity
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.testing.ConceptCardFragmentTestActivity
import org.oppia.android.app.testing.DragDropTestActivity
import org.oppia.android.app.testing.DrawableBindingAdaptersTestActivity
import org.oppia.android.app.testing.ExplorationInjectionActivity
import org.oppia.android.app.testing.ExplorationTestActivity
import org.oppia.android.app.testing.HomeFragmentTestActivity
import org.oppia.android.app.testing.HomeTestActivity
import org.oppia.android.app.testing.HtmlParserTestActivity
import org.oppia.android.app.testing.ImageRegionSelectionTestActivity
import org.oppia.android.app.testing.ImageViewBindingAdaptersTestActivity
import org.oppia.android.app.testing.InputInteractionViewTestActivity
import org.oppia.android.app.testing.MarginBindingAdaptersTestActivity
import org.oppia.android.app.testing.NavigationDrawerTestActivity
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.app.testing.SplashTestActivity
import org.oppia.android.app.testing.StateAssemblerMarginBindingAdaptersTestActivity
import org.oppia.android.app.testing.StateAssemblerPaddingBindingAdaptersTestActivity
import org.oppia.android.app.testing.TestFontScaleConfigurationUtilActivity
import org.oppia.android.app.testing.TopicRevisionTestActivity
import org.oppia.android.app.testing.TopicTestActivity
import org.oppia.android.app.testing.TopicTestActivityForStory
import org.oppia.android.app.testing.ViewBindingAdaptersTestActivity
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.app.walkthrough.WalkthroughActivity

// TODO(#59): Restrict access to this implementation by introducing injectors in each activity.

/** Implementation of [ActivityComponent]. */
@Subcomponent(
  modules = [
    ActivityModule::class, FragmentComponentBuilderModule::class,
    ActivityIntentFactoriesModule::class
  ]
)
@ActivityScope
interface ActivityComponentImpl :
  ActivityComponent, FragmentComponentBuilderInjector, TestActivity.Injector {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setActivity(appCompatActivity: AppCompatActivity): Builder

    fun build(): ActivityComponentImpl
  }

  fun inject(addProfileActivity: AddProfileActivity)
  fun inject(adminAuthActivity: AdminAuthActivity)
  fun inject(administratorControlsActivity: AdministratorControlsActivity)
  fun inject(adminPinActivity: AdminPinActivity)
  fun inject(appLanguageActivity: AppLanguageActivity)
  fun inject(appVersionActivity: AppVersionActivity)
  fun inject(audioFragmentTestActivity: AudioFragmentTestActivity)
  fun inject(audioLanguageActivity: AudioLanguageActivity)
  fun inject(completedStoryListActivity: CompletedStoryListActivity)
  fun inject(conceptCardFragmentTestActivity: ConceptCardFragmentTestActivity)
  fun inject(developerOptionsActivity: DeveloperOptionsActivity)
  fun inject(developerOptionsTestActivity: DeveloperOptionsTestActivity)
  fun inject(dragDropTestActivity: DragDropTestActivity)
  fun inject(drawableBindingAdaptersTestActivity: DrawableBindingAdaptersTestActivity)
  fun inject(explorationActivity: ExplorationActivity)
  fun inject(explorationInjectionActivity: ExplorationInjectionActivity)
  fun inject(explorationTestActivity: ExplorationTestActivity)
  fun inject(faqListActivity: FAQListActivity)
  fun inject(faqSingleActivity: FAQSingleActivity)
  fun inject(forceNetworkTypeActivity: ForceNetworkTypeActivity)
  fun inject(forceNetworkTypeTestActivity: ForceNetworkTypeTestActivity)
  fun inject(helpActivity: HelpActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(homeFragmentTestActivity: HomeFragmentTestActivity)
  fun inject(homeTestActivity: HomeTestActivity)
  fun inject(htmlParserTestActivity: HtmlParserTestActivity)
  fun inject(imageRegionSelectionTestActivity: ImageRegionSelectionTestActivity)
  fun inject(imageViewBindingAdaptersTestActivity: ImageViewBindingAdaptersTestActivity)
  fun inject(inputInteractionViewTestActivity: InputInteractionViewTestActivity)
  fun inject(licenseListActivity: LicenseListActivity)
  fun inject(licenseTextViewerActivity: LicenseTextViewerActivity)
  fun inject(markChaptersCompletedActivity: MarkChaptersCompletedActivity)
  fun inject(markChaptersCompletedTestActivity: MarkChaptersCompletedTestActivity)
  fun inject(markStoriesCompletedActivity: MarkStoriesCompletedActivity)
  fun inject(markStoriesCompletedTestActivity: MarkStoriesCompletedTestActivity)
  fun inject(markTopicsCompletedActivity: MarkTopicsCompletedActivity)
  fun inject(marginBindableAdaptersTestActivity: MarginBindingAdaptersTestActivity)
  fun inject(markTopicsCompletedTestActivity: MarkTopicsCompletedTestActivity)
  fun inject(myDownloadsActivity: MyDownloadsActivity)
  fun inject(navigationDrawerTestActivity: NavigationDrawerTestActivity)
  fun inject(onboardingActivity: OnboardingActivity)
  fun inject(ongoingTopicListActivity: OngoingTopicListActivity)
  fun inject(optionActivity: OptionsActivity)
  fun inject(pinPasswordActivity: PinPasswordActivity)
  fun inject(policiesActivity: PoliciesActivity)
  fun inject(profileChooserActivity: ProfileChooserActivity)
  fun inject(profileChooserFragmentTestActivity: ProfileChooserFragmentTestActivity)
  fun inject(profileEditActivity: ProfileEditActivity)
  fun inject(profileListActivity: ProfileListActivity)
  fun inject(profilePictureActivity: ProfilePictureActivity)
  fun inject(profileProgressActivity: ProfileProgressActivity)
  fun inject(profileRenameActivity: ProfileRenameActivity)
  fun inject(profileResetPinActivity: ProfileResetPinActivity)
  fun inject(questionPlayerActivity: QuestionPlayerActivity)
  fun inject(readingTextSizeActivity: ReadingTextSizeActivity)
  fun inject(recentlyPlayedActivity: RecentlyPlayedActivity)
  fun inject(resumeLessonActivity: ResumeLessonActivity)
  fun inject(revisionCardActivity: RevisionCardActivity)
  fun inject(splashActivity: SplashActivity)
  fun inject(splashTestActivity: SplashTestActivity)
  fun inject(
    stateAssemblerMarginBindingAdaptersTestActivity:
      StateAssemblerMarginBindingAdaptersTestActivity
  )

  fun inject(
    stateAssemblerPaddingBindingAdaptersTestActivity:
      StateAssemblerPaddingBindingAdaptersTestActivity
  )

  fun inject(stateFragmentTestActivity: StateFragmentTestActivity)
  fun inject(storyActivity: StoryActivity)
  fun inject(testFontScaleConfigurationUtilActivity: TestFontScaleConfigurationUtilActivity)
  fun inject(thirdPartyDependencyListActivity: ThirdPartyDependencyListActivity)
  fun inject(topicActivity: TopicActivity)
  fun inject(topicRevisionTestActivity: TopicRevisionTestActivity)
  fun inject(topicTestActivity: TopicTestActivity)
  fun inject(topicTestActivityForStory: TopicTestActivityForStory)
  fun inject(viewBindingAdaptersTestActivity: ViewBindingAdaptersTestActivity)
  fun inject(viewEventLogsActivity: ViewEventLogsActivity)
  fun inject(viewEventLogsTestActivity: ViewEventLogsTestActivity)
  fun inject(walkthroughActivity: WalkthroughActivity)
}
