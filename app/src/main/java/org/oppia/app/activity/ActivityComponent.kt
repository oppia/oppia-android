package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.fragment.FragmentComponent
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.mydownloads.MyDownloadsActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.profile.AddProfileActivity
import org.oppia.app.profile.AdminAuthActivity
import org.oppia.app.profile.AdminPinActivity
import org.oppia.app.profile.PinPasswordActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.settings.profile.ProfileEditActivity
import org.oppia.app.settings.profile.ProfileListActivity
import org.oppia.app.settings.profile.ProfileRenameActivity
import org.oppia.app.settings.profile.ProfileResetPinActivity
import org.oppia.app.splash.SplashActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.story.testing.StoryFragmentTestActivity
import org.oppia.app.testing.AudioFragmentTestActivity
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.app.testing.ContentCardTestActivity
import org.oppia.app.testing.RecentlyPlayedFragmentTestActivity
import org.oppia.app.testing.ExplorationInjectionActivity
import org.oppia.app.testing.ExplorationTestActivity
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.app.testing.NavigationDrawerTestActivity
import org.oppia.app.testing.TopicTestActivity
import org.oppia.app.testing.TopicTestActivityForStory
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.topic.reviewcard.ReviewCardActivity
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
  fun inject(audioFragmentTestActivity: AudioFragmentTestActivity)
  fun inject(bindableAdapterTestActivity: BindableAdapterTestActivity)
  fun inject(conceptCardFragmentTestActivity: ConceptCardFragmentTestActivity)
  fun inject(contentCardTestActivity: ContentCardTestActivity)
  fun inject(explorationActivity: ExplorationActivity)
  fun inject(explorationInjectionActivity: ExplorationInjectionActivity)
  fun inject(explorationTestActivity: ExplorationTestActivity)
  fun inject(helpActivity: HelpActivity)
  fun inject(homeActivity: HomeActivity)
  fun inject(htmlParserTestActivity: HtmlParserTestActivity)
  fun inject(myDownloadsActivity: MyDownloadsActivity)
  fun inject(navigationDrawerTestActivity: NavigationDrawerTestActivity)
  fun inject(onboardingActivity: OnboardingActivity)
  fun inject(pinPasswordActivity: PinPasswordActivity)
  fun inject(profileActivity: ProfileActivity)
  fun inject(questionPlayerActivity: QuestionPlayerActivity)
  fun inject(profileEditActivity: ProfileEditActivity)
  fun inject(profileListActivity: ProfileListActivity)
  fun inject(profileRenameActivity: ProfileRenameActivity)
  fun inject(profileResetPinActivity: ProfileResetPinActivity)
  fun inject(recentlyPlayedActivity: RecentlyPlayedActivity)
  fun inject(recentlyPlayedFragmentTestActivity: RecentlyPlayedFragmentTestActivity)
  fun inject(reviewCardActivity: ReviewCardActivity)
  fun inject(splashActivity: SplashActivity)
  fun inject(storyActivity: StoryActivity)
  fun inject(topicActivity: TopicActivity)
  fun inject(topicTestActivity: TopicTestActivity)
  fun inject(topicTestActivityForStory: TopicTestActivityForStory)
  fun inject(storyFragmentTestActivity: StoryFragmentTestActivity)
  fun inject(walkthroughActivity: WalkthroughActivity)
}
