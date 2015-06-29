package us.foc.transcranial.dcs.ui;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Android viewpager programmatic scroll speed is far too fast, this fixes the duration
 * <p/>
 * See http://stackoverflow.com/questions/8155257
 */
public class SlowScroller extends Scroller {

    private static final int DURATION_MS = 575;

    public SlowScroller(Context context) {
        super(context);
    }

    public SlowScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public SlowScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, DURATION_MS);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, DURATION_MS);
    }

}
