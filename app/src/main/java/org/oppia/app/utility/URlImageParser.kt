package org.oppia.app.utility;

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.net.URL

/** */
open class URLImageParser(internal var mTv: TextView, internal var mContext: Context) : Html.ImageGetter {

    override fun getDrawable(source: String): Drawable {
        val drawable = LevelListDrawable()
        Glide.with(mContext)
                .asBitmap()
                .load(URL(source))
                .apply( RequestOptions().override(100, 100)) //This is important
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (resource != null) {
                            val bitmapDrawable = BitmapDrawable(resource)
                            drawable.addLevel(1, 1, bitmapDrawable)
                            drawable.setBounds(0, 0, resource.width, resource.height)
                            drawable.level = 1
                            mTv.invalidate()
                            mTv.text = mTv.text

                        }
                    }
                });
        return drawable
    }

}
