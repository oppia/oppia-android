package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.app.completedstorylist.CompletedStoryListActivity
import org.oppia.app.fragment.FragmentComponent
import org.oppia.app.help.HelpActivity
import org.oppia.app.help.faq.FAQListActivity
import org.oppia.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.mydownloads.MyDownloadsActivity
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.app.options.AppLanguageActivity
import org.oppia.app.options.DefaultAudioActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.options.ReadingTextSizeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.profile.AddProfileActivity
import org.oppia.app.profile.AdminAuthActivity
import org.oppia.app.profile.AdminPinActivity
import org.oppia.app.profile.PinPasswordActivity
import org.oppia.app.profile.ProfileChooserActivity
import org.oppia.app.profileprogress.ProfilePictureActivity
import org.oppia.app.profileprogress.ProfileProgressActivity
import org.oppia.app.settings.profile.ProfileEditActivity
import org.oppia.app.settings.profile.ProfileListActivity
import org.oppia.app.settings.profile.ProfileRenameActivity
import org.oppia.app.settings.profile.ProfileResetPinActivity
import org.oppia.app.splash.SplashActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.testing.AudioFragmentTestActivity
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.app.testing.DragDropTestActivity
import org.oppia.app.testing.ExplorationInjectionActivity
import org.oppia.app.testing.ExplorationTestActivity
import org.oppia.app.testing.HomeInjectionActivity
import org.oppia.app.testing.HomeTestActivity
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.app.testing.ImageRegionSelectionTestActivity
import org.oppia.app.testing.NavigationDrawerTestActivity
import org.oppia.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.app.testing.TestFontScaleConfigurationUtilActivity
import org.oppia.app.testing.TopicRevisionTestActivity
import org.oppia.app.testing.TopicTestActivity
import org.oppia.app.testing.TopicTestActivityForStory
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.topic.revisioncard.RevisionCardActivity
import org.oppia.app.walkthrough.WalkthroughActivity
import javax.inject.Provider

/** Root subcomponent for all activities. */
@Subcomponent(modules = [ActivityModule::class])
@ActivityScope
interface ActivityComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setActivity(appCompatActivity: AppCompatActivity): Builder

    fun build(): ActivityComponent
  }

  fun getFragmentComponentBuilderProvider(): Provider<FragmentComponent.Builder>

  fun inject(addProfileActivity: AddProfileActivity)
  fun inject(adminAuthActivity: AdminAuthActivity)
  fun inject(administratorControlsActivity: AdministratorControlsActivity)
  fun inject(adminPinActivity: AdminPinActivity)
  fun inject(appLanguageActivity: AppLanguageActivity)
  fun inject(appVersionActivity: AppVersionActivity)
  fun inject(audioFragmentTestActivity: AudioFragmentTestActivity)
  fun inject(bindableAdapterTestActivity: BindableAdapterTestActivity)
  fun inject(completedStoryListActivity: CompletedStoryListActivity)
  fun inject(conceptCardFragmentTestActivity: ConceptCardFragmentTestActivity)
  fun inject(defaultAudioActivity: DefaultAudioActivity)
  fun inject(dragDropTestActivity: DragDropTestActivity)
  fun inject(explorationActivity: ExplorationActivity)
  fun inject(explorationInjectionActivity: ExplorationInjectionActivity)
  fun inject(explorationTestActivity: ExplorationTestActivity)
  fun inject(faqListActivity: FAQListActivity)
  fun inject(faqSingleActivity: FAQSingleActivity)
  fun inject(testFontScaleConfigurationUtilActivity: TestFontScaleConfigurationUtilActivity)
  fun inject(helpActivity: HelpActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(homeInjectionActivity: HomeInjectionActivity)
  fun inject(homeTestActivity: HomeTestActivity)
  fun inject(htmlParserTestActivity: HtmlParserTestActivity)
  fun inject(imageRegionSelectionTestActivity: ImageRegionSelectionTestActivity)
  fun inject(myDownloadsActivity: MyDownloadsActivity)
  fun inject(navigationDrawerTestActivity: NavigationDrawerTestActivity)
  fun inject(onboardingActivity: OnboardingActivity)
  fun inject(ongoingTopicListActivity: OngoingTopicListActivity)
  fun inject(optionActivity: OptionsActivity)
  fun inject(pinPasswordActivity: PinPasswordActivity)
  fun inject(profileChooserActivity: ProfileChooserActivity)
  fun inject(questionPlayerActivity: QuestionPlayerActivity)
  fun inject(profileChooserFragmentTestActivity: ProfileChooserFragmentTestActivity)
  fun inject(profileEditActivity: ProfileEditActivity)
  fun inject(profileListActivity: ProfileListActivity)
  fun inject(profilePictureActivity: ProfilePictureActivity)
  fun inject(profileProgressActivity: ProfileProgressActivity)
  fun inject(profileRenameActivity: ProfileRenameActivity)
  fun inject(profileResetPinActivity: ProfileResetPinActivity)
  fun inject(recentlyPlayedActivity: RecentlyPlayedActivity)
  fun inject(revisionCardActivity: RevisionCardActivity)
  fun inject(splashActivity: SplashActivity)
  fun inject(stateFragmentTestActivity: StateFragmentTestActivity)
  fun inject(storyActivity: StoryActivity)
  fun inject(readingTextSizeActivity: ReadingTextSizeActivity)
  fun inject(topicActivity: TopicActivity)
  fun inject(topicRevisionTestActivity: TopicRevisionTestActivity)
  fun inject(topicTestActivity: TopicTestActivity)
  fun inject(topicTestActivityForStory: TopicTestActivityForStory)
  fun inject(walkthroughActivity: WalkthroughActivity)
}
