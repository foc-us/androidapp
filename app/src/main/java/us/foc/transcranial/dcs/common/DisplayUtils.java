package us.foc.transcranial.dcs.common;

import android.content.res.Resources;
import android.util.TypedValue;

public class DisplayUtils {

    public static float dpToPx(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                px,
                Resources.getSystem().getDisplayMetrics());
    }
}
