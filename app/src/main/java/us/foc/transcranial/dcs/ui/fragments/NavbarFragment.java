package us.foc.transcranial.dcs.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.common.BusProvider;
import us.foc.transcranial.dcs.model.events.NavbarClickEvent;
import us.foc.transcranial.dcs.model.events.NavbarEnableEvent;
import us.foc.transcranial.dcs.model.events.NavbarUpdateEvent;

/**
 * A fragment which acts as an overlay on top of a viewpager, providing navigation buttons
 */
public class NavbarFragment extends Fragment {

    public static NavbarFragment newInstance(Bundle args) {
        NavbarFragment fragment = new NavbarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.previous_program_button) View previousProgram;
    @InjectView(R.id.next_program_button) View nextProgram;

    @OnClick(R.id.previous_program_button) void onPreviousProgramClick() {
        BusProvider.instance().post(new NavbarClickEvent(false));
    }

    @OnClick(R.id.next_program_button) void onNextProgramClick() {
        BusProvider.instance().post(new NavbarClickEvent(true));
    }

    @Override public void onStart() {
        super.onStart();
        BusProvider.instance().register(this);
    }

    @Override public void onStop() {
        super.onStop();
        BusProvider.instance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navbar_overlay, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Subscribe public void onNavbarUpdateEvent(NavbarUpdateEvent event) {
        updateNavbarIcon(previousProgram, event.isDisplayPrevious());
        updateNavbarIcon(nextProgram, event.isDisplayNext());
    }

    @Subscribe public void onNavbarUpdateEvent(NavbarEnableEvent event) {
        updateNavbarIcon(previousProgram, event.isEnabled());
        updateNavbarIcon(nextProgram, event.isEnabled());
    }

    private void updateNavbarIcon(View view, boolean visible) {
        if (view != null) {
            Animation anim = null;

            if (visible && view.getVisibility() == View.GONE) { // fade in
                anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
            }
            else if (!visible && view.getVisibility() == View.VISIBLE) { // fade out
                anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            }

            if (anim != null) {
                view.startAnimation(anim);
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

}
