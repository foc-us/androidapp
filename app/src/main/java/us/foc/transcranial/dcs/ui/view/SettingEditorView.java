package us.foc.transcranial.dcs.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.common.DisplayUtils;
import us.foc.transcranial.dcs.model.ProgramEntity;
import us.foc.transcranial.dcs.model.ProgramSetting;
import us.foc.transcranial.dcs.model.ProgramSettingsBridge;
import us.foc.transcranial.dcs.model.events.SettingEditEvent;
import us.foc.transcranial.dcs.ui.fragments.SettingsEditEventListener;

/**
 * A scrollable view which displays all the settings for a program, and allows the user to edit them
 */
public class SettingEditorView extends ScrollView implements SettingsEditor {

    private static final int bottomPaddingPx = (int) DisplayUtils.dpToPx(8);

    private static final LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private int childViewCount;

    @InjectView(R.id.left_column) ViewGroup leftColumn;
    @InjectView(R.id.right_column) ViewGroup rightColumn;

    private SettingsEditEventListener listener;

    public SettingEditorView(Context context) {
        super(context);
        init();
    }

    public SettingEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_settings_editor, this, false);
        ButterKnife.inject(this, view);
        this.addView(view);

        childParams.setMargins(0, 0, 0, bottomPaddingPx);
    }

    @Override public void setProgramEntity(ProgramEntity program) {
        leftColumn.removeAllViews();
        rightColumn.removeAllViews();
        childViewCount = 0;

        for (ProgramSettingsBridge.SettingsEntity settings : new ProgramSettingsBridge(program).constructSettingsList(getContext())) {
            addSettingChild(settings.getTitle(), settings.getValue(), settings.getProgramSetting());
        }
    }

    @Override public void setSettingEditEventListener(SettingsEditEventListener listener) {
        this.listener = listener;
    }

    private void addSettingChild(String title, String value, ProgramSetting setting) {
        ProgramSettingView psView = new ProgramSettingView(getContext());

        psView.setPsTitle(title);
        psView.setPsValue(value);
        psView.setTag(setting);
        psView.setLayoutParams(childParams);

        psView.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                ProgramSetting setting = (ProgramSetting) v.getTag();

                if (listener != null) {
                    listener.onSettingEditEvent(new SettingEditEvent(setting));
                }
            }
        });

        String onText = getContext().getString(R.string.boolean_off);

        if (onText.equals(value)) {
            psView.setValueOn(false);
        }

        ViewGroup column = (childViewCount % 2 == 0) ? leftColumn : rightColumn;
        column.addView(psView);
        childViewCount++;
    }
}
