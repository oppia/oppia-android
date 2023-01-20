package org.oppia.android.domain.hintsandsolution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.SubtitledHtml
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [HelpIndex] extensions. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class HelpIndexExtensionsTest {

  private companion object {
    private val HINT_0 = createHint(text = "First hint")
    private val HINT_1 = createHint(text = "Second hint")
    private val HINT_2 = createHint(text = "Third hint")

    private val HINT_LIST_OF_SIZE_1 = listOf(HINT_0)
    private val HINT_LIST_OF_SIZE_2 = HINT_LIST_OF_SIZE_1 + HINT_1
    private val HINT_LIST_OF_SIZE_3 = HINT_LIST_OF_SIZE_2 + HINT_2
  }

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

  @Test
  fun testDropLastUnavailable_defaultHelpIndex_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.getDefaultInstance()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_defaultHelpIndex_nonEmptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.getDefaultInstance()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex0_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex0_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex0_twoHintsList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    // Only the first hint is available to be seen.
    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex1_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex1_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex1_twoHintsList_returnsTwoItemList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_availableHintIndex1_threeHintsList_returnsTwoItemList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_3)

    // Only the first two hints are available.
    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex0_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex0_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex0_twoHintsList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    // Only the first hint is available.
    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex1_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex1_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex1_twoHintsList_returnsTwoItemList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_lastRevealedHintIndex1_threeHintsList_returnsTwoItemList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_3)

    // Only the first two hints are available.
    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_showSolution_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    // There are no hints to show.
    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_showSolution_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    // The only hint is available.
    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_showSolution_twoHintsList_returnsTwoHintsList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    // All hints are available.
    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_showSolution_threeHintsList_returnsThreeHintsList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_3)

    // All hints are available.
    assertThat(hintList).hasSize(3)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
    assertThat(hintList[2]).isEqualTo(HINT_2)
  }

  @Test
  fun testDropLastUnavailable_everythingRevealed_emptyList_returnsEmptyList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = listOf())

    // There are no hints to show.
    assertThat(hintList).isEmpty()
  }

  @Test
  fun testDropLastUnavailable_everythingRevealed_singletonList_returnsSingletonList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_1)

    // The only hint is available.
    assertThat(hintList).hasSize(1)
    assertThat(hintList[0]).isEqualTo(HINT_0)
  }

  @Test
  fun testDropLastUnavailable_everythingRevealed_twoHintsList_returnsTwoHintsList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_2)

    // All hints are available.
    assertThat(hintList).hasSize(2)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
  }

  @Test
  fun testDropLastUnavailable_everythingRevealed_threeHintsList_returnsThreeHintsList() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val hintList = helpIndex.dropLastUnavailable(hintList = HINT_LIST_OF_SIZE_3)

    // All hints are available.
    assertThat(hintList).hasSize(3)
    assertThat(hintList[0]).isEqualTo(HINT_0)
    assertThat(hintList[1]).isEqualTo(HINT_1)
    assertThat(hintList[2]).isEqualTo(HINT_2)
  }

  @Test
  fun testIsSolutionAvailable_defaultHelpIndex_returnsFalse() {
    val helpIndex = HelpIndex.getDefaultInstance()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // By default, the solution is not available to be revealed.
    assertThat(solutionIsAvailable).isFalse()
  }

  @Test
  fun testIsSolutionAvailable_availableHint0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 0
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If there's an available hint to be shown, the solution is not yet available to reveal.
    assertThat(solutionIsAvailable).isFalse()
  }

  @Test
  fun testIsSolutionAvailable_availableHint1_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      nextAvailableHintIndex = 1
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If there's an available hint to be shown, the solution is not yet available to reveal.
    assertThat(solutionIsAvailable).isFalse()
  }

  @Test
  fun testIsSolutionAvailable_viewedHint0_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 0
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If a hint has been viewed and there are yet more hints, the solution isn't yet available.
    assertThat(solutionIsAvailable).isFalse()
  }

  @Test
  fun testIsSolutionAvailable_viewedHint1_returnsFalse() {
    val helpIndex = HelpIndex.newBuilder().apply {
      latestRevealedHintIndex = 1
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If a hint has been viewed and there are yet more hints, the solution isn't yet available.
    assertThat(solutionIsAvailable).isFalse()
  }

  @Test
  fun testIsSolutionAvailable_showSolution_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      showSolution = true
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If the solution can be shown, it's available.
    assertThat(solutionIsAvailable).isTrue()
  }

  @Test
  fun testIsSolutionAvailable_everythingIsRevealed_returnsTrue() {
    val helpIndex = HelpIndex.newBuilder().apply {
      everythingRevealed = true
    }.build()

    val solutionIsAvailable = helpIndex.isSolutionAvailable()

    // If the solution has been revealed already, it's still available to be viewed.
    assertThat(solutionIsAvailable).isTrue()
  }

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

private fun createHint(text: String): Hint {
  return Hint.newBuilder().apply {
    hintContent = SubtitledHtml.newBuilder().apply {
      html = text
    }.build()
  }.build()
}
