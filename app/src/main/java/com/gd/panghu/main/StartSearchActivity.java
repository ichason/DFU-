package com.gd.panghu.main;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.data.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class StartSearchActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private static final int RETRY_SCAN = 1;
    private ImageView img_loading;
    private Animation operatingAnim;
    private static BluetoothService mBluetoothService;
    private ResultAdapter mResultAdapter;
    private boolean isConnectAction;
    private Dialog dialog;
    private String mac;
    private ListView listView_device;
    public static final String MAC_ADDRESS = "mac address";
    public static final String DEVICE_NAME = "device name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_search);

        isConnectAction = false;
        ImageButton button = (ImageButton) findViewById(R.id.back_button);
        button.setOnClickListener(this);
        img_loading = (ImageView) findViewById(R.id.img_loading);
        img_loading.setVisibility(View.INVISIBLE);
        operatingAnim = AnimationUtils.loadAnimation(this,R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        dialog = LoadingDialog.createLoadingDialog(this,"Connecting");
        dialog.setCanceledOnTouchOutside(false);

        mResultAdapter = new ResultAdapter(this);
        listView_device = (ListView) findViewById(R.id.search_list);
        listView_device.setAdapter(mResultAdapter);
        listView_device.setOnItemClickListener(this);

        checkPermissions();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (mBluetoothService != null) {

                mResultAdapter.notifyDataSetChanged();
                dialog = LoadingDialog.createLoadingDialog(this, "Connecting");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                isConnectAction = true;
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                mac = mResultAdapter.getItem(position).getDevice().getAddress();
                mBluetoothService.scanAndConnect5(mac,15000);

        }
    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<ScanResult> scanResultList;

        ResultAdapter(Context context) {
            this.context = context;
            scanResultList = new ArrayList<>();
        }

        public List<ScanResult> getScanResultList() {
            return scanResultList;
        }

        void addResult(ScanResult result) {
            scanResultList.add(result);
        }

        void clear() {
            scanResultList.clear();
        }

        @Override
        public int getCount() {
            return scanResultList.size();
        }

        @Override
        public ScanResult getItem(int position) {
            if (position > scanResultList.size())
                return null;
            return scanResultList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ResultAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (ResultAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.bluetooth_list, null);
                holder = new ResultAdapter.ViewHolder();
                convertView.setTag(holder);
               holder.txt_name = (TextView) convertView.findViewById(R.id.scan_device_name);
                holder.txt_mac = (TextView) convertView.findViewById(R.id.mac_text);
                holder.txt_rssi = (TextView)convertView.findViewById(R.id.tv_rssi);
            }

            ScanResult result = scanResultList.get(position);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String mac = device.getAddress();
            int rssi = result.getRssi();
            holder.txt_name.setText(name);
              holder.txt_mac.setText(mac);
            holder.txt_rssi.setText(String.valueOf(rssi)+"dB");
            return convertView;
        }

        class ViewHolder {
            TextView txt_name;
            TextView txt_mac;
           TextView txt_rssi;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RETRY_SCAN:
                if(resultCode == RESULT_OK){
                    mac = "";
                    checkPermissions();
                }
                break;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 12:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }


    //事先绑定服务
    private void bindService() {

        Intent bindIntent = new Intent(StartSearchActivity.this, BluetoothService.class);
        this.bindService(bindIntent, mFhrSCon, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        this.unbindService(mFhrSCon);
    }

    private ServiceConnection mFhrSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();

            //服务设置扫描回调
            mBluetoothService.setScanCallback(callback);

               Handler handler = new Handler();
               handler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       if(mBluetoothService!=null && mBluetoothService.isBlueEnable()) {

                               mBluetoothService.scanDevice();

                       }
                   }
               }, 1000);
           }



        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    private BluetoothService.Callback callback = new BluetoothService.Callback() {
        @Override
        public void onStartScan() {
            if(!isConnectAction) {
                mResultAdapter.clear();
                mResultAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void onScanning(ScanResult result) {

            BluetoothDevice device = result.getDevice();
            mResultAdapter.addResult(result);
            mResultAdapter.notifyDataSetChanged();

        }

        @Override
        public void onScanComplete() {
            if(!isConnectAction){
                if(mResultAdapter.getCount()>0) {
                    img_loading.clearAnimation();
                    img_loading.setVisibility(View.INVISIBLE);
                }else{
                        Intent intent = new Intent(StartSearchActivity.this, RetryScanActivity.class);
                        startActivityForResult(intent, RETRY_SCAN);
                }
            }

        }

        @Override
        public void onConnecting() {



        }

        @Override
        public void onConnectFail() {


                img_loading.clearAnimation();
                dialog.dismiss();
                Toast.makeText(StartSearchActivity.this, "Connect fail", Toast.LENGTH_LONG).show();
                mBluetoothService.cancelScan();

        }

        @Override
        public void onDisConnected() {
            dialog.dismiss();
            mResultAdapter.clear();
            mResultAdapter.notifyDataSetChanged();
            img_loading.clearAnimation();
            Toast.makeText(StartSearchActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServicesDiscovered() {
            if(dialog != null){
                if(dialog.isShowing()){
                    dialog.dismiss();
                }
            }
            //发现可用服务的时候，跳转dfu
            Log.e("StartSeratchActivity", "onServicesDiscovered " );
            Intent intent = new Intent(StartSearchActivity.this,DfuUpdateActivity.class);
            intent.putExtra(MAC_ADDRESS,mac);
            intent.putExtra(DEVICE_NAME,mBluetoothService.getName());
            startActivity(intent);
        }
    };


    /**
     * 检查权限
     */
    private void checkPermissions(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        //若有权限未申请，则事先申请权限
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, 12);
        }

    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (mBluetoothService == null) {
                    //开始检查权限， 如果获取到权限则进行
                    bindService();
                }else {
                    if("".equals(mac)){
                        mBluetoothService.scanDevice();
                    }else{
                        img_loading.clearAnimation();
                        img_loading.setVisibility(View.INVISIBLE);
                        dialog = LoadingDialog.createLoadingDialog(this,"Connecting");
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        mBluetoothService.scanAndConnect5(mac,15000);
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        img_loading.clearAnimation();

        if (mBluetoothService != null) {
            unbindService();
            mBluetoothService = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_button:

                finish();

                break;
        }
    }


}
