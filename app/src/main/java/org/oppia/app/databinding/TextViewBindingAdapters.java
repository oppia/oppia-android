package org.oppia.app.databinding;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import org.oppia.app.R;
import org.oppia.util.system.OppiaDateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Holds all custom binding adapters that bind to [TextView]. */
public final class TextViewBindingAdapters {

  private static int MINUTE_MILLIS = (int) TimeUnit.MINUTES.toMillis(1);
  private static int HOUR_MILLIS = (int) TimeUnit.HOURS.toMillis(1);
  private static int DAY_MILLIS = (int) TimeUnit.DAYS.toMillis(1);

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
    textView.setText(
        String.format(
            textView.getContext().getString(R.string.profile_last_used) + " " + getTimeAgo(
                timestamp,
                textView.getContext()
            )
        )
    );
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

    if (timeDifferenceMillis < MINUTE_MILLIS) {
      return context.getString(R.string.just_now);
    } else if (timeDifferenceMillis < 50 * MINUTE_MILLIS) {
      return context.getString(
          R.string.time_ago,
          res.getQuantityString(
              R.plurals.minutes,
              (int) (timeDifferenceMillis / MINUTE_MILLIS),
              (int) (timeDifferenceMillis / MINUTE_MILLIS)
          )
      );
    } else if (timeDifferenceMillis < 24 * HOUR_MILLIS) {
      return context.getString(
          R.string.time_ago,
          res.getQuantityString(
              R.plurals.hours,
              (int) (timeDifferenceMillis / HOUR_MILLIS),
              (int) (timeDifferenceMillis / HOUR_MILLIS)
          )
      );
    } else if (timeDifferenceMillis < 48 * HOUR_MILLIS) {
      return context.getString(R.string.yesterday);
    }
    return context.getString(
        R.string.time_ago,
        res.getQuantityString(
            R.plurals.days,
            (int) (timeDifferenceMillis / DAY_MILLIS),
            (int) (timeDifferenceMillis / DAY_MILLIS)
        ));
  }
}
