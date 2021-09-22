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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        ButterKnife.bind(this);
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

    @OnTextChanged({R.id.iv_ipadd})
    public void onTextChanged(CharSequence text) {
                final TextView textViewToChange = (TextView) findViewById(R.id.address);
                textViewToChange.setText(text.toString());
    }
}
