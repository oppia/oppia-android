package org.oppia.android.domain.hintsandsolution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [HelpIndex] extensions. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class HelpIndexExtensionsTest {
  @Suppress("PrivatePropertyName")
  private val HINT_LIST_OF_SIZE_2 = listOf(Hint.getDefaultInstance(), Hint.getDefaultInstance())

  @Suppress("PrivatePropertyName")
  private val HINT_LIST_OF_SIZE_3 = listOf(
    Hint.getDefaultInstance(), Hint.getDefaultInstance(), Hint.getDefaultInstance()
  )

  @Test
  fun testIsHintRevealed_defaultHelpIndex_returnsFalse() {
    val helpIndex = HelpIndex.getDefaultInstance()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, hintList = listOf())

    // An unknown or initial HelpIndex state means no hints have yet been viewed.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex0_index0_hintListSize3_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_3)

    // This hint is available, but hasn't yet been viewed.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex1_index0_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_3)

    // The next hint being available implies the previous must have been seen.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex1_index1_hintListSize3_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_3)

    // This hint is available to view, but hasn't been yet.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex2_index0_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 2
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_3)

    // Two hints from now is available, so this one must have been viewed.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex2_index1_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 2
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_3)

    // The next hint is available, so this one must have been viewed.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_availableHintIndex2_index2_hintListSize3_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 2
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 2, HINT_LIST_OF_SIZE_3)

    // This hint is available to view, but hasn't yet been viewed.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_lastRevealedHintIndex0_index0_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_3)

    // The revealed index matches the one being checked.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_lastRevealedHintIndex0_index1_hintListSize3_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_3)

    // This hint hasn't yet been revealed, but the previous one has.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_lastRevealedHintIndex1_index0_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_3)

    // The next hint has been revealed.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_lastRevealedHintIndex1_index1_hintListSize3_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_3)

    // This hint has been revealed.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_showSolution_index0_hintListSize2_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_2)

    // A viewable solution means all previous hints must have been seen.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_showSolution_index1_hintListSize2_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_2)

    // A viewable solution means all previous hints must have been seen.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_showSolution_index0_hintListSize0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, hintList = listOf())

    // Despite the solution being visible, no hints means that no hints could have been viewed.
    assertThat(hintIsRevealed).isFalse()
  }

  @Test
  fun testIsHintRevealed_everythingRevealed_index0_hintListSize2_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, HINT_LIST_OF_SIZE_2)

    // Everything has been revealed including all past hints.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_everythingRevealed_index1_hintListSize2_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 1, HINT_LIST_OF_SIZE_2)

    // Everything has been revealed including all past hints.
    assertThat(hintIsRevealed).isTrue()
  }

  @Test
  fun testIsHintRevealed_everythingRevealed_index0_hintListSize0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintIsRevealed = helpIndex.isHintRevealed(hintIndex = 0, hintList = listOf())

    // Despite everything being visible, no hints means that no hints could have been viewed.
    assertThat(hintIsRevealed).isFalse()
  }

  // TODO: Add tests.
  // - testDropLastUnavailable_defaultHelpIndex_emptyList_returnsEmptyList
  // - testDropLastUnavailable_defaultHelpIndex_nonEmptyList_returnsEmptyList
  // - testDropLastUnavailable_availableHintIndex0_emptyList_returnsEmptyList
  // - testDropLastUnavailable_availableHintIndex0_singletonList_returnsSingletonList
  // - testDropLastUnavailable_availableHintIndex0_twoHintsList_returnsSingletonList
  // - testDropLastUnavailable_availableHintIndex1_emptyList_returnsEmptyList
  // - testDropLastUnavailable_availableHintIndex1_singletonList_returnsSingletonList
  // - testDropLastUnavailable_availableHintIndex1_twoHintsList_returnsTwoItemList
  // - testDropLastUnavailable_availableHintIndex1_threeHintsList_returnsTwoItemList
  // - testDropLastUnavailable_lastRevealedHintIndex0_emptyList_returnsEmptyList
  // - testDropLastUnavailable_lastRevealedHintIndex0_singletonList_returnsSingletonList
  // - testDropLastUnavailable_lastRevealedHintIndex0_twoHintsList_returnsSingletonList
  // - testDropLastUnavailable_lastRevealedHintIndex1_emptyList_returnsEmptyList
  // - testDropLastUnavailable_lastRevealedHintIndex1_singletonList_returnsSingletonList
  // - testDropLastUnavailable_lastRevealedHintIndex1_twoHintsList_returnsTwoItemList
  // - testDropLastUnavailable_lastRevealedHintIndex1_threeHintsList_returnsTwoItemList
  // - testDropLastUnavailable_showSolution_emptyList_returnsEmptyList
  // - testDropLastUnavailable_showSolution_singletonList_returnsSingletonList
  // - testDropLastUnavailable_showSolution_twoHintsList_returnsTwoHintsList
  // - testDropLastUnavailable_showSolution_emptyList_returnsEmptyList
  // - testDropLastUnavailable_showSolution_singletonList_returnsSingletonList
  // - testDropLastUnavailable_showSolution_twoHintsList_returnsTwoItemList
  // - testDropLastUnavailable_showSolution_threeHintsList_returnsThreeItemList
  // - testDropLastUnavailable_everythingRevealed_emptyList_returnsEmptyList
  // - testDropLastUnavailable_everythingRevealed_singletonList_returnsSingletonList
  // - testDropLastUnavailable_everythingRevealed_twoHintsList_returnsTwoHintsList
  // - testDropLastUnavailable_everythingRevealed_emptyList_returnsEmptyList
  // - testDropLastUnavailable_everythingRevealed_singletonList_returnsSingletonList
  // - testDropLastUnavailable_everythingRevealed_twoHintsList_returnsTwoItemList
  // - testDropLastUnavailable_everythingRevealed_threeHintsList_returnsThreeItemList
  //
  // - testIsSolutionAvailable_defaultHelpIndex_returnsFalse
  // - testIsSolutionAvailable_availableHint0_returnsFalse
  // - testIsSolutionAvailable_availableHint1_returnsFalse
  // - testIsSolutionAvailable_viewedHint0_returnsFalse
  // - testIsSolutionAvailable_viewedHint1_returnsFalse
  // - testIsSolutionAvailable_showSolution_returnsTrue
  // - testIsSolutionAvailable_everythingIsRevealed_returnsTrue

  @Test
  fun testIsSolutionRevealed_defaultHelpIndex_returnsFalse() {
    val helpIndex = HelpIndex.getDefaultInstance()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // The default state indicates nothing has been viewed yet, including the solution.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_availableHint0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // If a hint is available to view, the solution could not yet have been revealed.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_availableHint1_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // If a hint is available to view, the solution could not yet have been revealed.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_viewedHint0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // If a hint was viewed, the solution might eventually be available to view but it evidently
    // hasn't yet been revealed.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_viewedHint1_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // If a hint was viewed, the solution might eventually be available to view but it evidently
    // hasn't yet been revealed.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_showSolution_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // The solution is now available to view, but it reportedly hasn't yet been revealed.
    assertThat(solutionIsRevealed).isFalse()
  }

  @Test
  fun testIsSolutionRevealed_everythingIsRevealed_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val solutionIsRevealed = helpIndex.isSolutionRevealed()

    // If everything has been revealed, that ensures the solution has also been revealed.
    assertThat(solutionIsRevealed).isTrue()
  }
}
