package com.gd.panghu.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RetryScanActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retry_scan);
        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);
        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(this);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //启动一个意图,回到桌面
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.retry_button:
                SharedPreferences sp = getSharedPreferences("mac_address", Context.MODE_PRIVATE);
                String mac = sp.getString("mac_name","");
                int i = sp.getInt("retry_count",0);
                if(!"".equals(mac)) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("retry_count",i+1);
                    editor.apply();
                }
            case R.id.back_button:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
