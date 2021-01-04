package org.oppia.android.app.parser

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.UnderlineSpan
import androidx.core.text.getSpans
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.parser.CustomBulletSpan
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(qualifiers = "port-xxhdpi")
class CustomBulletSpanTest {

  private var context: Context = ApplicationProvider.getApplicationContext()

  private val testStringWithoutBulletSpan = SpannableString("Text Without BulletSpan")
  private val testStringWithBulletSpan = SpannableString("Text With \nBullet Point").apply {
    setSpan(BulletSpan(), 10, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  private val testStringWithMultipleBulletSpan = SpannableString(
    "Text With \nfirst \nsecond \nthird \nfour \nfive"
  ).apply {
    setSpan(BulletSpan(), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 18, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 27, 35, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(UnderlineSpan(), 42, 43, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  private val testStringWithCustomBulletSpan = SpannableString("Text With \nBullet Point").apply {
    this.setSpan(
      CustomBulletSpan(context),
      10,
      22,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_spannableStringWithoutBulletSpanRemainSame() {
    val spannableString1 = testStringWithoutBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(0)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_spannableStringWithBulletSpanIsNotSame() {
    val spannableString1 = testStringWithBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)

    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_multipleBulletSpanAndUnderlineSpan_underlineSpan() {
    val spannableString1 = testStringWithMultipleBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(4)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getUnderlineSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(4)
    assertThat(getUnderlineSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_customBulletSpans_RemainsSame() {
    val spannableString1 = testStringWithCustomBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testLeadMargin_isComputedToProperlyIndentText() {
    val bulletRadius = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.bullet_radius
    )
    val spacingBeforeBullet = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_bullet
    )
    val spacingBeforeText = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_text
    )
    val expectedMargin = spacingBeforeBullet + spacingBeforeText + 2 * bulletRadius
    val spannableString = SpannableStringBuilder(testStringWithBulletSpan)
    val customBulletSpannable = CustomBulletSpan.replaceBulletSpan(spannableString, context)
    val leadingMargin = customBulletSpannable.getSpans(
      0,
      spannableString.length,
      CustomBulletSpan::class.java
    )[0].getLeadingMargin(true)
    assertThat(leadingMargin).isEqualTo(expectedMargin)
  }

  private fun getBulletSpans(spannableString: SpannableString): Array<out BulletSpan> {
    return spannableString.getSpans<BulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getCustomBulletSpans(
    spannableString: SpannableString
  ): Array<out CustomBulletSpan> {
    return spannableString.getSpans<CustomBulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getUnderlineSpans(spannableString: SpannableString): Array<out UnderlineSpan> {
    return spannableString.getSpans<UnderlineSpan>(
      0,
      spannableString.length
    )
  }

  private fun getBulletSpanCount(spannableString: SpannableString): Int {
    return getBulletSpans(spannableString).size
  }

  private fun getCustomBulletSpanCount(spannableString: SpannableString): Int {
    return getCustomBulletSpans(spannableString).size
  }

  private fun getUnderlineSpanCount(spannableString: SpannableString): Int {
    return getUnderlineSpans(spannableString).size
  }
}