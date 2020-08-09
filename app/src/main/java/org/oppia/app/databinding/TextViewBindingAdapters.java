package org.oppia.app.databinding;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import org.oppia.app.R;
import org.oppia.util.system.OppiaDateTimeFormatter;
import java.util.Locale;

/**
 * Holds all the custom binding adapters that bind to [TextView] adapters.
 */
public final class TextViewBindingAdapters {
  /**
   * Binds date text with relative time.
   */
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

  private static int SECOND_MILLIS = 1000;
  private static int MINUTE_MILLIS = 60 * SECOND_MILLIS;
  private static int HOUR_MILLIS = 60 * MINUTE_MILLIS;
  private static int DAY_MILLIS = 24 * HOUR_MILLIS;

  public static String getTimeAgo(long lastVisitedTimeStamp, Context context) {

    OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
    long timeStamp =
        oppiaDateTimeFormatter.checkAndConvertTimestampToMilliseconds(lastVisitedTimeStamp);
    long now = oppiaDateTimeFormatter.currentDate().getTime();

    if (timeStamp > now || timeStamp <= 0) { return ""; }

    Resources res = context.getResources();
    long timeDifference = now - timeStamp;

    if (timeDifference < MINUTE_MILLIS) {
      return context.getString(R.string.just_now);
    } else if (timeDifference < 50 * MINUTE_MILLIS) {
      return context.getString(
          R.string.time_ago,
          res.getQuantityString(
              R.plurals.minutes,
              (int) timeDifference / MINUTE_MILLIS,
              timeDifference / MINUTE_MILLIS
          )
      );
    } else if (timeDifference < 24 * HOUR_MILLIS) {
      return context.getString(
          R.string.time_ago,
          res.getQuantityString(
              R.plurals.hours,
              (int) timeDifference / HOUR_MILLIS,
              timeDifference / HOUR_MILLIS
          )
      );
    } else if (timeDifference < 48 * HOUR_MILLIS) {
      return context.getString(R.string.yesterday);
    }
    return context.getString(
        R.string.time_ago,
        res.getQuantityString(
            R.plurals.days,
            (int) timeDifference / DAY_MILLIS,
            timeDifference / DAY_MILLIS
        ));
  }
}
