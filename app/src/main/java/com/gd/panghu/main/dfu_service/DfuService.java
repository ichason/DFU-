package com.gd.panghu.main.dfu_service;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;


import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
       return NotificationActivity.class;
    }

    @Override
    protected boolean isDebug() {
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }
}
