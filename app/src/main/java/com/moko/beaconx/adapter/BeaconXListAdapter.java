package com.moko.beaconx.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconx.adapter.BeaconXListAdapter
 */
public class BeaconXListAdapter extends MokoBaseAdapter<BeaconXInfo> {

    public BeaconXListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindViewHolder(int position, ViewHolder viewHolder, View convertView, ViewGroup parent) {
        final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
        final BeaconXInfo device = getItem(position);
        setView(holder, device);
    }

    private void setView(DeviceViewHolder holder, BeaconXInfo device) {
        holder.tvName.setText(TextUtils.isEmpty(device.name) ? "N/A" : device.name);
        holder.tvMac.setText(device.mac);
        holder.tvRssi.setText(device.rssi + "");
        holder.tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogModule.i("连接");
            }
        });
        LogModule.i(device.toString());
        holder.llData.removeAllViews();
        ArrayList<BeaconXInfo.ValidData> validDatas = new ArrayList<>(device.validDataHashMap.values());
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
                holder.llData.addView(createUIDView(BeaconXParser.getUID(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_URL) {
                holder.llData.addView(createURLView(BeaconXParser.getURL(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
                holder.llData.addView(createTLMView(BeaconXParser.getTLM(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                holder.llData.addView(createiBeaconView(BeaconXParser.getiBeacon(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO) {
                BeaconXDevice beaconXDevice = BeaconXParser.getDevice(validData.data);
                device.name = beaconXDevice.deviceName;
                holder.tvName.setText(TextUtils.isEmpty(device.name) ? "N/A" : device.name);
                int battery = Integer.parseInt(beaconXDevice.battery);
                if (battery >= 0 && battery <= 20) {
                    holder.ivBattery.setImageResource(R.drawable.battery_5);
                }
                if (battery > 20 && battery <= 40) {
                    holder.ivBattery.setImageResource(R.drawable.battery_4);
                }
                if (battery > 40 && battery <= 60) {
                    holder.ivBattery.setImageResource(R.drawable.battery_3);
                }
                if (battery > 60 && battery <= 80) {
                    holder.ivBattery.setImageResource(R.drawable.battery_2);
                }
                if (battery > 80 && battery <= 100) {
                    holder.ivBattery.setImageResource(R.drawable.battery_1);
                }
                LogModule.i(beaconXDevice.toString());
            }
        }
    }

    private View createUIDView(BeaconXUID uid) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_uid, null);
        TextView tvTxPower = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tvNameSpace = ButterKnife.findById(view, R.id.tv_namespace);
        TextView tvInstanceId = ButterKnife.findById(view, R.id.tv_instance_id);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", uid.rangingData));
        tvNameSpace.setText(uid.namespace);
        tvInstanceId.setText(uid.instanceId);
        return view;
    }

    private View createURLView(BeaconXURL url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_url, null);
        TextView tvTxPower = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tvUrl = ButterKnife.findById(view, R.id.tv_url);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", url.rangingData));
        tvUrl.setText(url.url);
        return view;
    }

    private View createTLMView(BeaconXTLM tlm) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_tlm, null);
        TextView tv_vbatt = ButterKnife.findById(view, R.id.tv_vbatt);
        TextView tv_temp = ButterKnife.findById(view, R.id.tv_temp);
        TextView tv_adv_cnt = ButterKnife.findById(view, R.id.tv_adv_cnt);
        TextView tv_sec_cnt = ButterKnife.findById(view, R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createiBeaconView(BeaconXiBeacon iBeacon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_ibeacon, null);
        TextView tv_tx_power = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tv_uuid = ButterKnife.findById(view, R.id.tv_uuid);
        TextView tv_major = ButterKnife.findById(view, R.id.tv_major);
        TextView tv_minor = ButterKnife.findById(view, R.id.tv_minor);
        tv_tx_power.setText(String.format("RSSI@1m:%sdBm", iBeacon.rangingData));
        tv_uuid.setText(iBeacon.uuid);
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }

    @Override
    protected ViewHolder createViewHolder(int position, LayoutInflater inflater, ViewGroup parent) {
        final View convertView = inflater.inflate(R.layout.list_item_device, parent, false);
        return new DeviceViewHolder(convertView);
    }

    static class DeviceViewHolder extends ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_rssi)
        TextView tvRssi;
        @Bind(R.id.tv_connect)
        TextView tvConnect;
        @Bind(R.id.iv_battery)
        ImageView ivBattery;
        @Bind(R.id.tv_mac)
        TextView tvMac;
        @Bind(R.id.ll_data)
        LinearLayout llData;

        public DeviceViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);
        }
    }
}
