package com.moko.beaconx.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.adapter.BeaconXListAdapter;
import com.moko.beaconx.dialog.LoadingDialog;
import com.moko.beaconx.dialog.LoadingMessageDialog;
import com.moko.beaconx.dialog.PasswordDialog;
import com.moko.beaconx.dialog.ScanFilterDialog;
import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.beaconx.utils.BeaconXInfoParseableImpl;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends BaseActivity implements MokoScanDeviceCallback{

    /*
    * Test Commit in Main Activity
    * */

    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    private ConcurrentHashMap<String, BeaconXInfo> beaconXInfoHashMap;
    private CopyOnWriteArrayList<BeaconXInfo> beaconXInfos;
    //private BeaconXListAdapter adapter;
    private String ip_address = "";
    private String user_id = "";
    private boolean enableToggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        beaconXInfoHashMap = new ConcurrentHashMap<>();
        beaconXInfos = new CopyOnWriteArrayList<>();
        mHandler = new CunstomHandler(this);
        //EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mHandler.removeMessages(0);
                                MokoSupport.getInstance().stopScanDevice();
                            }
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.v("have", "stuff");
            if (data.hasExtra("ip_address") && data.hasExtra("user_id")) {
                ip_address = data.getStringExtra("ip_address");
                user_id = data.getStringExtra("user_id");
            }
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                case AppConstants.REQUEST_CODE_DEVICE_INFO:
                    if (animation == null) {
                        startScan();
                    }
                    break;
            }
        } else {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    MainActivity.this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onStartScan() {
        beaconXInfoHashMap.clear();
        beaconXInfos.clear();
//        new Thread(() -> {
//            while (enableToggle != false) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                updateDevices();
//            }
//        }).start();
    }

    private BeaconXInfoParseableImpl beaconXInfoParseable;

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        final BeaconXInfo beaconXInfo = beaconXInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconXInfo == null) {
            return;
        }
        beaconXInfoHashMap.put(beaconXInfo.mac, beaconXInfo);

    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
    }

    private void sendBeaconInfo() {
        // Post to server
        if(!ip_address.isEmpty() && !user_id.isEmpty()) {
            OkHttpClient client = new OkHttpClient();
            RequestBody form_body;
            Log.d("beacons: ", beaconXInfos.toString());
            if(!beaconXInfos.isEmpty()) {
                form_body = new FormBody.Builder().add("user_id", user_id).add("beacons", String.valueOf(beaconXInfos.get(0).mac)).build();
            }
            else{
                form_body = new FormBody.Builder().add("user_id", user_id).add("beacons", "No Nearby Beacons").build();
            }
            Request request = new Request.Builder()
                    .url("http://"+ip_address+"/receive_beacon")
                    .post(form_body)
                    .build();
            Long startTime = System.currentTimeMillis();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("Output: ", "AHHHHHHHH");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String myResponse = response.body().string();
                    Log.d("Response Time: ", String.valueOf((System.currentTimeMillis() - startTime)));
                }
            });
        }
    }

    private void updateDevices() {
        if (!beaconXInfoHashMap.isEmpty()) {
            if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
                ArrayList<BeaconXInfo> beaconXInfosFilter = new ArrayList<>(beaconXInfoHashMap.values());
                Iterator<BeaconXInfo> iterator = beaconXInfosFilter.iterator();
                while (iterator.hasNext()) {
                    BeaconXInfo beaconXInfo = iterator.next();
                    if (beaconXInfo.rssi > filterRssi) {
                        if (TextUtils.isEmpty(filterName)) {
                            continue;
                        } else {
                            if (TextUtils.isEmpty(beaconXInfo.name) && TextUtils.isEmpty(beaconXInfo.mac)) {
                                iterator.remove();
                            } else if (TextUtils.isEmpty(beaconXInfo.name) && beaconXInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())) {
                                continue;
                            } else if (TextUtils.isEmpty(beaconXInfo.mac) && beaconXInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                                continue;
                            } else if (!TextUtils.isEmpty(beaconXInfo.name) && !TextUtils.isEmpty(beaconXInfo.mac) && (beaconXInfo.name.toLowerCase().contains(filterName.toLowerCase()) || beaconXInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase()))) {
                                continue;
                            } else {
                                iterator.remove();
                            }
                        }
                    } else {
                        iterator.remove();
                    }
                }
                    beaconXInfos.addAll(beaconXInfosFilter);
            } else {
                        beaconXInfos.addAll(beaconXInfoHashMap.values());
            }
                Collections.sort(beaconXInfos, new Comparator<BeaconXInfo>() {
                    @Override
                    public int compare(BeaconXInfo lhs, BeaconXInfo rhs) {
                        if (lhs.rssi > rhs.rssi) {
                            return -1;
                        } else if (lhs.rssi < rhs.rssi) {
                            return 1;
                        }
                        return 0;
                    }
                });
        }
    }


    private Animation animation = null;
    public String filterName;
    public int filterRssi = -127;

    @OnClick({R.id.iv_rest, R.id.enable_switch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_rest:
                Intent acti = new Intent(this, RestActivity.class);
                startActivityForResult(acti, 1);
                break;
            case R.id.enable_switch:
                enableToggle = !enableToggle;
                if(enableToggle == true) {
                    if(!ip_address.isEmpty() && !user_id.isEmpty()) {
                        startScan();
                        new Thread() {
                            @Override
                            public void run() {
                                while(enableToggle && !ip_address.isEmpty() && !user_id.isEmpty()) {
                                    try {
                                        onStartScan();
                                        Thread.sleep(5000);
                                        updateDevices();
                                        sendBeaconInfo();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    }
                    else{
                        Context context = getApplicationContext();
                        CharSequence text = "Please key in user ID and address first!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
                else{
                    MokoSupport.getInstance().stopScanDevice();
                }
                break;
        }
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        findViewById(R.id.iv_refresh).startAnimation(animation);
        beaconXInfoParseable = new BeaconXInfoParseableImpl();
        MokoSupport.getInstance().startScanDevice(this);
//        mHandler.postDelayed(new Runnable() {
//           @Override
//           public void run() {
//               MokoSupport.getInstance().stopScanDevice();
//               updateDevices();
//               sendBeaconInfo();
//           }
//         }, 3000);
    }

    public CunstomHandler mHandler;

    public class CunstomHandler extends BaseMessageHandler<MainActivity> {

        public CunstomHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}
