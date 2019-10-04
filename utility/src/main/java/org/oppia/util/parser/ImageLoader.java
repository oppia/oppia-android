package org.oppia.util.parser;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.DrawableRes;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.annotation.Target;

public interface ImageLoader {

  static void load(Context context, String path, int placeholder, SimpleTarget<Bitmap> target) {
    Glide.with(context)
        .asBitmap()
        .load(path)
        .placeholder(placeholder)
        .into(target);
  }
}
