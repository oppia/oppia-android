package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
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
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.LicenseTextViewerActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.AdminAuthActivityParams
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.DestinationScreen.DestinationScreenCase
import org.oppia.android.app.model.TopicActivityParams
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
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.testing.AdministratorControlsFragmentTestActivity
import org.oppia.android.app.testing.AppCompatCheckBoxBindingAdaptersTestActivity
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.testing.BindableAdapterTestActivity
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
import org.oppia.android.app.testing.LessonThumbnailImageViewTestActivity
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
import org.oppia.android.app.testing.ViewBindingAdaptersTestActivity
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.app.walkthrough.WalkthroughActivity

/** Module to bind destination screens to navigable activity routes. */
@Module
class ActivityRouterModule {
  // TODO(#1720): Post-Bazel (and maybe Hilt), decompose this into per-activity modules to simplify
  //  dependencies.
  // TODO(#4986): Move the usage of protos into the individual activities & migrate all navigation
  //  flow over to using the router.

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.APP_VERSION_ACTIVITY_PARAMS)
  fun provideAppVersionActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        AppVersionActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_AND_DEVICE_ID_ACTIVITY_PARAMS)
  fun provideProfileAndDeviceIdActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ProfileAndDeviceIdActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.COMPLETED_STORY_LIST_ACTIVITY_PARAMS)
  fun provideCompletedStoryListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return CompletedStoryListActivity.createIntent(
          context, destinationScreen.completedStoryListActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.DEVELOPER_OPTIONS_ACTIVITY_PARAMS)
  fun provideDeveloperOptionsActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return DeveloperOptionsActivity.createIntent(
          context, destinationScreen.developerOptionsActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.FORCE_NETWORK_TYPE_ACTIVITY_PARAMS)
  fun provideForceNetworkTypeActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ForceNetworkTypeActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.FORCE_NETWORK_TYPE_TEST_ACTIVITY_PARAMS)
  fun provideForceNetworkTypeTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ForceNetworkTypeTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_CHAPTERS_COMPLETED_ACTIVITY_PARAMS)
  fun provideMarkChaptersCompletedActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkChaptersCompletedActivity.createIntent(
          context,
          destinationScreen.markChaptersCompletedActivityParams.internalProfileId,
          destinationScreen.markChaptersCompletedActivityParams.showConfirmationNotice
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_CHAPTERS_COMPLETED_TEST_ACTIVITY_PARAMS)
  fun provideMarkChaptersCompletedTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkChaptersCompletedTestActivity.createIntent(
          context,
          destinationScreen.markChaptersCompletedTestActivityParams.internalProfileId,
          destinationScreen.markChaptersCompletedTestActivityParams.showConfirmationNotice
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_STORIES_COMPLETED_ACTIVITY_PARAMS)
  fun provideMarkStoriesCompletedActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkStoriesCompletedActivity.createIntent(
          context, destinationScreen.markStoriesCompletedActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_STORIES_COMPLETED_TEST_ACTIVITY_PARAMS)
  fun provideMarkStoriesCompletedTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkStoriesCompletedTestActivity.createIntent(
          context, destinationScreen.markStoriesCompletedTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_TOPICS_COMPLETED_ACTIVITY_PARAMS)
  fun provideMarkTopicsCompletedActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkTopicsCompletedActivity.createIntent(
          context, destinationScreen.markTopicsCompletedActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARK_TOPICS_COMPLETED_TEST_ACTIVITY_PARAMS)
  fun provideMarkTopicsCompletedTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MarkTopicsCompletedTestActivity.createIntent(
          context, destinationScreen.markTopicsCompletedTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MATH_EXPRESSION_PARSER_ACTIVITY_PARAMS)
  fun provideMathExpressionParserActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        MathExpressionParserActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.DEVELOPER_OPTIONS_TEST_ACTIVITY_PARAMS)
  fun provideDeveloperOptionsTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return DeveloperOptionsTestActivity.createIntent(
          context, destinationScreen.developerOptionsTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.VIEW_EVENT_LOGS_ACTIVITY_PARAMS)
  fun provideViewEventLogsActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ViewEventLogsActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.VIEW_EVENT_LOGS_TEST_ACTIVITY_PARAMS)
  fun provideViewEventLogsTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ViewEventLogsTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.HELP_ACTIVITY_PARAMS)
  fun provideHelpActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return HelpActivity.createIntent(
          context,
          destinationScreen.helpActivityParams.internalProfileId,
          destinationScreen.helpActivityParams.isFromNavigationDrawer
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.FAQ_LIST_ACTIVITY_PARAMS)
  fun provideFaqListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        FAQListActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.FAQ_SINGLE_ACTIVITY_PARAMS)
  fun provideFaqSingleActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return FAQSingleActivity.createIntent(
          context,
          destinationScreen.faqSingleActivityParams.questionText,
          destinationScreen.faqSingleActivityParams.answerText
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.LICENSE_LIST_ACTIVITY_PARAMS)
  fun provideLicenseListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return LicenseListActivity.createIntent(
          context, destinationScreen.licenseListActivityParams.dependencyIndex
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.LICENSE_TEXT_VIEWER_ACTIVITY_PARAMS)
  fun provideLicenseTextViewerActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return LicenseTextViewerActivity.createIntent(
          context,
          destinationScreen.licenseTextViewerActivityParams.dependencyIndex,
          destinationScreen.licenseTextViewerActivityParams.licenseIndex
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY_PARAMS)
  fun provideThirdPartyDependencyListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ThirdPartyDependencyListActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.HOME_ACTIVITY_PARAMS)
  fun provideHomeActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return HomeActivity.createIntent(
          context, destinationScreen.homeActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS)
  fun provideRecentlyPlayedActivityRoute(): Route {
    return object : Route {
      override fun createIntent(
        context: Context,
        destinationScreen: DestinationScreen
      ): Intent {
        return RecentlyPlayedActivity.createIntent(
          context,
          destinationScreen.recentlyPlayedActivityParams
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MY_DOWNLOADS_ACTIVITY_PARAMS)
  fun provideMyDownloadsActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return MyDownloadsActivity.createIntent(
          context, destinationScreen.myDownloadsActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ONBOARDING_ACTIVITY_PARAMS)
  fun provideOnboardingActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        OnboardingActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ONGOING_TOPIC_LIST_ACTIVITY_PARAMS)
  fun provideOngoingTopicListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return OngoingTopicListActivity.createIntent(
          context, destinationScreen.ongoingTopicListActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.APP_LANGUAGE_ACTIVITY_PARAMS)
  fun provideAppLanguageActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AppLanguageActivity.createIntent(
          context, destinationScreen.appLanguageActivityParams.summaryValue
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.AUDIO_LANGUAGE_ACTIVITY_PARAMS)
  fun provideAudioLanguageActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AudioLanguageActivity.createIntent(
          context, destinationScreen.audioLanguageActivityParams.audioLanguage
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.OPTIONS_ACTIVITY_PARAMS)
  fun provideOptionsActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return OptionsActivity.createIntent(
          context,
          destinationScreen.optionsActivityParams.internalProfileId,
          destinationScreen.optionsActivityParams.isFromNavigationDrawer
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.READING_TEXT_SIZE_ACTIVITY_PARAMS)
  fun provideReadingTextSizeActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ReadingTextSizeActivity.createIntent(
          context, destinationScreen.readingTextSizeActivityParams.readingTextSize
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.EXPLORATION_ACTIVITY_PARAMS)
  fun provideExplorationActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ExplorationActivity.createIntent(context, destinationScreen.explorationActivityParams)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.STATE_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideStateFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return StateFragmentTestActivity.createIntent(
          context,
          destinationScreen.stateFragmentTestActivityParams.internalProfileId,
          destinationScreen.stateFragmentTestActivityParams.topicId,
          destinationScreen.stateFragmentTestActivityParams.storyId,
          destinationScreen.stateFragmentTestActivityParams.explorationId,
          destinationScreen.stateFragmentTestActivityParams.shouldSavePartialProgress
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.POLICIES_ACTIVITY_PARAMS)
  fun providePoliciesActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        PoliciesActivity.createIntent(context, destinationScreen.policiesActivityParams.policyPage)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ADD_PROFILE_ACTIVITY_PARAMS)
  fun provideAddProfileActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AddProfileActivity.createIntent(
          context, destinationScreen.addProfileActivityParams.colorRgb
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ADMIN_AUTH_ACTIVITY_PARAMS)
  fun provideAdminAuthActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        val adminAuthValue = when (destinationScreen.adminAuthActivityParams.adminAuth) {
          AdminAuthActivityParams.AdminAuth.PROFILE_ADMIN_CONTROLS -> 0
          AdminAuthActivityParams.AdminAuth.PROFILE_ADD_PROFILE -> 1
          AdminAuthActivityParams.AdminAuth.ADMIN_AUTH_UNSPECIFIED,
          AdminAuthActivityParams.AdminAuth.UNRECOGNIZED, null ->
            error("Invalid configuration provided: $destinationScreen.")
        }
        return AdminAuthActivity.createIntent(
          context,
          destinationScreen.adminAuthActivityParams.adminPin,
          destinationScreen.adminAuthActivityParams.internalProfileId,
          destinationScreen.adminAuthActivityParams.colorRgb,
          adminAuthValue
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ADMIN_PIN_ACTIVITY_PARAMS)
  fun provideAdminPinActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        val adminAuthValue = when (destinationScreen.adminPinActivityParams.adminAuth) {
          AdminAuthActivityParams.AdminAuth.PROFILE_ADMIN_CONTROLS -> 0
          AdminAuthActivityParams.AdminAuth.PROFILE_ADD_PROFILE -> 1
          AdminAuthActivityParams.AdminAuth.ADMIN_AUTH_UNSPECIFIED,
          AdminAuthActivityParams.AdminAuth.UNRECOGNIZED, null ->
            error("Invalid configuration provided: $destinationScreen.")
        }
        return AdminPinActivity.createIntent(
          context,
          destinationScreen.adminPinActivityParams.internalProfileId,
          destinationScreen.adminPinActivityParams.colorRgb,
          adminAuthValue
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PIN_PASSWORD_ACTIVITY_PARAMS)
  fun providePinPasswordActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return PinPasswordActivity.createIntent(
          context,
          destinationScreen.pinPasswordActivityParams.adminPin,
          destinationScreen.pinPasswordActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_CHOOSER_ACTIVITY_PARAMS)
  fun provideProfileChooserActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ProfileChooserActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_PICTURE_ACTIVITY_PARAMS)
  fun provideProfilePictureActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfilePictureActivity.createIntent(
          context, destinationScreen.profilePictureActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_PROGRESS_ACTIVITY_PARAMS)
  fun provideProfileProgressActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileProgressActivity.createIntent(
          context, destinationScreen.profileProgressActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.RESUME_LESSON_ACTIVITY_PARAMS)
  fun provideResumeLessonActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ResumeLessonActivity.createIntent(context, destinationScreen.resumeLessonActivityParams)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_EDIT_ACTIVITY_PARAMS)
  fun provideProfileEditActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileEditActivity.createIntent(
          context,
          destinationScreen.profileEditActivityParams.internalProfileId,
          destinationScreen.profileEditActivityParams.isMultipane,
          destinationScreen.profileEditActivityParams.clearTop
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_LIST_ACTIVITY_PARAMS)
  fun provideProfileListActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileListActivity.createIntent(
          context, destinationScreen.profileListActivityParams.clearTop
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_RENAME_ACTIVITY_PARAMS)
  fun provideProfileRenameActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileRenameActivity.createIntent(
          context, destinationScreen.profileRenameActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_RESET_PIN_ACTIVITY_PARAMS)
  fun provideProfileResetPinActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileResetPinActivity.createIntent(
          context,
          destinationScreen.profileResetPinActivityParams.internalProfileId,
          destinationScreen.profileResetPinActivityParams.isAdmin
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.SPLASH_ACTIVITY_PARAMS)
  fun provideSplashActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        error("Cannot explicitly navigate to SplashActivity.")
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.STORY_ACTIVITY_PARAMS)
  fun provideStoryActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return StoryActivity.createIntent(
          context,
          destinationScreen.storyActivityParams.internalProfileId,
          destinationScreen.storyActivityParams.topicId,
          destinationScreen.storyActivityParams.storyId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ADMINISTRATOR_CONTROLS_ACTIVITY_PARAMS)
  fun provideAdministratorControlsActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AdministratorControlsActivity.createIntent(
          context,
          destinationScreen.administratorControlsActivityParams.internalProfileId,
          destinationScreen.administratorControlsActivityParams.clearTop
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.ADMINISTRATOR_CONTROLS_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideAdministratorControlsFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AdministratorControlsFragmentTestActivity.createIntent(
          context,
          destinationScreen.administratorControlsFragmentTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.APP_COMPAT_CHECK_BOX_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideAppCompatCheckBoxBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        AppCompatCheckBoxBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.AUDIO_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideAudioFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return AudioFragmentTestActivity.createIntent(
          context, destinationScreen.audioFragmentTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.BINDABLE_ADAPTER_TEST_ACTIVITY_PARAMS)
  fun provideBindableAdapterTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        BindableAdapterTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.CIRCULAR_PROGRESS_INDICATOR_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideCircularProgressIndicatorAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        CircularProgressIndicatorAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.CONCEPT_CARD_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideConceptCardFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ConceptCardFragmentTestActivity.createIntent(
          context, destinationScreen.conceptCardFragmentTestActivityParams.profileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.DRAG_DROP_TEST_ACTIVITY_PARAMS)
  fun provideDragDropTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        DragDropTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.DRAWABLE_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideDrawableBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        DrawableBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.EXPLORATION_INJECTION_ACTIVITY_PARAMS)
  fun provideExplorationInjectionActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ExplorationInjectionActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.EXPLORATION_TEST_ACTIVITY_PARAMS)
  fun provideExplorationTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ExplorationTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.HOME_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideHomeFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        HomeFragmentTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.HOME_TEST_ACTIVITY_PARAMS)
  fun provideHomeTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        HomeTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.HTML_PARSER_TEST_ACTIVITY_PARAMS)
  fun provideHtmlParserTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        HtmlParserTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.IMAGE_REGION_SELECTION_TEST_ACTIVITY_PARAMS)
  fun provideImageRegionSelectionTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ImageRegionSelectionTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.IMAGE_VIEW_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideImageViewBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ImageViewBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.INPUT_INTERACTION_VIEW_TEST_ACTIVITY_PARAMS)
  fun provideInputInteractionViewTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return InputInteractionViewTestActivity.createIntent(
          context, destinationScreen.inputInteractionViewTestActivityParams
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.LESSON_THUMBNAIL_IMAGE_VIEW_TEST_ACTIVITY_PARAMS)
  fun provideLessonThumbnailImageViewTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        LessonThumbnailImageViewTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.LIST_ITEM_LEADING_MARGIN_SPAN_TEST_ACTIVITY_PARAMS)
  fun provideListItemLeadingMarginSpanTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ListItemLeadingMarginSpanTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.MARGIN_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideMarginBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        MarginBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.NAVIGATION_DRAWER_TEST_ACTIVITY_PARAMS)
  fun provideNavigationDrawerTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return NavigationDrawerTestActivity.createIntent(
          context, destinationScreen.navigationDrawerTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.POLICIES_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun providePoliciesFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return PoliciesFragmentTestActivity.createIntent(
          context, destinationScreen.policiesFragmentTestActivityParams.policyPage
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_CHOOSER_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideProfileChooserFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ProfileChooserFragmentTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.PROFILE_EDIT_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideProfileEditFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return ProfileEditFragmentTestActivity.createIntent(
          context, destinationScreen.profileEditFragmentTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.SPLASH_TEST_ACTIVITY_PARAMS)
  fun provideSplashTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        SplashTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.SPOTLIGHT_FRAGMENT_TEST_ACTIVITY_PARAMS)
  fun provideSpotlightFragmentTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return SpotlightFragmentTestActivity.createIntent(
          context, destinationScreen.spotlightFragmentTestActivityParams.internalProfileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.STATE_ASSEMBLER_MARGIN_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideStateAssemblerMarginBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        StateAssemblerMarginBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.STATE_ASSEMBLER_PADDING_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideStateAssemblerPaddingBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        StateAssemblerPaddingBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_PARAMS)
  fun provideTestFontScaleConfigurationUtilActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return TestFontScaleConfigurationUtilActivity.createIntent(
          context, destinationScreen.testFontScaleConfigurationUtilActivityParams.readingTextSize
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.TEXT_VIEW_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideTextViewBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        TextViewBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.TOPIC_REVISION_TEST_ACTIVITY_PARAMS)
  fun provideTopicRevisionTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        TopicRevisionTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.TOPIC_TEST_ACTIVITY_PARAMS)
  fun provideTopicTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        TopicTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.VIEW_BINDING_ADAPTERS_TEST_ACTIVITY_PARAMS)
  fun provideViewBindingAdaptersTestActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent =
        ViewBindingAdaptersTestActivity.createIntent(context)
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.TOPIC_ACTIVITY_PARAMS)
  fun provideTopicActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return when (destinationScreen.topicActivityParams.routeModeCase) {
          TopicActivityParams.RouteModeCase.STORY_LIST -> {
            TopicActivity.createIntent(
              context,
              destinationScreen.topicActivityParams.internalProfileId,
              destinationScreen.topicActivityParams.topicId
            )
          }
          TopicActivityParams.RouteModeCase.SPECIFIC_STORY_ID -> {
            TopicActivity.createIntent(
              context,
              destinationScreen.topicActivityParams.internalProfileId,
              destinationScreen.topicActivityParams.topicId,
              destinationScreen.topicActivityParams.specificStoryId
            )
          }
          TopicActivityParams.RouteModeCase.ROUTEMODE_NOT_SET, null ->
            error("Invalid configuration provided: $destinationScreen.")
        }
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.QUESTION_PLAYER_ACTIVITY_PARAMS)
  fun provideQuestionPlayerActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return QuestionPlayerActivity.createIntent(
          context,
          ArrayList(destinationScreen.questionPlayerActivityParams.skillIdsList),
          destinationScreen.questionPlayerActivityParams.profileId
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.REVISION_CARD_ACTIVITY_PARAMS)
  fun provideRevisionCardActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return RevisionCardActivity.createIntent(
          context,
          destinationScreen.revisionCardActivityParams.internalProfileId,
          destinationScreen.revisionCardActivityParams.topicId,
          destinationScreen.revisionCardActivityParams.subtopicIndex,
          destinationScreen.revisionCardActivityParams.subtopicListSize
        )
      }
    }
  }

  @Provides
  @IntoMap
  @RouteKey(DestinationScreenCase.WALKTHROUGH_ACTIVITY_PARAMS)
  fun provideWalkthroughActivityRoute(): Route {
    return object : Route {
      override fun createIntent(context: Context, destinationScreen: DestinationScreen): Intent {
        return WalkthroughActivity.createIntent(
          context, destinationScreen.walkthroughActivityParams.internalProfileId
        )
      }
    }
  }
}
