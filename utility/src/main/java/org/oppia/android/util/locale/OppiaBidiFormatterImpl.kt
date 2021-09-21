package org.oppia.android.util.locale

import android.text.BidiFormatter
import java.util.Locale
import javax.inject.Inject

/** Production implementation of [OppiaBidiFormatter]. */
class OppiaBidiFormatterImpl private constructor(locale: Locale) : OppiaBidiFormatter {
  private val bidiFormatter by lazy { BidiFormatter.getInstance(locale) }

  override fun wrapText(unicode: CharSequence): CharSequence = bidiFormatter.unicodeWrap(unicode)

  /** Implementation of [OppiaBidiFormatter.Factory]. */
  class FactoryImpl @Inject constructor() : OppiaBidiFormatter.Factory {
    override fun createFormatter(locale: Locale): OppiaBidiFormatter =
      OppiaBidiFormatterImpl(locale)
  }
}
