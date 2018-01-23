package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconx.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    @Bind(R.id.iv_slot1)
    ImageView ivSlot1;
    @Bind(R.id.tv_slot1)
    TextView tvSlot1;
    @Bind(R.id.rl_slot1)
    RelativeLayout rlSlot1;
    @Bind(R.id.iv_slot2)
    ImageView ivSlot2;
    @Bind(R.id.tv_slot2)
    TextView tvSlot2;
    @Bind(R.id.rl_slot2)
    RelativeLayout rlSlot2;
    @Bind(R.id.iv_slot3)
    ImageView ivSlot3;
    @Bind(R.id.tv_slot3)
    TextView tvSlot3;
    @Bind(R.id.rl_slot3)
    RelativeLayout rlSlot3;
    @Bind(R.id.iv_slot4)
    ImageView ivSlot4;
    @Bind(R.id.tv_slot4)
    TextView tvSlot4;
    @Bind(R.id.rl_slot4)
    RelativeLayout rlSlot4;
    @Bind(R.id.iv_slot5)
    ImageView ivSlot5;
    @Bind(R.id.tv_slot5)
    TextView tvSlot5;
    @Bind(R.id.rl_slot5)
    RelativeLayout rlSlot5;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_slot1, R.id.rl_slot2, R.id.rl_slot3, R.id.rl_slot4, R.id.rl_slot5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_slot1:
                break;
            case R.id.rl_slot2:
                break;
            case R.id.rl_slot3:
                break;
            case R.id.rl_slot4:
                break;
            case R.id.rl_slot5:
                break;
        }
    }
}
