package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.options.AppLanguageActivity
import org.oppia.android.app.options.DefaultAudioActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.options.ReadingTextSizeActivity
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity
import org.oppia.android.app.profile.AddProfileActivity
import org.oppia.android.app.profile.AdminAuthActivity
import org.oppia.android.app.profile.AdminPinActivity
import org.oppia.android.app.profile.PinPasswordActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.profileprogress.ProfilePictureActivity
import org.oppia.android.app.profileprogress.ProfileProgressActivity
import org.oppia.android.app.settings.profile.ProfileEditActivity
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.settings.profile.ProfileRenameActivity
import org.oppia.android.app.settings.profile.ProfileResetPinActivity
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.testing.BindableAdapterTestActivity
import org.oppia.android.app.testing.ConceptCardFragmentTestActivity
import org.oppia.android.app.testing.DragDropTestActivity
import org.oppia.android.app.testing.ExplorationInjectionActivity
import org.oppia.android.app.testing.ExplorationTestActivity
import org.oppia.android.app.testing.HomeInjectionActivity
import org.oppia.android.app.testing.HomeTestActivity
import org.oppia.android.app.testing.HtmlParserTestActivity
import org.oppia.android.app.testing.ImageRegionSelectionTestActivity
import org.oppia.android.app.testing.NavigationDrawerTestActivity
import org.oppia.android.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.android.app.testing.StoryFragmentTestActivity
import org.oppia.android.app.testing.TestFontScaleConfigurationUtilActivity
import org.oppia.android.app.testing.TopicRevisionTestActivity
import org.oppia.android.app.testing.TopicTestActivity
import org.oppia.android.app.testing.TopicTestActivityForStory
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.app.walkthrough.WalkthroughActivity
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
  fun inject(storyFragmentTestActivity: StoryFragmentTestActivity)
  fun inject(walkthroughActivity: WalkthroughActivity)
}
