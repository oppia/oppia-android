package org.oppia.util.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.net.URL
import android.graphics.BitmapFactory
import android.graphics.drawable.LevelListDrawable
import android.os.AsyncTask
import org.oppia.util.data.UrlImageParser.LoadImage
import android.R
import android.content.ContentValues.TAG
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException

// TODO (#169) : Replace this with exploration asset downloader
/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser(
  internal var tvContents: TextView,
  internal var context: Context,
  private val entity_type: String,
  private val entity_id: String
) : Html.ImageGetter {

  val GCS_PREFIX: String = "https://storage.googleapis.com/"
  val GCS_RESOURCE_BUCKET_NAME = "oppiaserver-resources/"
  var IMAGE_DOWNLOAD_URL_TEMPLATE = "<entity_type>/<entity_id>/assets/image/<filename>"

  // TODO below implementation is to load image using Glide Library
  /***
   * This method is called when the HTML parser encounters an <img> tag
   * @param urlString : urlString argument is the string from the "src" attribute
   * @return Drawable : Drawable representation of the image
   */
  override fun getDrawable(urlString: String): Drawable {
    IMAGE_DOWNLOAD_URL_TEMPLATE = entity_type + "/" + entity_id + "/assets/image/"
    val urlDrawable = UrlDrawable()
    val load = Glide.with(context).asBitmap()
      .load(URL(GCS_PREFIX + GCS_RESOURCE_BUCKET_NAME + IMAGE_DOWNLOAD_URL_TEMPLATE + urlString))
    val target = BitmapTarget(urlDrawable)
    load.into(target)
    return urlDrawable
  }

  inner class BitmapTarget(private val urlDrawable: UrlDrawable) : SimpleTarget<Bitmap>() {
    internal var drawable: Drawable? = null
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      drawable = BitmapDrawable(context.resources, resource)
      tvContents.post {
        val textviewWidth = tvContents.width
        val drawableHeight = (drawable as BitmapDrawable).intrinsicHeight
        val drawableWidth = (drawable as BitmapDrawable).intrinsicWidth
        // To resize the image keeping aspect ratio.
        if (drawableWidth > textviewWidth) {
          val calculatedHeight = textviewWidth * drawableHeight / drawableWidth;
          val rect = Rect(0, 0, textviewWidth, calculatedHeight)
          (drawable as BitmapDrawable).bounds = rect
          urlDrawable.bounds = rect
          Log.d("textviewWidth",""+textviewWidth+" "+calculatedHeight)
        } else {
          val rect = Rect(0, 0, drawableWidth, drawableHeight)
          (drawable as BitmapDrawable).bounds = rect
          urlDrawable.bounds = rect
          Log.d("drawableWidth",""+drawableWidth+" "+drawableHeight)
        }
        urlDrawable.drawable = drawable
        tvContents.text = tvContents.text
        tvContents.invalidate()
      }
    }
  }
  
  inner class UrlDrawable : BitmapDrawable() {
    var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
      if (drawable != null)
        drawable!!.draw(canvas)
    }
  }

//  TODO below implementation is to load image using Asynctask
//  override fun getDrawable(urlString: String): Drawable {
//    IMAGE_DOWNLOAD_URL_TEMPLATE = entity_type + "/" + entity_id + "/assets/image/"
//
//    val d = LevelListDrawable()
//    val empty = context.getResources().getDrawable(R.drawable.ic_menu_gallery)
//    d.addLevel(0, 0, empty)
//    d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight())
//
//    LoadImage().execute(GCS_PREFIX + GCS_RESOURCE_BUCKET_NAME + IMAGE_DOWNLOAD_URL_TEMPLATE + urlString, d)
//    return d
//  }
//
//  internal inner class LoadImage : AsyncTask<Any, Void, Bitmap>() {
//
//    private var mDrawable: LevelListDrawable? = null
//
//    override fun doInBackground(vararg params: Any): Bitmap? {
//      val source = params[0] as String
//      mDrawable = params[1] as LevelListDrawable
//      Log.d(TAG, "doInBackground $source")
//      try {
//        val `is` = URL(source).openStream()
//        return BitmapFactory.decodeStream(`is`)
//      } catch (e: FileNotFoundException) {
//        e.printStackTrace()
//      } catch (e: MalformedURLException) {
//        e.printStackTrace()
//      } catch (e: IOException) {
//        e.printStackTrace()
//      }
//      return null
//    }
//
//    override fun onPostExecute(bitmap: Bitmap?) {
//      if (bitmap != null) {
//        val d = BitmapDrawable(bitmap)
//        mDrawable!!.addLevel(1, 1, d)
//        mDrawable!!.setBounds(0, 0, bitmap.width, bitmap.height)
//        mDrawable!!.level = 1
//        // to refresh TextView
//        val t = tvContents.getText()
//        tvContents.setText(t)
//      }
//    }
//  }
}
