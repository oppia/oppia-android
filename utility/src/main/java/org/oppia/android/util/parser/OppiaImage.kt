package org.oppia.android.util.parser

sealed class OppiaImage {
  class BitmapImage() : OppiaImage()
  class SvgImage() : OppiaImage()
}