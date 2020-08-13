package org.oppia.domain.classify.rules.imageClickInput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.ClickOnImage
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Point2d
import org.oppia.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [ImageClickInputIsInRegionRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ImageClickInputIsInRegionRuleClassifierProviderTest {
  private val NON_NEGATIVE_VALUE_0 = createNonNegativeInt(value = 0)
  private val ITEM_REGION_A = "a"
  private val ITEM_REGION_B = "b"
  private val ITEM_REGION_C = "c"
  private val ITEM_REGION_D = "d"
  private val ITEM_POINT_1 = createPoint2d(0.5f, 0.5f)

  private val IMAGE_REGION_ABC_POSITION_1 =
    createClickOnImage(ITEM_POINT_1, listOf(ITEM_REGION_A, ITEM_REGION_B, ITEM_REGION_C))

  @Inject
  internal lateinit var imageClickInputIsInRegionClassifierProvider:
    ImageClickInputIsInRegionRuleClassifierProvider

  private val isInRegionClassifierProvider: RuleClassifier by lazy {
    imageClickInputIsInRegionClassifierProvider.createRuleClassifier()
  }

  @Test
  fun testAnswer_testClickOnImage_regionA_bothValuesMatch() {
    val inputs = mapOf("x" to createString(ITEM_REGION_A))

    val matches =
      isInRegionClassifierProvider.matches(answer = IMAGE_REGION_ABC_POSITION_1, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testClickOnImage_regionC_bothValuesMatch() {
    val inputs = mapOf("x" to createString(ITEM_REGION_C))

    val matches =
      isInRegionClassifierProvider.matches(answer = IMAGE_REGION_ABC_POSITION_1, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testClickOnImage_regionD_bothValuesDoNoMatch() {
    val inputs = mapOf("x" to createString(ITEM_REGION_D))

    val matches =
      isInRegionClassifierProvider.matches(answer = IMAGE_REGION_ABC_POSITION_1, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_0)

    val exception = assertThrows(IllegalStateException::class) {
      isInRegionClassifierProvider.matches(
        answer = IMAGE_REGION_ABC_POSITION_1,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type NORMALIZED_STRING not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testAnswer_testString_missingInputX_throwsException() {
    val inputs = mapOf("y" to createString(ITEM_REGION_A))

    val exception = assertThrows(IllegalStateException::class) {
      isInRegionClassifierProvider.matches(
        answer = IMAGE_REGION_ABC_POSITION_1,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun createClickOnImage(point: Point2d, regions: List<String>): InteractionObject {
    val clickOnImage = ClickOnImage.newBuilder()
      .addAllClickedRegions(regions)
      .setClickPosition(point)
      .build()

    return InteractionObject.newBuilder().setClickOnImage(clickOnImage).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun createPoint2d(x: Float, y: Float): Point2d {
    return Point2d.newBuilder().setX(x).setY(y).build()
  }

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerImageClickInputIsInRegionRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move to a common test library.
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ImageClickInputIsInRegionRuleClassifierProviderTest)
  }
}
