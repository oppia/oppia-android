package org.oppia.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.databinding.BindingAdapter;

import org.jetbrains.annotations.NotNull;

public final class MarginBindingAdapters {
    /** Used to set a margin-start for views. */
    @BindingAdapter("app:layoutMarginStart")
    public static void setLayoutMarginStart(@NotNull View view, Float marginStart) {
        if (view.layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) view.layoutParams;
            params.setMargins(marginStart.intValue(), params.topMargin, params.marginEnd, params.bottomMargin);
            view.requestLayout();
        }
    }

    /** Used to set a margin-end for views. */
    @BindingAdapter("app:layoutMarginEnd")
    public static void setLayoutMarginEnd(@NotNull View view, Float marginStart) {
        if (view.layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) view.layoutParams;
            params.setMargins(params.marginStart, params.topMargin, marginEnd.intValue(), params.bottomMargin);
            view.requestLayout();
        }
    }

    /** Used to set a margin-top for views. */
    @BindingAdapter("app:layoutMarginTop")
    public static void setLayoutMarginTop(@NotNull View view, Float marginStart) {
        if (view.layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) view.layoutParams;
            params.setMargins(params.marginStart, marginTop.intValue(), params.marginEnd, params.bottomMargin);
            view.requestLayout();
        }
    }

    /** Used to set a margin-bottom for views. */
    @BindingAdapter("app:layoutMarginBottom")
    public static void setLayoutMarginBottom(@NotNull View view, Float marginStart) {
        if (view.layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) view.layoutParams;
            params.setMargins(params.marginStart, params.topMargin, params.marginEnd, marginBottom.intValue());
            view.requestLayout();
        }
    }
}
