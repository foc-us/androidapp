package us.foc.transcranial.dcs.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import us.foc.transcranial.dcs.R;

/**
 * A custom view which provides a button for paging in the ViewPager
 */
public class NavButtonView extends View {

    private static final float CIRCLE_RADIUS_DP = 18.0f;
    private static final float ARROW_WIDTH_DP = 12.0f;
    private static final float ARROW_HEIGHT_DP = 10.0f;
    private static final float WIDTH_OFFSET_DP = 0.75f; // shift triangle slightly offcentre

    private float circleRadiusPx;
    private float arrowWidthOffsetPx;
    private float arrowHeightOffsetPx;
    private float widthOffsetPx;

    private Paint bgPaint;
    private Paint arrowPaint;
    private Path arrowPath;

    @ColorInt private int defaultArrowColor;
    @ColorInt private int activeArrowColor;
    @ColorInt private int defaultBgColor;
    @ColorInt private int activeBgColor;

    public NavButtonView(Context context) {
        super(context);
        init();
    }

    public NavButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Resources resources = getResources();

        defaultArrowColor = resources.getColor(R.color.icon_active);
        activeArrowColor = resources.getColor(R.color.icon_white);
        defaultBgColor = resources.getColor(R.color.ps_btn_dark_active);
        activeBgColor = resources.getColor(R.color.ps_btn_dark_default);

        circleRadiusPx = pixelsFromDpValue(CIRCLE_RADIUS_DP);
        arrowWidthOffsetPx = pixelsFromDpValue(ARROW_WIDTH_DP) / 2;
        arrowHeightOffsetPx = pixelsFromDpValue(ARROW_HEIGHT_DP) / 2;
        widthOffsetPx = pixelsFromDpValue(WIDTH_OFFSET_DP);

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);

        arrowPaint = new Paint();
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setAntiAlias(true);

        arrowPath = new Path();
        setDrawStatePressed(false);
    }

    private float pixelsFromDpValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                         dpValue, getResources().getDisplayMetrics());
    }

    private void setDrawStatePressed(boolean pressed) {
        if (pressed) {
            bgPaint.setColor(activeBgColor);
            arrowPaint.setColor(activeArrowColor);
        }
        else {
            bgPaint.setColor(defaultBgColor);
            arrowPaint.setColor(defaultArrowColor);
        }
        invalidate();
    }

    @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                setDrawStatePressed(true);
                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                setDrawStatePressed(false);
                return true;

            case MotionEvent.ACTION_CANCEL:
                setDrawStatePressed(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float center = getWidth() / 2;
        float x = center + widthOffsetPx;
        arrowPath.reset();

        arrowPath.moveTo(x + arrowWidthOffsetPx, center);
        arrowPath.lineTo(x - arrowHeightOffsetPx, center - arrowWidthOffsetPx);
        arrowPath.lineTo(x - arrowHeightOffsetPx, center + arrowWidthOffsetPx);
    }

    @Override protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, circleRadiusPx, bgPaint);
        canvas.drawPath(arrowPath, arrowPaint);
    }

}
