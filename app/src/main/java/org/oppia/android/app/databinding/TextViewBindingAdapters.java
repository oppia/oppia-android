package org.oppia.android.app.databinding;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.databinding.BindingAdapter;
import asia.ivity.android.marqueeview.MarqueeView;
import java.util.concurrent.TimeUnit;
import org.oppia.android.R;
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider;
import org.oppia.android.app.translation.AppLanguageResourceHandler;
import org.oppia.android.util.system.OppiaClock;
import org.oppia.android.util.system.OppiaClockInjectorProvider;

/** Holds all custom binding adapters that bind to [TextView]. */
public final class TextViewBindingAdapters {

  /** Binds date text with relative time. */
  @BindingAdapter("profile:created")
  public static void setProfileDataText(@NonNull TextView textView, long timestamp) {
    AppLanguageResourceHandler resourceHandler = getResourceHandler(textView);
    String time = resourceHandler.computeDateString(timestamp);
    textView.setText(resourceHandler.getStringInLocaleWithWrapping(
        R.string.profile_edit_created,
        time
    ));
  }

  /** Binds last used with relative timestamp. */
  @BindingAdapter("profile:lastVisited")
  public static void setProfileLastVisitedText(@NonNull TextView textView, long timestamp) {
    AppLanguageResourceHandler resourceHandler = getResourceHandler(textView);
    String profileLastUsed = resourceHandler.getStringInLocale(R.string.profile_last_used);
    String timeAgoTimeStamp = getTimeAgo(textView, timestamp);
    String profileLastVisited = resourceHandler.getStringInLocaleWithWrapping(
        R.string.profile_last_visited,
        profileLastUsed,
        timeAgoTimeStamp
    );
    textView.setText(profileLastVisited);
  }

  // TODO(#4345): Add test for this method.
  /** Binds an AndroidX KitKat-compatible drawable top to the specified text view. */
  @BindingAdapter("app:drawableTopCompat")
  public static void setDrawableTopCompat(
      @NonNull TextView imageView,
      Drawable drawable
  ) {
    imageView.setCompoundDrawablesRelativeWithIntrinsicBounds(
        /* start= */ null, /* top= */ drawable, /* end= */ null, /* bottom= */ null
    );
  }

  /** Binds an AndroidX KitKat-compatible drawable end to the specified text view. */
  @BindingAdapter("app:drawableEndCompat")
  public static void setDrawableEndCompat(
      @NonNull TextView imageView,
      Drawable drawable
  ) {
    imageView.setCompoundDrawablesRelativeWithIntrinsicBounds(
        /* start= */ null, /* top= */ null, /* end= */ drawable, /* bottom= */ null
    );
  }

  /** Binds speed to the specified MarqueeView. */
  @BindingAdapter("app:speed")
  public static void setMarqueeSpeed(
      @NonNull MarqueeView marqueeView,
      int speed
  ) {
    marqueeView.setSpeed(speed);
  }

  /** Binds end animation pause to the specified MarqueeView. */
  @BindingAdapter("app:pause")
  public static void setAnimationPause(
      @NonNull MarqueeView marqueeView,
      int speed
  ) {
    marqueeView.setPauseBetweenAnimations(speed);
  }

  private static String getTimeAgo(View view, long lastVisitedTimestamp) {
    long timeStampMillis = ensureTimestampIsInMilliseconds(lastVisitedTimestamp);
    long currentTimeMillis = getOppiaClock(view).getCurrentTimeMs();
    AppLanguageResourceHandler resourceHandler = getResourceHandler(view);

    if (timeStampMillis > currentTimeMillis || timeStampMillis <= 0) {
      return resourceHandler.getStringInLocale(R.string.last_logged_in_recently);
    }

    long timeDifferenceMillis = currentTimeMillis - timeStampMillis;

    if (timeDifferenceMillis < (int) TimeUnit.MINUTES.toMillis(1)) {
      return resourceHandler.getStringInLocale(R.string.just_now);
    } else if (timeDifferenceMillis < TimeUnit.MINUTES.toMillis(50)) {
      return getPluralString(
              resourceHandler,
          R.plurals.minutes,
          (int) TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(1)) {
      return getPluralString(
              resourceHandler,
          R.plurals.hours,
          (int) TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(2)) {
      return resourceHandler.getStringInLocale(R.string.yesterday);
    }
    return getPluralString(
            resourceHandler,
        R.plurals.days,
        (int) TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis)
    );
  }

  private static String getPluralString(
      AppLanguageResourceHandler resourceHandler,
      @PluralsRes int pluralsResId,
      int count
  ) {
    // TODO(#3841): Combine these strings together.
    return resourceHandler.getStringInLocaleWithWrapping(
        R.string.time_ago,
        resourceHandler.getQuantityStringInLocaleWithWrapping(
            pluralsResId, count, String.valueOf(count)
        )
    );
  }

  private static long ensureTimestampIsInMilliseconds(long lastVisitedTimestamp) {
    // TODO(#3842): Investigate & remove this check.
    if (lastVisitedTimestamp < 1000000000000L) {
      // If timestamp is given in seconds, convert that to milliseconds.
      return TimeUnit.SECONDS.toMillis(lastVisitedTimestamp);
    }
    return lastVisitedTimestamp;
  }

  private static AppLanguageResourceHandler getResourceHandler(View view) {
    AppLanguageActivityInjectorProvider provider =
        (AppLanguageActivityInjectorProvider) getAttachedActivity(view);
    return provider.getAppLanguageActivityInjector().getAppLanguageResourceHandler();
  }

  private static Activity getAttachedActivity(View view) {
    Context context = view.getContext();
    while (context != null && !(context instanceof Activity)) {
      if (!(context instanceof ContextWrapper)) {
        throw new IllegalStateException(
          "Encountered context in view (" + view + ") that doesn't wrap a parent context: "
            + context
        );
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    if (context == null) {
      throw new IllegalStateException("Failed to find base Activity for view: " + view);
    }
    return (Activity) context;
  }

  private static OppiaClock getOppiaClock(View view) {
    OppiaClockInjectorProvider provider = (OppiaClockInjectorProvider) getApplication(view);
    return provider.getOppiaClockInjector().getOppiaClock();
  }

  private static Context getApplication(View view) {
    return view.getContext().getApplicationContext();
  }
}
