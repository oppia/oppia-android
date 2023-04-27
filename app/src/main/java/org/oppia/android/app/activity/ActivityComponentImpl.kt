package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileAndDeviceIdActivity
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
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivity
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
import org.oppia.android.app.testing.AdministratorControlsFragmentTestActivity
import org.oppia.android.app.testing.AppCompatCheckBoxBindingAdaptersTestActivity
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.testing.CircularProgressIndicatorAdaptersTestActivity
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
import org.oppia.android.app.testing.ListItemLeadingMarginSpanTestActivity
import org.oppia.android.app.testing.MarginBindingAdaptersTestActivity
import org.oppia.android.app.testing.NavigationDrawerTestActivity
import org.oppia.android.app.testing.PoliciesFragmentTestActivity
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.app.testing.ProfileEditFragmentTestActivity
import org.oppia.android.app.testing.SplashTestActivity
import org.oppia.android.app.testing.SpotlightFragmentTestActivity
import org.oppia.android.app.testing.StateAssemblerMarginBindingAdaptersTestActivity
import org.oppia.android.app.testing.StateAssemblerPaddingBindingAdaptersTestActivity
import org.oppia.android.app.testing.TestFontScaleConfigurationUtilActivity
import org.oppia.android.app.testing.TextViewBindingAdaptersTestActivity
import org.oppia.android.app.testing.TopicRevisionTestActivity
import org.oppia.android.app.testing.TopicTestActivity
import org.oppia.android.app.testing.TopicForStoryTestActivity
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
  ActivityComponent, FragmentComponentBuilderInjector, TestActivity.Injector,
  AddProfileActivity.Injector, AdminAuthActivity.Injector, AdministratorControlsActivity.Injector,
  AdministratorControlsFragmentTestActivity.Injector, AdminPinActivity.Injector,
  AppCompatCheckBoxBindingAdaptersTestActivity.Injector, AppLanguageActivity.Injector,
  AppVersionActivity.Injector, AudioFragmentTestActivity.Injector, AudioLanguageActivity.Injector,
  CircularProgressIndicatorAdaptersTestActivity.Injector, CompletedStoryListActivity.Injector,
  ConceptCardFragmentTestActivity.Injector, DeveloperOptionsActivity.Injector,
  DeveloperOptionsTestActivity.Injector, DragDropTestActivity.Injector,
  DrawableBindingAdaptersTestActivity.Injector, ExplorationActivity.Injector,
  ExplorationInjectionActivity.Injector, ExplorationTestActivity.Injector, FAQListActivity.Injector,
  FAQSingleActivity.Injector, ForceNetworkTypeActivity.Injector,
  ForceNetworkTypeTestActivity.Injector, HelpActivity.Injector, HomeActivity.Injector,
  HomeFragmentTestActivity.Injector, HomeTestActivity.Injector, HtmlParserTestActivity.Injector,
  ImageRegionSelectionTestActivity.Injector, ImageViewBindingAdaptersTestActivity.Injector,
  InputInteractionViewTestActivity.Injector, LicenseListActivity.Injector,
  LicenseTextViewerActivity.Injector, ListItemLeadingMarginSpanTestActivity.Injector,
  MarkChaptersCompletedActivity.Injector, MarkChaptersCompletedTestActivity.Injector,
  MarkStoriesCompletedActivity.Injector, MarkStoriesCompletedTestActivity.Injector,
  MarkTopicsCompletedActivity.Injector, MarginBindingAdaptersTestActivity.Injector,
  MarkTopicsCompletedTestActivity.Injector, MathExpressionParserActivity.Injector,
  MyDownloadsActivity.Injector, NavigationDrawerTestActivity.Injector, OnboardingActivity.Injector,
  OngoingTopicListActivity.Injector, OptionsActivity.Injector, PinPasswordActivity.Injector,
  PoliciesActivity.Injector, PoliciesFragmentTestActivity.Injector,
  ProfileAndDeviceIdActivity.Injector, ProfileChooserActivity.Injector,
  ProfileChooserFragmentTestActivity.Injector, ProfileEditActivity.Injector,
  ProfileEditFragmentTestActivity.Injector, ProfileListActivity.Injector,
  ProfilePictureActivity.Injector, ProfileProgressActivity.Injector, ProfileRenameActivity.Injector,
  ProfileResetPinActivity.Injector, QuestionPlayerActivity.Injector,
  ReadingTextSizeActivity.Injector, RecentlyPlayedActivity.Injector, ResumeLessonActivity.Injector,
  RevisionCardActivity.Injector, SplashActivity.Injector, SplashTestActivity.Injector,
  StateAssemblerMarginBindingAdaptersTestActivity.Injector,
  StateAssemblerPaddingBindingAdaptersTestActivity.Injector, SpotlightFragmentTestActivity.Injector,
  StateFragmentTestActivity.Injector, StoryActivity.Injector,
  TestFontScaleConfigurationUtilActivity.Injector, TextViewBindingAdaptersTestActivity.Injector,
  ThirdPartyDependencyListActivity.Injector, TopicActivity.Injector,
  TopicRevisionTestActivity.Injector, TopicTestActivity.Injector,
  TopicForStoryTestActivity.Injector, ViewBindingAdaptersTestActivity.Injector,
  ViewEventLogsActivity.Injector, ViewEventLogsTestActivity.Injector, WalkthroughActivity.Injector {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setActivity(appCompatActivity: AppCompatActivity): Builder

    fun build(): ActivityComponentImpl
  }
}
