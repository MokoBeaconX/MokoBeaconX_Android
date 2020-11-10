package com.moko.beaconx.dialog;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.beaconx.R;
import com.moko.support.entity.UrlSchemeEnum;

import butterknife.BindView;
import butterknife.OnClick;

public class UrlSchemeDialog extends BaseDialog<String> {

    @BindView(R.id.rg_url_scheme)
    RadioGroup rgUrlScheme;
    @BindView(R.id.rb_http_www)
    RadioButton rbHttpWww;
    @BindView(R.id.rb_https_www)
    RadioButton rbHttpsWww;
    @BindView(R.id.rb_http)
    RadioButton rbHttp;
    @BindView(R.id.rb_https)
    RadioButton rbHttps;

    public UrlSchemeDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_url_scheme;
    }

    @Override
    protected void renderConvertView(View convertView, String urlScheme) {
        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlDesc(urlScheme);
        switch (urlSchemeEnum.getUrlType()) {
            case 0:
                rbHttpWww.setChecked(true);
                break;
            case 1:
                rbHttpsWww.setChecked(true);
                break;
            case 2:
                rbHttp.setChecked(true);
                break;
            case 3:
                rbHttps.setChecked(true);
                break;
        }
    }

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_ensure:
                dismiss();
                urlSchemeClickListener.onEnsureClicked((String) findViewById(rgUrlScheme.getCheckedRadioButtonId()).getTag());
                break;
        }
    }

    private UrlSchemeClickListener urlSchemeClickListener;

    public void setUrlSchemeClickListener(UrlSchemeClickListener urlSchemeClickListener) {
        this.urlSchemeClickListener = urlSchemeClickListener;
    }

    public interface UrlSchemeClickListener {

        void onEnsureClicked(String urlType);
    }
}
