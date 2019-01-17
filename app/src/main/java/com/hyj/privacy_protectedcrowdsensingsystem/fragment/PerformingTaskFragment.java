package com.hyj.privacy_protectedcrowdsensingsystem.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.hyj.privacy_protectedcrowdsensingsystem.R;
import com.hyj.privacy_protectedcrowdsensingsystem.util.OverlayManager;
import com.hyj.privacy_protectedcrowdsensingsystem.util.WalkingRouteOverlay;

import static android.content.Context.MODE_PRIVATE;

public class PerformingTaskFragment extends Fragment implements BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener {

    // 浏览路线节点相关
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    // 搜索相关
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    SharedPreferences share;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_performing, container, false);
        // 初始化地图
        mMapView = (MapView) view.findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        LatLng cenpt = new LatLng(32.083136, 118.647906);
        MapStatus mapStatus = new MapStatus.Builder().target(cenpt).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaidumap.setMapStatus(mapStatusUpdate);

        mBtnPre = (Button) view.findViewById(R.id.pre);
        mBtnNext = (Button) view.findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("执行任务");
        share = getActivity().getSharedPreferences("", MODE_PRIVATE);
        if (share.getFloat("latitude", 0) == 0) {
            Toast.makeText(getActivity(), "没有执行中的任务，请点击获取任务", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "正在规划任务路线。。。", Toast.LENGTH_LONG).show();

            Handler handler = new Handler();
            handler.postDelayed(new splashhandler(), 2000);
        }

        mBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.pre) {
                    return;
                }
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
                // 获取节结果信息
                LatLng nodeLocation = null;
                String nodeTitle = null;
                Object step = route.getAllStep().get(nodeIndex);
                if (step instanceof DrivingRouteLine.DrivingStep) {
                    nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                    nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
                } else if (step instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
                } else if (step instanceof TransitRouteLine.TransitStep) {
                    nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                    nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
                } else if (step instanceof BikingRouteLine.BikingStep) {
                    nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
                    nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
                }

                if (nodeLocation == null || nodeTitle == null) {
                    return;
                }
                // 移动节点至中心
                mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                // show popup
                popupText = new TextView(getActivity());
                popupText.setBackgroundResource(R.drawable.popup);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
            }
        });

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.pre) {
                    return;
                }
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
                // 获取节结果信息
                LatLng nodeLocation = null;
                String nodeTitle = null;
                Object step = route.getAllStep().get(nodeIndex);
                if (step instanceof DrivingRouteLine.DrivingStep) {
                    nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                    nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
                } else if (step instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
                } else if (step instanceof TransitRouteLine.TransitStep) {
                    nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                    nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
                } else if (step instanceof BikingRouteLine.BikingStep) {
                    nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
                    nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
                }

                if (nodeLocation == null || nodeTitle == null) {
                    return;
                }
                // 移动节点至中心
                mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                // show popup
                popupText = new TextView(getActivity());
                popupText.setBackgroundResource(R.drawable.popup);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
            }
        });
    }

    private class splashhandler implements Runnable {
        public void run() {
            // 重置浏览节点的路线数据
            route = null;
            mBtnPre.setVisibility(View.INVISIBLE);
            mBtnNext.setVisibility(View.INVISIBLE);
            mBaidumap.clear();
            PlanNode stNode = PlanNode.withLocation(new LatLng(share.getFloat("curLatitude", 32.083137f), share.getFloat("curLongitude", 118.647907f)));
            PlanNode enNode = PlanNode.withLocation(new LatLng(share.getFloat("latitude", 0), share.getFloat("longitude", 0)));
            mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
    }


    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mSearch.destroy();
        mMapView.onDestroy();
        super.onDestroy();
    }

}
