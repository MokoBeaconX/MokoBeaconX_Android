package com.moko.beaconx.adapter;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.beaconx.R;
import com.moko.beaconx.entity.BeaconXDevice;
import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.beaconx.entity.BeaconXTLM;
import com.moko.beaconx.entity.BeaconXUID;
import com.moko.beaconx.entity.BeaconXURL;
import com.moko.beaconx.entity.BeaconXiBeacon;
import com.moko.beaconx.utils.BeaconXParser;
import com.moko.support.log.LogModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconx.adapter.BeaconXListAdapter
 */
public class BeaconXListAdapter extends BaseQuickAdapter<BeaconXInfo, BaseViewHolder> {

    public BeaconXListAdapter() {
        super(R.layout.list_item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, BeaconXInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, item.rssi + "");
        helper.addOnClickListener(R.id.tv_connect);
        helper.setText(R.id.tv_conn_state, "");
        LogModule.i(item.toString());
        LinearLayout llData = helper.getView(R.id.ll_data);
        llData.removeAllViews();
        ArrayList<BeaconXInfo.ValidData> validDatas = new ArrayList<>(item.validDataHashMap.values());
        Collections.sort(validDatas, new Comparator<BeaconXInfo.ValidData>() {
            @Override
            public int compare(BeaconXInfo.ValidData lhs, BeaconXInfo.ValidData rhs) {
                if (lhs.type > rhs.type) {
                    return 1;
                } else if (lhs.type < rhs.type) {
                    return -1;
                }
                return 0;
            }
        });
        for (BeaconXInfo.ValidData validData : validDatas) {
            LogModule.i(validData.toString());
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_UID) {
                llData.addView(createUIDView(BeaconXParser.getUID(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_URL) {
                llData.addView(createURLView(BeaconXParser.getURL(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
                llData.addView(createTLMView(BeaconXParser.getTLM(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                llData.addView(createiBeaconView(BeaconXParser.getiBeacon(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO) {
                BeaconXDevice beaconXDevice = BeaconXParser.getDevice(validData.data);
                int battery = Integer.parseInt(beaconXDevice.battery);
                if (battery >= 0 && battery <= 20) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_5);
                }
                if (battery > 20 && battery <= 40) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_4);
                }
                if (battery > 40 && battery <= 60) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_3);
                }
                if (battery > 60 && battery <= 80) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_2);
                }
                if (battery > 80 && battery <= 100) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_1);
                }
                if (Integer.parseInt(beaconXDevice.isConnected) == 0) {
                    helper.setText(R.id.tv_conn_state, "UNCON");
                } else {
                    helper.setText(R.id.tv_conn_state, "CON");
                }
                LogModule.i(beaconXDevice.toString());
            }
        }
    }


    private View createUIDView(BeaconXUID uid) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_uid, null);
        TextView tvTxPower = view.findViewById(R.id.tv_tx_power);
        TextView tvNameSpace = view.findViewById(R.id.tv_namespace);
        TextView tvInstanceId = view.findViewById(R.id.tv_instance_id);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", uid.rangingData));
        tvNameSpace.setText(uid.namespace.toUpperCase());
        tvInstanceId.setText(uid.instanceId.toUpperCase());
        return view;
    }

    private View createURLView(final BeaconXURL url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_url, null);
        TextView tvTxPower = view.findViewById(R.id.tv_tx_power);
        TextView tvUrl = view.findViewById(R.id.tv_url);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", url.rangingData));
        tvUrl.setText(url.url);
        tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvUrl.getPaint().setAntiAlias(true);//抗锯齿
        tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(url.url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    private View createTLMView(BeaconXTLM tlm) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_tlm, null);
        TextView tv_vbatt = view.findViewById(R.id.tv_vbatt);
        TextView tv_temp = view.findViewById(R.id.tv_temp);
        TextView tv_adv_cnt = view.findViewById(R.id.tv_adv_cnt);
        TextView tv_sec_cnt = view.findViewById(R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createiBeaconView(BeaconXiBeacon iBeacon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_ibeacon, null);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_uuid = view.findViewById(R.id.tv_uuid);
        TextView tv_major = view.findViewById(R.id.tv_major);
        TextView tv_minor = view.findViewById(R.id.tv_minor);
        tv_tx_power.setText("0".equals(iBeacon.rangingData) ? String.format("RSSI@1m:%sdBm", iBeacon.rangingData) :
                String.format("RSSI@1m:-%sdBm", iBeacon.rangingData));
        tv_uuid.setText(iBeacon.uuid.toUpperCase());
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }
}
