package org.oppia.android.util.parser.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.security.MessageDigest

/** Tests for [MathModel]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class MathModelTest {
  @Test
  fun testToKeySignature_sameModelByValues_returnsSameKeyWithSameDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    assertThat(key1).isEqualTo(key2)
    assertThat(key1.hashCode()).isEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isEqualTo(digest2.digest())
    assertThat(model1).isEqualTo(model2)
  }

  @Test
  fun testToKeySignature_differentModelByLatex_returnsDifferentKeyWithDifferentDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 = MathModel(rawLatex = "\\frac{3}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    // Since the LaTeX differs, nothing should match.
    assertThat(key1).isNotEqualTo(key2)
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isNotEqualTo(digest2.digest())
    assertThat(model1).isNotEqualTo(model2)
  }

  @Test
  fun testToKeySignature_differentModelByLineHeight_returnsDifferentKeyWithDifferentDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 20.5f, useInlineRendering = true)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    // Since the line height differs, nothing should match.
    assertThat(key1).isNotEqualTo(key2)
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isNotEqualTo(digest2.digest())
    assertThat(model1).isNotEqualTo(model2)
  }

  @Test
  fun testToKeySignature_diffModelByLineHeight_withinTwoDecimals_returnsSameKeyWithSameDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 =
      MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.501f, useInlineRendering = true)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    // The line heights are close enough that they're considered equal for key purposes (but are
    // still different models).
    assertThat(key1).isEqualTo(key2)
    assertThat(key1.hashCode()).isEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isEqualTo(digest2.digest())
    assertThat(model1).isNotEqualTo(model2)
  }

  @Test
  fun testToKeySignature_diffModelByLineHeight_outsideTwoDecimals_returnsDiffKeyWithDiffDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.6f, useInlineRendering = true)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    // Since the line height differs, nothing should match.
    assertThat(key1).isNotEqualTo(key2)
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isNotEqualTo(digest2.digest())
    assertThat(model1).isNotEqualTo(model2)
  }

  @Test
  fun testToKeySignature_differentModelByInlineRendering_returnsDifferentKeyWithDifferentDigest() {
    val model1 = MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = true)
    val model2 =
      MathModel(rawLatex = "\\frac{2}{6}", lineHeight = 21.5f, useInlineRendering = false)
    val digest1 = MessageDigest.getInstance("SHA-256")
    val digest2 = MessageDigest.getInstance("SHA-256")

    val key1 = model1.toKeySignature()
    val key2 = model2.toKeySignature()
    key1.updateDiskCacheKey(digest1)
    key2.updateDiskCacheKey(digest2)

    // Since the inline rendering setting differs, nothing should match.
    assertThat(key1).isNotEqualTo(key2)
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode())
    assertThat(digest1.digest()).isNotEqualTo(digest2.digest())
    assertThat(model1).isNotEqualTo(model2)
  }
}
