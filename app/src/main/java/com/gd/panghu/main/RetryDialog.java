package com.gd.panghu.main;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by wangyongchao on 2017/11/27.
 */

public class RetryDialog  {

    private static LeaveMyDialogListener listener;
    public static Dialog createRetryDialog(Context context, String msg, LeaveMyDialogListener l) {

        listener = l;
        // 首先得到整个View
        View view = LayoutInflater.from(context).inflate(
                R.layout.retry_connect_item, null);
        // 获取整个布局
        LinearLayout layout = (LinearLayout) view
                .findViewById(R.id.dialog_view_retry);
//        Resources resources = context.getResources();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        float density = dm.density;
//        int width = dm.widthPixels /2;
//        int height = dm.heightPixels/4;
//
//        layout.setMinimumHeight(height);
//        layout.setMinimumWidth(width);
        // 页面中的Img

        // 页面中显示文本
        TextView tipText = (TextView) view.findViewById(R.id.retryTextView);

        // 加载动画，动画用户使img图片不停的旋转

        // 显示文本
        tipText.setText(msg);

        Button retry_con = (Button) view.findViewById(R.id.retry_connect_btn);
        retry_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDialogClick(v);
            }
        });
        Button retry_cancel = (Button) view.findViewById(R.id.retry_cancel_btn);
        retry_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDialogClick(v);
            }
        });
        // 创建自定义样式的Dialog
        Dialog loadingDialog = new Dialog(context);
        // 设置返回键无效
        //    loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        return loadingDialog;
    }

    public interface LeaveMyDialogListener{
        public void onDialogClick(View view);
    }

}
