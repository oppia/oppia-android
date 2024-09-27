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
import java.util.concurrent.TimeUnit;
import org.oppia.android.R;
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider;
import org.oppia.android.app.translation.AppLanguageResourceHandler;
import org.oppia.android.util.system.OppiaClock;
import org.oppia.android.util.system.OppiaClockInjectorProvider;

/** Holds all custom binding adapters that bind to [TextView]. */
public final class TextViewBindingAdapters {

  /** Binds date text with relative time. */
  @BindingAdapter("profileCreatedTime")
  public static void setProfileDataText(@NonNull TextView textView, long timestamp) {
    AppLanguageResourceHandler resourceHandler = getResourceHandler(textView);
    String time = resourceHandler.computeDateString(timestamp);
    textView.setText(resourceHandler.getStringInLocaleWithWrapping(
        R.string.profile_edit_created,
        time
    ));
  }

  /** Binds last used with relative timestamp. */
  @BindingAdapter("profileLastVisitedTime")
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
  @BindingAdapter("drawableTopCompat")
  public static void setDrawableTopCompat(
      @NonNull TextView imageView,
      Drawable drawable
  ) {
    imageView.setCompoundDrawablesRelativeWithIntrinsicBounds(
        /* start= */ null, /* top= */ drawable, /* end= */ null, /* bottom= */ null
    );
  }

  /** Binds an AndroidX KitKat-compatible drawable end to the specified text view. */
  @BindingAdapter("drawableEndCompat")
  public static void setDrawableEndCompat(
      @NonNull TextView imageView,
      Drawable drawable
  ) {
    imageView.setCompoundDrawablesRelativeWithIntrinsicBounds(
        /* start= */ null, /* top= */ null, /* end= */ drawable, /* bottom= */ null
    );
  }

  private static String getTimeAgo(View view, long lastVisitedTimestamp) {
    long currentTimeMillis = getOppiaClock(view).getCurrentTimeMs();
    AppLanguageResourceHandler resourceHandler = getResourceHandler(view);

    if (lastVisitedTimestamp > currentTimeMillis || lastVisitedTimestamp <= 0) {
      return resourceHandler.getStringInLocale(R.string.last_logged_in_recently);
    }

    long timeDifferenceMillis = currentTimeMillis - lastVisitedTimestamp;

    if (timeDifferenceMillis < (int) TimeUnit.MINUTES.toMillis(1)) {
      return resourceHandler.getStringInLocale(R.string.just_now);
    } else if (timeDifferenceMillis < TimeUnit.MINUTES.toMillis(50)) {
      return getPluralString(
              resourceHandler,
          R.plurals.minutes_ago,
          (int) TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(1)) {
      return getPluralString(
              resourceHandler,
          R.plurals.hours_ago,
          (int) TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(2)) {
      return resourceHandler.getStringInLocale(R.string.yesterday);
    }
    return getPluralString(
            resourceHandler,
        R.plurals.days_ago,
        (int) TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis)
    );
  }

  private static String getPluralString(
      AppLanguageResourceHandler resourceHandler,
      @PluralsRes int pluralsResId,
      int count
  ) {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
            pluralsResId, count, String.valueOf(count)
        );
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
