package com.moko.beaconx.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.moko.beaconx.R;
import com.moko.beaconx.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class RestActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        ButterKnife.bind(this);
        EditText editIpField = (EditText) findViewById(R.id.ipadd);
        EditText editStaffIdField = (EditText) findViewById(R.id.staffid);
        EditText editRepeatCountField = (EditText) findViewById(R.id.repeat);
        TextView textField = (TextView) findViewById(R.id.settings);
        TextViewChange(editIpField, editStaffIdField, editRepeatCountField, textField);
    }

    @OnClick({R.id.tv_back, R.id.tv_company_website})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_company_website:
                Uri uri = Uri.parse("https://" + getString(R.string.company_website));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }

    public void TextViewChange(EditText editIpField, EditText editStaffIdField, EditText editRepeatCountField,TextView textField) {
        editIpField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String staffId = "";
                String repeatCount = "";
                if(editStaffIdField.getText().toString().equals("")){staffId="not set";}else{staffId=editStaffIdField.getText().toString();}
                if(editRepeatCountField.getText().toString().equals("")){repeatCount="not set";}else{repeatCount=editRepeatCountField.getText().toString();}
                if(s.length() != 0)
                    textField.setText("IP Address: " + s.toString() + ", Staff ID: " + staffId + ", Repeat Count: " + repeatCount);
            }
        });

        editStaffIdField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String ipAddress = "";
                String repeatCount = "";
                if(editIpField.getText().toString().equals("")){ipAddress ="not set";}else{ipAddress =editIpField.getText().toString();}
                if(editRepeatCountField.getText().toString().equals("")){repeatCount="not set";}else{repeatCount=editRepeatCountField.getText().toString();}
                if(s.length() != 0)
                    textField.setText("IP Address: " + ipAddress + ", Staff ID: " + s.toString() + ", Repeat Count: " + repeatCount);
            }
        });

        editRepeatCountField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String staffId = "";
                String ipAddress = "";
                if(editIpField.getText().toString().equals("")){ipAddress ="not set";}else{ipAddress =editIpField.getText().toString();}
                if(editStaffIdField.getText().toString().equals("")){staffId="not set";}else{staffId=editStaffIdField.getText().toString();}
                if(s.length() != 0)
                    textField.setText("IP Address: " + ipAddress + ", Staff ID: " + staffId + ", Repeat Count: " + s.toString());
            }
        });
    }
}
