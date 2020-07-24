package org.oppia.app.databinding;

import android.content.Context;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.load.engine.Resource;

import org.jetbrains.annotations.NotNull;
import org.oppia.app.R;
import org.oppia.util.system.OppiaDateTimeFormatter;

import java.util.Locale;

public final class TextViewBindingAdapters {
    /** Binds date text with relative time. */
    @BindingAdapter("profile:created")
    public static void setProfileDataText(@NotNull TextView textView, Long timestamp) {
        OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
        String time = oppiaDateTimeFormatter.formatDateFromDateString(
            OppiaDateTimeFormatter.DD_MMM_YYYY,
            timestamp,
            Locale.getDefault()
        );
        textView.text = String.format(textView.context.getString(R.string.profile_edit_created, time));
    }

    @BindingAdapter("profile:lastVisited")
    public static void setProfileLastVisitedText(@NotNull TextView textView, Long timestamp) {
        textView.text =
            String.format(
                textView.context.getString(R.string.profile_last_used) + " " + getTimeAgo(
                    timestamp,
                    textView.context
                )
            );
    }

    private final int SECOND_MILLIS = 1000;
    private final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(Long lastVisitedTimeStamp, Context context) {

        OppiaDateTimeFormatter oppiaDateTimeFormatter = new OppiaDateTimeFormatter();
        Long timeStamp =
            oppiaDateTimeFormatter.checkAndConvertTimestampToMilliseconds(lastVisitedTimeStamp);
        Long now = oppiaDateTimeFormatter.currentDate().time;

        if (timeStamp > now || timeStamp <= 0) { return ""; }
        //TODO: Figure out what the type is
        val res = context.resources;
        Long timeDifference = now - timeStamp;
        //TODO: Fix this into if statements
        return switch {
            case (timeDifference < MINUTE_MILLIS): context.getString(R.string.just_now);
            case (timeDifference < 50 * MINUTE_MILLIS): context.getString(
            R.string.time_ago,
            res.getQuantityString(
                R.plurals.minutes,
                timeDifference.toInt() / MINUTE_MILLIS,
                timeDifference / MINUTE_MILLIS
            )
            );
            case (timeDifference < 24 * HOUR_MILLIS): context.getString(
            R.string.time_ago,
            res.getQuantityString(
                R.plurals.hours,
                timeDifference.toInt() / HOUR_MILLIS,
                timeDifference / HOUR_MILLIS
            )
            );
            case (timeDifference < 48 * HOUR_MILLIS): context.getString(R.string.yesterday);
            default: context.getString(
                R.string.time_ago,
                res.getQuantityString(
                    R.plurals.days,
                    timeDifference.toInt() / DAY_MILLIS,
                    timeDifference / DAY_MILLIS
                );
            );
        }
    }
}