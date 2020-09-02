package org.oppia.app.databinding;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.databinding.BindingAdapter;
import org.oppia.app.R;
import org.oppia.util.system.OppiaDateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Holds all custom binding adapters that bind to [TextView]. */
public final class TextViewBindingAdapters {

  /** Binds date text with relative time. */
  @BindingAdapter("profile:created")
  public static void setProfileDataText(@NonNull TextView textView, long timestamp) {
    OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
    String time = oppiaDateTimeFormatter.formatDateFromDateString(
        OppiaDateTimeFormatter.DD_MMM_YYYY,
        timestamp,
        Locale.getDefault()
    );
    textView.setText(textView.getContext().getString(
        R.string.profile_edit_created,
        time
    ));
  }

  @BindingAdapter("profile:lastVisited")
  public static void setProfileLastVisitedText(@NonNull TextView textView, long timestamp) {
    String profileLastUsed = textView.getContext().getString(R.string.profile_last_used);
    String timeAgoTimeStamp = getTimeAgo(
        timestamp,
        textView.getContext()
    );
    String profileLastVisited = textView.getContext().getString(
        R.string.profile_last_visited,
        profileLastUsed,
        timeAgoTimeStamp
    );
    textView.setText(profileLastVisited);
  }

  private static String getTimeAgo(long lastVisitedTimeStamp, Context context) {
    OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
    long timeStampMillis =
        oppiaDateTimeFormatter.checkAndConvertTimestampToMilliseconds(lastVisitedTimeStamp);
    long currentTimeMillis = oppiaDateTimeFormatter.currentDate().getTime();

    if (timeStampMillis > currentTimeMillis || timeStampMillis <= 0) {
      return "";
    }

    Resources res = context.getResources();
    long timeDifferenceMillis = currentTimeMillis - timeStampMillis;

    if (timeDifferenceMillis < (int) TimeUnit.MINUTES.toMillis(1)) {
      return context.getString(R.string.just_now);
    } else if (timeDifferenceMillis < TimeUnit.MINUTES.toMillis(50)) {
      return getPluralString(
          context,
          R.plurals.minutes,
          (int) TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(1)) {
      return getPluralString(
          context,
          R.plurals.hours,
          (int) TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
      );
    } else if (timeDifferenceMillis < TimeUnit.DAYS.toMillis(2)) {
      return context.getString(R.string.yesterday);
    }
    return getPluralString(
        context,
        R.plurals.days,
        (int) TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis)
    );
  }

  private static String getPluralString(
      @NonNull Context context,
      @PluralsRes int pluralsResId,
      int count
  ) {
    Resources resources = context.getResources();
    return context.getString(
        R.string.time_ago,
        resources.getQuantityString(
            pluralsResId,
            count,
            count
        )
    );
  }
}
