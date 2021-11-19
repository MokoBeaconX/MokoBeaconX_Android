package com.moko.beaconx.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class RestActivity extends BaseActivity{

    private String ip_address = "";
    private String user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        ButterKnife.bind(this);
        ip_address = "";
        user_id = "";
    }

    @OnClick({R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("ip_address", ip_address);
                returnIntent.putExtra("user_id", user_id);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
        }
    }

    @OnTextChanged({R.id.iv_ipadd, R.id.iv_staffid})
    public void onTextChanged(CharSequence text) {
                View view = getCurrentFocus();
                if(view.getId() == R.id.iv_ipadd) {
                    final TextView textViewToChange = findViewById(R.id.address);
                    textViewToChange.setText(text.toString());
                    ip_address = text.toString();
                }
                else if (view.getId() == R.id.iv_staffid){
                    final TextView textViewToChange2 = findViewById(R.id.s_id);
                    textViewToChange2.setText(text.toString());
                    user_id = text.toString();
                }
    }
}
