package org.oppia.android.util.parser

sealed class OppiaImage {
  class BitmapImage(val urlDrawable: UrlImageParser.UrlDrawable) : OppiaImage()
  class SvgImage(val urlDrawable: UrlImageParser.UrlDrawable) : OppiaImage()
}