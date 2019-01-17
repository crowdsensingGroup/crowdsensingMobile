package com.hyj.privacy_protectedcrowdsensingsystem;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hyj.privacy_protectedcrowdsensingsystem.fragment.GetTaskFragment;
import com.hyj.privacy_protectedcrowdsensingsystem.fragment.PerformingTaskFragment;
import com.hyj.privacy_protectedcrowdsensingsystem.fragment.UserCenterFragment;
import com.hyj.privacy_protectedcrowdsensingsystem.util.PermissionHelper;
import com.hyj.privacy_protectedcrowdsensingsystem.util.PermissionInterface;

public class MainActivity extends AppCompatActivity implements PermissionInterface {

    private PermissionHelper mPermissionHelper;
    private FragmentTabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
        initViews();
//        Intent intent = new Intent(MainActivity.this, RoutePlanDemo.class);
//        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.READ_LOGS,
                Manifest.permission.VIBRATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.WRITE_SETTINGS,
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        //权限请求用户已经全部允许
        //initViews();
    }

    @Override
    public void requestPermissionsFail() {
        //权限请求不被用户允许。可以提示并退出或者提示权限的用途并重新发起权限申请。
        //showNormalDialog();
    }

    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        //normalDialog.setIcon(R.drawable.icon_dialog);
        //normalDialog.setTitle("我是一个普通Dialog");
        normalDialog.setMessage("权限不完整,请手动开启权限！");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        // 显示
        normalDialog.show();
    }

    private void initViews() {
        tabHost = (FragmentTabHost) findViewById(R.id.tab_Host);
        //获取tab的标题
        String[] titles = getResources().getStringArray(R.array.tab_title);
        //背景图
        int[] icons = new int[]{R.drawable.get_selector, R.drawable.performing_selector, R.drawable.user_selector};
        Class[] classes = new Class[]{GetTaskFragment.class, PerformingTaskFragment.class, UserCenterFragment.class};
        //1 绑定 ->fragment显示的容器
        tabHost.setup(this, getSupportFragmentManager(), R.id.content);

        for (int i = 0; i < titles.length; i++) {
            TabHost.TabSpec tmp = tabHost.newTabSpec("" + i);
            tmp.setIndicator(getEveryView(this, titles, icons, i));
            tabHost.addTab(tmp, classes[i], null);
        }
        tabHost.setCurrentTab(1);
    }

    public View getEveryView(Context context, String[] titles, int[] icons, int index) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View title_view = inflater.inflate(R.layout.item_title, null);
        TextView title = (TextView) title_view.findViewById(R.id.title);
        ImageView icon = (ImageView) title_view.findViewById(R.id.icon);
        // 设置标签的内容
        title.setText(titles[index]);
        icon.setImageResource(icons[index]);
        return title_view;
    }

    public FragmentTabHost getTabHost() {
        return tabHost;
    }
}