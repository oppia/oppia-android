//package org.oppia.util.parser
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.Options
//import com.bumptech.glide.load.ResourceDecoder
//import com.bumptech.glide.load.engine.Resource
//import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
//import com.bumptech.glide.load.resource.bitmap.BitmapResource
//import com.caverock.androidsvg.SVG
//import com.caverock.androidsvg.SVGParseException
//import java.io.IOException
//import java.io.InputStream
//
//class SvgBitmapDecoder(private val bitmapPool: BitmapPool) :
//  ResourceDecoder<InputStream?, Bitmap?> {
//
//  constructor(context: Context?) : this(Glide.get(context!!).bitmapPool) {}
//
//  @Throws(IOException::class)
//  override fun decode(
//    source: InputStream,
//    width: Int,
//    height: Int,
//    options: Options
//  ): Resource<Bitmap?>? {
//    return try {
//      val svg = SVG.getFromInputStream(source)
//      val bitmap = findBitmap(width, height)
//      val canvas = Canvas(bitmap!!)
//      svg.renderToCanvas(canvas)
//      BitmapResource.obtain(bitmap, bitmapPool)
//    } catch (ex: SVGParseException) {
//      throw IOException("Cannot load SVG from stream", ex)
//    }
//  }
//
//  private fun findBitmap(width: Int, height: Int): Bitmap? {
//    var bitmap: Bitmap? = bitmapPool[width, height, Bitmap.Config.ARGB_8888]
//    if (bitmap == null) {
//      bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    }
//    return bitmap
//  }
//
//  val id: String
//    get() = ""
//
//  override fun handles(source: InputStream, options: Options): Boolean {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//}
