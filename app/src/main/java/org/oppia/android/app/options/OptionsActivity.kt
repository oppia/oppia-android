package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityResultBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OptionsActivityParams
import org.oppia.android.app.model.OptionsActivityStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ReadingTextSizeActivityResultBundle
import org.oppia.android.app.model.ScreenName.OPTIONS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** [String] key for mapping to [ReadingTextSizeFragment]. */
const val READING_TEXT_SIZE_FRAGMENT = "READING_TEXT_SIZE_FRAGMENT"

/** [String] key for mapping to [AppLanguageFragment]. */
const val APP_LANGUAGE_FRAGMENT = "APP_LANGUAGE_FRAGMENT"

/** [String] key for mapping to [AudioLanguageFragment]. */
const val AUDIO_LANGUAGE_FRAGMENT = "AUDIO_LANGUAGE_FRAGMENT"

/** The activity for setting user preferences. */
class OptionsActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToAppLanguageListListener,
  RouteToAudioLanguageListListener,
  RouteToReadingTextSizeListener,
  LoadReadingTextSizeListener,
  LoadAppLanguageListListener,
  LoadAudioLanguageListListener {
  @Inject
  lateinit var optionActivityPresenter: OptionsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  // used to initially load the suitable fragment in the case of multipane.
  private var isFirstOpen = true
  private lateinit var selectedFragment: String
  private lateinit var profileId: ProfileId
  private var internalProfileId: Int = -1
  private lateinit var readingTextSizeLauncher: ActivityResultLauncher<Intent>
  private lateinit var audioLanguageLauncher: ActivityResultLauncher<Intent>

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    /** Params key for OptionsActivity. */
    const val OPTIONS_ACTIVITY_PARAMS_KEY = "OptionsActivity.params"

    /** Saved state key for OptionsActivity. */
    const val OPTIONS_ACTIVITY_STATE_KEY = "OptionsActivity.state"

    /** Returns an [Intent] to start this activity. */
    fun createOptionsActivity(
      context: Context,
      profileId: ProfileId?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val args =
        OptionsActivityParams.newBuilder().setIsFromNavigationDrawer(isFromNavigationDrawer)
          .build()
      return Intent(context, OptionsActivity::class.java).apply {
        putProtoExtra(OPTIONS_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(OPTIONS_ACTIVITY)
        if (profileId != null) {
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      OPTIONS_ACTIVITY_PARAMS_KEY,
      OptionsActivityParams.getDefaultInstance()
    )
    val isFromNavigationDrawer = args?.isFromNavigationDrawer ?: false
    profileId = intent.extractCurrentUserProfileId()
    internalProfileId = profileId.internalId
    if (savedInstanceState != null) {
      isFirstOpen = false
    }
    val stateArgs =
      savedInstanceState?.getProto(
        OPTIONS_ACTIVITY_STATE_KEY,
        OptionsActivityStateBundle.getDefaultInstance()
      )

    selectedFragment = if (savedInstanceState == null) {
      READING_TEXT_SIZE_FRAGMENT
    } else {
      stateArgs?.selectedFragment as String
    }
    val extraOptionsTitle =
      stateArgs?.selectedOptionsTitle
    optionActivityPresenter.handleOnCreate(
      isFromNavigationDrawer,
      extraOptionsTitle,
      isFirstOpen,
      selectedFragment,
      internalProfileId
    )
    title = resourceHandler.getStringInLocale(R.string.menu_options)

    readingTextSizeLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == RESULT_OK && result.data != null) {
        val textSizeResults = result.data?.getProtoExtra(
          MESSAGE_READING_TEXT_SIZE_RESULTS_KEY,
          ReadingTextSizeActivityResultBundle.getDefaultInstance()
        )
        if (textSizeResults != null) {
          optionActivityPresenter.updateReadingTextSize(textSizeResults.selectedReadingTextSize)
        }
      }
    }

    audioLanguageLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == RESULT_OK && result.data != null) {
        val audioLanguage = result.data?.getProtoExtra(
          MESSAGE_AUDIO_LANGUAGE_RESULTS_KEY, AudioLanguageActivityResultBundle.getDefaultInstance()
        )?.audioLanguage
        if (audioLanguage != null) {
          optionActivityPresenter.updateAudioLanguage(audioLanguage)
        }
      }
    }
  }

  override fun routeAppLanguageList(oppiaLanguage: OppiaLanguage) {
    startActivity(
      AppLanguageActivity.createAppLanguageActivityIntent(
        this,
        oppiaLanguage,
        internalProfileId
      )
    )
  }

  override fun routeAudioLanguageList(audioLanguage: AudioLanguage) {
    val intent = AudioLanguageActivity.createAudioLanguageActivityIntent(this, audioLanguage)
    intent.decorateWithUserProfileId(profileId)
    audioLanguageLauncher.launch(intent)
  }

  override fun routeReadingTextSize(readingTextSize: ReadingTextSize) {
    readingTextSizeLauncher.launch(
      ReadingTextSizeActivity.createReadingTextSizeActivityIntent(this, readingTextSize)
    )
  }

  override fun loadReadingTextSizeFragment(textSize: ReadingTextSize) {
    selectedFragment = READING_TEXT_SIZE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.reading_text_size)
    )
    optionActivityPresenter.loadReadingTextSizeFragment(textSize)
  }

  override fun loadAppLanguageFragment(oppiaLanguage: OppiaLanguage) {
    selectedFragment = APP_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.app_language)
    )
    optionActivityPresenter.loadAppLanguageFragment(oppiaLanguage)
  }

  override fun loadAudioLanguageFragment(audioLanguage: AudioLanguage) {
    selectedFragment = AUDIO_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.audio_language)
    )
    optionActivityPresenter.loadAudioLanguageFragment(audioLanguage, profileId)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val titleTextView = findViewById<TextView>(R.id.options_activity_selected_options_title)
    val args = OptionsActivityStateBundle.newBuilder().apply {
      if (titleTextView != null) {
        selectedOptionsTitle = titleTextView.text.toString()
      }
      selectedFragment = this@OptionsActivity.selectedFragment
    }.build()
    outState.putProto(OPTIONS_ACTIVITY_STATE_KEY, args)
  }
}
