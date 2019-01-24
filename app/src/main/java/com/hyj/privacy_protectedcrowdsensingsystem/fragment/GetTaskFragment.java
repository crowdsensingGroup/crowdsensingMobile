package com.hyj.privacy_protectedcrowdsensingsystem.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.hyj.privacy_protectedcrowdsensingsystem.MainActivity;
import com.hyj.privacy_protectedcrowdsensingsystem.R;
import com.hyj.privacy_protectedcrowdsensingsystem.util.HttpPostUtils;

import static android.content.Context.MODE_PRIVATE;


public class GetTaskFragment extends Fragment {

    private MapView mMapView;
    private TextView taskInfoTextView;
    private Button buttonAccept;
    private Button buttonGet;

    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();

    private SharedPreferences share;


    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);
    private Marker mMarkerA;
    private MarkerOptions ooA;
    private PoiSearch mPoiSearch;
    private MyPoiSearchListenner myPoiListener = new MyPoiSearchListenner();
    private StringBuffer textString = new StringBuffer();

    boolean isFirstPoi = true;
    boolean isFirstLocation = true;
    float curLat = 0, curLng = 0;
    float taskLat = 0, taskLng = 0;
    String taskType;
    int taskId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        View view = inflater.inflate(R.layout.fragment_get, container, false);

        mMapView = (MapView) view.findViewById(R.id.bmapView);
        taskInfoTextView = (TextView) view.findViewById(R.id.textView);
        buttonAccept = (Button) view.findViewById(R.id.accept);
        buttonGet = (Button) view.findViewById(R.id.get);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("获取任务");
        mBaiduMap = mMapView.getMap();
        LatLng cenpt = new LatLng(32.083136, 118.647906);
        MapStatus mapStatus = new MapStatus.Builder().target(cenpt).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        mBaiduMap.setMyLocationEnabled(true);

        share = getActivity().getSharedPreferences("", MODE_PRIVATE);
        if (share.getFloat("latitude", 0) != 0) {
            onResume();
        }
        /*Toast.makeText(getActivity(), "正在获取任务。。。", Toast.LENGTH_LONG).show();
        Handler handler = new Handler();
        handler.postDelayed(new splashhandler(), 2000);*/

       /* buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskLat == 0) {
                    Toast.makeText(getActivity(), "定位异常，请重新获取任务。。。", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor editor = share.edit();
                editor.putFloat("latitude", taskLat);
                editor.putFloat("longitude", taskLng);
                editor.apply();
                ((MainActivity) getActivity()).getTabHost().setCurrentTab(1);
            }
        });

        buttonRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "正在重新获取任务。。。", Toast.LENGTH_LONG).show();
                onResume();
                Handler handler = new Handler();
                handler.postDelayed(new splashhandler(), 2000);
            }
        });*/

        mLocClient = new LocationClient(getActivity());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);
        option.setIsNeedLocationPoiList(true);
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(myListener);

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(myPoiListener);
        mLocClient.start();

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userLoc = "latitude=" + curLat + "&longitude=" + curLng;
                    /*这里需要留意的是httpPostUtils请求在Android里面不能放在主线程里面，必须新建一个子线程，然后通过Hanlder把子线程的值传过来更新UI（因为子线程不能直接更改UI）*/
                new Thread() {
                    public void run() {
                        String response = HttpPostUtils.doPostRequest("/task/getTask", userLoc);
                        Message message = Message.obtain();
                        message.obj = response;
                        getHanlder.sendMessage(message);
                    }
                }.start();
            }
        });

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskLat == 0) {
                    Toast.makeText(getActivity(), "定位异常，请重新获取任务。。。", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String requestData = "latitude=" + curLat + "&longitude=" + curLng + "&taskId=" + taskId;
                    /*这里需要留意的是httpPostUtils请求在Android里面不能放在主线程里面，必须新建一个子线程，然后通过Hanlder把子线程的值传过来更新UI（因为子线程不能直接更改UI）*/
                new Thread() {
                    public void run() {
                        String response = HttpPostUtils.doPostRequest("/task/acceptTask", requestData);
                        Message message = Message.obtain();
                        message.obj = response;
                        acceptHanlder.sendMessage(message);
                    }
                }.start();
            }
        });
    }

    private String result;

    private Handler getHanlder = new Handler() {
        @Override
        public void handleMessage(Message message) {
            result = (String) message.obj;
            if (result == null) {
                System.out.println("连接边缘节点失败");
                return;
            }
            JSONObject resultJson = JSON.parseObject(result);
            taskLat = resultJson.getFloat("latitude");
            taskLng = resultJson.getFloat("longitude");
            taskType = resultJson.getString("taskType");
            taskId = resultJson.getInteger("taskId");
            System.out.println("收到服务器任务，维度：" + taskLat + ",经度：" + taskLng);

            textString.append("\n 维度区间 : ");
            textString.append((float) (Math.round((taskLat - 0.0001) * 10000)) / 10000 + "-" + (float) (Math.round((taskLat + 0.0001) * 10000)) / 10000);
            textString.append("\n 经度区间 : ");
            textString.append((float) (Math.round((taskLng - 0.0001) * 10000)) / 10000 + "-" + (float) (Math.round((taskLng + 0.0001) * 10000)) / 10000);
            textString.append("\n 任务距离 : " + Math.round(DistanceUtil.getDistance(new LatLng(curLat, curLng), new LatLng(taskLat, taskLng)) * 10) / 10 + "米");
            textString.append("\n 任务内容 : " + taskType);

            PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
            nearbySearchOption.location(new LatLng(taskLat, taskLng));
            nearbySearchOption.keyword("餐厅");
            nearbySearchOption.radius(300);// 检索半径，单位是米
            nearbySearchOption.sortType(PoiSortType.distance_from_near_to_far);
            mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
        }
    };

    private Handler acceptHanlder = new Handler() {
        @Override
        public void handleMessage(Message message) {
            result = (String) message.obj;
            if (result == null) {
                Toast.makeText(getActivity(), "连接边缘节点失败", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject resultJson = JSON.parseObject(result);
            String result = resultJson.getString("result");
            if (result.equals("success")) {
                SharedPreferences.Editor editor = share.edit();
                editor.putFloat("latitude", taskLat);
                editor.putFloat("longitude", taskLng);
                editor.putString("taskType", taskType);
                editor.putInt("taskId", taskId);
                editor.apply();
                ((MainActivity) getActivity()).getTabHost().setCurrentTab(1);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        taskInfoTextView.setText(" 任务地址 :\n 维度区间：\n 经度区间：\n 任务距离：\n 任务内容:");
        isFirstLocation = true;
        isFirstPoi = true;
        textString = new StringBuffer();
        taskLat = 0;
        taskLng = 0;
        curLat = 0;
        curLng = 0;
        SharedPreferences.Editor editor = share.edit();
        editor.putFloat("latitude", 0);
        editor.putFloat("longitude", 0);
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        mMapView.onDestroy();
        super.onDestroy();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            if (isFirstLocation) {
                isFirstLocation = false;
                // map view 销毁后不在处理新接收的位置
                Log.i("Get Task Location", "latitude = " + location.getLatitude() + "," + "longitude = " + location.getLongitude());
                curLat = (float) location.getLatitude();
                curLng = (float) location.getLongitude();
                SharedPreferences.Editor editor = share.edit();
                editor.putFloat("curLatitude", curLat);
                editor.putFloat("curLongitude", curLng);
                editor.apply();
            }
        }
    }

    /**
     * 兴趣的SDK监听函数
     */
    public class MyPoiSearchListenner implements OnGetPoiSearchResultListener {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (isFirstPoi) {
                Log.i("onGetPoiResult", poiResult.toString());
                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                    Toast.makeText(getActivity(), "暂时没有附近的任务", Toast.LENGTH_SHORT).show();
                    taskInfoTextView.setText(" 任务地址 :\n 维度区间：\n 经度区间：\n 任务距离：\n 任务内容:");
                    taskLat = 0;
                    taskLng = 0;
                    return;
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
                    isFirstPoi = false;
                    textString = new StringBuffer(" 任务地址 : " + poiResult.getAllPoi().get(0).name + "附近").append(textString);
                    SharedPreferences.Editor editor = share.edit();
                    editor.putString("taskInfo", textString.toString());
                    editor.apply();
                    taskInfoTextView.setText(textString.toString());

                    mBaiduMap.clear();
                    ooA = new MarkerOptions().icon(bdA).position(new LatLng(taskLat, taskLng)).zIndex(9).draggable(true);
                    mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(new LatLng(taskLat, taskLng)).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }

    }

    @Override
    //核心方法，避免因Fragment跳转导致地图崩溃
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser == true) {
            startRequestLocation();
        } else if (isVisibleToUser == false) {
            stopRequestLocation();
        }
    }

    private void startRequestLocation() {
        // this nullpoint check is necessary
        if (mLocClient != null) {
            mLocClient.registerLocationListener(myListener);
            mLocClient.start();
            mLocClient.requestLocation();
        }
    }

    private void stopRequestLocation() {
        if (mLocClient != null) {
            mLocClient.unRegisterLocationListener(myListener);
            mLocClient.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}


