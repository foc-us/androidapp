package us.foc.transcranial.dcs.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import us.foc.transcranial.dcs.R;

/**
 * Displays a key-value pair in the UI as a custom view e.g. "Mode" : "tDCS"
 */
public class ProgramSettingView extends LinearLayout implements ProgramSettingDisplay {

    private TextView psTitle;
    private TextView psValue;

    public ProgramSettingView(Context context) {
        super(context);
        init(null);
    }

    public ProgramSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgramSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.program_setting_button, this, false);

        psTitle = (TextView) view.findViewById(R.id.ps_title);
        psValue = (TextView) view.findViewById(R.id.ps_value);

        if (attrs != null) {
            TypedArray a = getContext().getResources().obtainAttributes(attrs, R.styleable.ProgramSettingView);

            try {
                String settingName = a.getString(R.styleable.ProgramSettingView_setting_name);
                String settingValue = a.getString(R.styleable.ProgramSettingView_setting_value);

                psTitle.setText(settingName);
                psValue.setText(settingValue);
            }
            finally {
                a.recycle();
            }
        }

        this.setClickable(true);
        this.setFocusable(true);

        this.addView(view);
    }

    public void setPsTitle(CharSequence title) {
        this.psTitle.setText(title);
    }

    public void setPsValue(CharSequence value) {
        this.psValue.setText(value);
    }

    @Override public void setValueOn(boolean on) {
        int color = (on) ? R.color.ps_default : R.color.ps_btn_dark_default;
        psValue.setTextColor(getContext().getResources().getColor(color));
    }

}
