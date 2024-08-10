package org.oppia.android.app.player.state.itemviewmodel

import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.ImageInteractionState
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.DefaultRegionClickedEvent
import org.oppia.android.app.utility.NamedRegionClickedEvent
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.app.utility.RegionClickedEvent
import javax.inject.Inject

/** [StateItemViewModel] for image region selection. */
class ImageRegionSelectionInteractionViewModel private constructor(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  userAnswerState: UserAnswerState
) : StateItemViewModel(ViewType.IMAGE_REGION_SELECTION_INTERACTION),
  InteractionAnswerHandler,
  OnClickableAreaClickedListener {
  private var pendingAnswerError: String? = null
  var errorMessage = ObservableField<String>("")
  private var isDefaultRegionClicked = false
  var answerText: CharSequence = ""
  private var dafaultRegionCoordinates: Point2d? = null
  val selectableRegions: List<ImageWithRegions.LabeledRegion> by lazy {
    val schemaObject = interaction.customizationArgsMap["imageAndRegions"]
    schemaObject?.customSchemaValue?.imageWithRegions?.labelRegionsList ?: listOf()
  }

  val observableUserAnswrerState by lazy {
    ObservableField(userAnswerState)
  }

  private var answerErrorCetegory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR

  val imagePath: String by lazy {
    val schemaObject = interaction.customizationArgsMap["imageAndRegions"]
    schemaObject?.customSchemaValue?.imageWithRegions?.imagePath ?: ""
  }

  val isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError = pendingAnswerError,
            inputAnswerAvailable = true // Allow blank answer submission.
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    errorMessage.addOnPropertyChangedCallback(callback)

    // Initializing with default values so that submit button is enabled by default.
    errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      pendingAnswerError = null,
      inputAnswerAvailable = true
    )
    checkPendingAnswerError(userAnswerState.answerErrorCategory)
  }

  override fun onClickableAreaTouched(region: RegionClickedEvent) {
    when (region) {
      is DefaultRegionClickedEvent -> {
        dafaultRegionCoordinates = Point2d.newBuilder().apply {
          x = region.x
          y = region.y
        }.build()
        answerText = ""
        isAnswerAvailable.set(false)
        isDefaultRegionClicked = true
      }
      is NamedRegionClickedEvent -> {
        dafaultRegionCoordinates = null
        answerText = region.regionLabel
        isAnswerAvailable.set(true)
      }
    }
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
  }

  /** It checks the pending error for the current image region input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    answerErrorCetegory = category
    when (category) {
      AnswerErrorCategory.REAL_TIME -> {
        pendingAnswerError = null
      }

      AnswerErrorCategory.SUBMIT_TIME -> {
        if (answerText.isNotEmpty() || isDefaultRegionClicked) {
          pendingAnswerError = null
        } else {
          pendingAnswerError =
            ImageRegionParsingUiError.createFromParsingError(
              getSubmitTimeError(answerText.toString())
            ).getErrorMessageFromStringRes(resourceHandler)
        }
      }
      else -> {}
    }

    errorMessage.set(pendingAnswerError)
    return pendingAnswerError
  }

  override fun getUserAnswerState(): UserAnswerState {
    return UserAnswerState.newBuilder().apply {
      if (answerText.isNotEmpty() || dafaultRegionCoordinates != null) {
        this.imageInteractionState = ImageInteractionState.newBuilder().apply {
          if (answerText.isNotEmpty()) {
            this.imageLabel = answerText.toString()
          } else {
            this.defaultRegionCoordinates = dafaultRegionCoordinates
          }
        }.build()
      }
      this.answerErrorCategory = answerErrorCetegory
    }.build()
  }

  override fun getPendingAnswer(): UserAnswer {
    // Resetting Observable UserAnswerState to its default instance to ensure that
    // the ImageRegionSelectionInteractionView reflects no image region selection.
    // This is necessary because ImageRegionSelectionInteractionView is not recreated every time
    // the user submits an answer, causing it to retain the old UserAnswerState.
    observableUserAnswrerState.set(UserAnswerState.getDefaultInstance())

    return UserAnswer.newBuilder().apply {
      val answerTextString = answerText.toString()
      answer = InteractionObject.newBuilder().apply {
        clickOnImage = parseClickOnImage(answerTextString)
      }.build()
      plainAnswer = resourceHandler.getStringInLocaleWithWrapping(
        R.string.image_interaction_answer_text,
        answerTextString
      )
      this.writtenTranslationContext =
        this@ImageRegionSelectionInteractionViewModel.writtenTranslationContext
    }.build()
  }

  private fun parseClickOnImage(answerTextString: String): ClickOnImage {
    val region = selectableRegions.find { it.label == answerTextString }
    return ClickOnImage.newBuilder()
      // The object supports multiple regions in an answer, but neither web nor Android supports this.
      .addClickedRegions(region?.label ?: "")
      .build()
  }

  /**
   * Returns [ImageRegionParsingError.EMPTY_INPUT] if input is blank, or
   * [TextParsingError.VALID] if input is not empty.
   */
  fun getSubmitTimeError(text: String): ImageRegionParsingError {
    if (text.isNullOrBlank()) {
      return ImageRegionParsingError.EMPTY_INPUT
    }
    return ImageRegionParsingError.VALID
  }

  /** Represents errors that can occur when parsing region name. */
  enum class ImageRegionParsingError {

    /** Indicates that the considered string is a valid. */
    VALID,

    /** Indicates that the input text was empty. */
    EMPTY_INPUT
  }

  enum class ImageRegionParsingUiError(@StringRes private var error: Int?) {
    /** Corresponds to [ImageRegionParsingError.VALID]. */
    VALID(error = null),

    /** Corresponds to [ImageRegionParsingError.EMPTY_INPUT]. */
    EMPTY_INPUT(error = R.string.image_error_empty_input);

    /**
     * Returns the string corresponding to this error's string resources, or null if there is none.
     */
    fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
      error?.let(resourceHandler::getStringInLocale)

    companion object {
      /**
       * Returns the [ImageRegionParsingUiError] corresponding to the specified [ImageRegionParsingError].
       */
      fun createFromParsingError(parsingError: ImageRegionParsingError): ImageRegionParsingUiError {
        return when (parsingError) {

          ImageRegionParsingError.VALID -> VALID

          ImageRegionParsingError.EMPTY_INPUT -> EMPTY_INPUT
        }
      }
    }
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler
  ) : InteractionItemFactory {

    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState
    ): StateItemViewModel {
      return ImageRegionSelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler,
        userAnswerState
      )
    }
  }
}
