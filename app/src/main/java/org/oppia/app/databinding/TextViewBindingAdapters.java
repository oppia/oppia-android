package org.oppia.app.databinding;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.load.engine.Resource;
import org.jetbrains.annotations.NotNull;
import org.oppia.app.R;
import org.oppia.util.system.OppiaDateTimeFormatter;
import java.util.Locale;

public final class TextViewBindingAdapters {
    /** Binds date text with relative time. */
    @BindingAdapter("created")
    public static void setProfileDataText(@NotNull TextView textView, Long timestamp) {
        OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
        String time = oppiaDateTimeFormatter.formatDateFromDateString(
            OppiaDateTimeFormatter.DD_MMM_YYYY,
            timestamp,
            Locale.getDefault()
        );
        textView.setText(String.format(textView.getContext().getString(R.string.profile_edit_created, time)));
    }

    @BindingAdapter("lastVisited")
    public static void setProfileLastVisitedText(@NotNull TextView textView, Long timestamp) {
        textView.setText(String.format(textView.getContext().getString(R.string.profile_last_used)
                + " " + getTimeAgo(timestamp, textView.getContext())));
    }

    private static int SECOND_MILLIS = 1000;
    private static int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(Long lastVisitedTimeStamp, Context context) {

        OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
        Long timeStamp =
            oppiaDateTimeFormatter.checkAndConvertTimestampToMilliseconds(lastVisitedTimeStamp);
        Long now = oppiaDateTimeFormatter.currentDate().getTime();

        if (timeStamp > now || timeStamp <= 0) { return ""; }

        //TODO: Figure out what the type is
        Resources res = context.getResources();
        Long timeDifference = now - timeStamp;

        if (timeDifference < MINUTE_MILLIS) {
            return context.getString(R.string.just_now);
        } else if (timeDifference < 50 * MINUTE_MILLIS) {
            return context.getString(
                R.string.time_ago,
                res.getQuantityString(
                    R.plurals.minutes,
                    timeDifference.intValue() / MINUTE_MILLIS,
                    timeDifference / MINUTE_MILLIS
                )
            );
        } else if (timeDifference < 24 * HOUR_MILLIS) {
            return context.getString(
                R.string.time_ago,
                res.getQuantityString(
                    R.plurals.hours,
                    timeDifference.intValue() / HOUR_MILLIS,
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
                timeDifference.intValue() / DAY_MILLIS,
                timeDifference / DAY_MILLIS
            ));
    }
}
