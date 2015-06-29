package us.foc.transcranial.dcs.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Boilerplate for dialogfragment with ButterKnife injection
 */
public abstract class BaseDialogFragment extends DialogFragment {

    protected SaveEventListener listener;

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResId(), container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    protected abstract @LayoutRes int getLayoutResId();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override public void onResume() {
        super.onResume();
        // dialogfragment doesn't correctly wrap width so require workaround

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                                          ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setSaveEventListener(SaveEventListener listener) {
        this.listener = listener;
    }

}
