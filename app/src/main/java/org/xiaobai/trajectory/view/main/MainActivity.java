package org.xiaobai.trajectory.view.main;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.litepal.LitePal;
import org.xiaobai.trajectory.R;
import org.xiaobai.trajectory.adapter.LatLngLiseAdapter;
import org.xiaobai.trajectory.db.LocationInfo;
import org.xiaobai.trajectory.view.base.BaseActivity;
import org.xiaobai.utils.DateUtils;
import org.xiaobai.utils.MarqueeTextView;
import org.xiaobai.utils.SPUtils;
import org.xiaobai.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 主界面
 * 2019-06-05
 */
public class MainActivity extends BaseActivity implements LocationSource, AMapLocationListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener {
    @BindView(R.id.tv_top)
    TextView tvTop;
    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tv_info)
    MarqueeTextView tvInfo;
    @BindView(R.id.bt_start)
    Button btStart;
    @BindView(R.id.bt_clear)
    Button btClear;
    @BindView(R.id.iv_query)
    ImageView ivQuery;
    @BindView(R.id.cv_head_image)
    CircleImageView cvHeadImage;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_latlng_number)
    TextView tvNumber;
    private String TAG = "MainActivity";

    //地图相关
    private AMap aMap;//地图控制器
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation AmapLocation;
    private UiSettings mUiSettings;

    //定位信息
    private Double MyLng = 0.0;//当前的
    private Double MyLat = 0.0;
    private Double MyHeight = 0.0;
    private String MyCity = "";
    private String MyTown = "";
    private String MyVillage = "";

    private Double MySLng = 0.0, MySLat = 0.0;
    //轨迹
    private int mIsStart = 0;
    //数据库的表
    private LocationInfo mLocationInfo;
    //保存的次数
    private int mSaveNumber = 0;
    //列表
    private LatLngLiseAdapter mLatLngLiseAdapter;
    private List<LocationInfo> mSumList = new ArrayList<>();//总数据
    //抽屉是否被打开
    private boolean mIsDrawerOpen = false;
    //地图上面的Marker 点集合
    private Map<Integer, Marker> mMarkerMaps = new HashMap<>();

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mIsStart = SPUtils.getInstance().getInt(SPUtils.IS_START_LOCATION, 0);//默认否
        if (mIsStart == 1) {
            //已经开始了 红色
            btStart.setBackgroundResource(R.drawable.shape_end_bt_bg);
            btStart.setText("结 束 轨 迹 记 录");
        } else {
            //未开始 红色
            btStart.setBackgroundResource(R.drawable.shape_start_bt_bg);
            btStart.setText("开 始 轨 迹 记 录");
        }
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        checkPermissions(this);
        initMap(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLatLngLiseAdapter = new LatLngLiseAdapter(mSumList);
        recyclerView.setAdapter(mLatLngLiseAdapter);
        mLatLngLiseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //添加地图的Marker 点
                if (mMarkerMaps.get(mSumList.get(position).getId()) != null) {
                    ToastUtil.showToast("该点已经添加到地图上了!");
                } else {
                    try {
                        double lat = Double.parseDouble(mSumList.get(position).getLat());
                        double lng = Double.parseDouble(mSumList.get(position).getLng());
                        LatLng point = new LatLng(lat, lng);
                        Marker marker =
                                aMap.addMarker(new MarkerOptions().anchor(0.5f, 1)
                                        .position(point));
                        mMarkerMaps.put(mSumList.get(position).getId(), marker);
                        ToastUtil.showToast("添加成功!");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        ToastUtil.showToast("添加失败!");
                    }
                }
                if (mMarkerMaps.size() != 0) {
                    btClear.setVisibility(View.VISIBLE);
                    btClear.setText("清除所有标记点(" + mMarkerMaps.size() + ")个");
                } else {
                    btClear.setVisibility(View.GONE);
                }
            }
        });

        //对抽屉的监听
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            /**
             * 当抽屉滑动状态改变的时候被调用
             * 状态值是STATE_IDLE（闲置--0）, STATE_DRAGGING（拖拽的--1）, STATE_SETTLING（固定--2）中之一。
             * 抽屉打开的时候，点击抽屉，drawer的状态就会变成STATE_DRAGGING，然后变成STATE_IDLE
             */
            @Override
            public void onDrawerStateChanged(int i) {

            }

            /**
             * 当抽屉被滑动的时候调用此方法
             * arg1 表示 滑动的幅度（0-1）
             */
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                //抽屉完全被打开
                LOG(TAG, "抽屉打开");
                mIsDrawerOpen = true;
                //显示数据库的定位数据 最多100条
                mSumList.clear();
                QuerLatLngs();

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                LOG(TAG, "抽屉关闭");
                //抽屉完全被关闭
                mIsDrawerOpen = false;
            }

        });
    }

    /**
     * 搜索数据库的数据
     */
    private void QuerLatLngs() {
        //降序 查询100条数据 从第的一个开始
        mSumList.addAll(LitePal.order("id desc").limit(100).offset(0).find(LocationInfo.class));
        tvNumber.setText("最新:" + mSumList.size() + "条");
        mLatLngLiseAdapter.notifyDataSetChanged();
    }


    /**
     * 初始化高德地图
     *
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            if (aMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对
        mUiSettings.setZoomControlsEnabled(true);//隐藏缩放组件
        mUiSettings.setCompassEnabled(true);//显示指南针
        mUiSettings.setLogoLeftMargin(-350);//设置高德地图的位置
        mUiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示

        MyLocationStyle myLocationStyle = new MyLocationStyle(); // 自定义系统定位蓝点
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.strokeWidth(1);    //自定义精度范围的圆形边框宽度
        //旋转位置不移动到中心点
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);


        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationStyle(myLocationStyle); // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);    //设置为卫星地图
        aMap.setTrafficEnabled(true);//显示实时路况图层，aMap是地图控制器对象。

        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
    }

    /**
     * 定位成功后回调函数
     */
    private boolean islocation = false;//是否成功定位
    private boolean isFirstLoc = true;

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                this.AmapLocation = amapLocation;
                MyLng = amapLocation.getLongitude();//经度
                MyLat = amapLocation.getLatitude();//纬度
                MyHeight = amapLocation.getAltitude();//高度
                MyCity = amapLocation.getCity();//城市信息
                MyTown = amapLocation.getDistrict();//城区信息
                MyVillage = amapLocation.getStreet();//街道信息
                tvInfo.setText(amapLocation.getAddress());
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                //这次定位与上次定位 超过10米就保存
                float distance = AMapUtils.calculateLineDistance(new LatLng(MySLat, MySLng), new LatLng(MyLat, MyLng));
                LOG(TAG, "当前定位经纬度:(" + MyLng + "," + MyLat + " 上次经纬度:(" + MySLng + "," + MySLat + ")  距离:" + distance);
                if (distance >= 10) {
                    if (mIsStart == 1) {
                        LOG(TAG, "距离超过10米,并且开启了轨迹记录,保存位置点~");
                        mLocationInfo = new LocationInfo();
                        mLocationInfo.setLat(MyLat + "");
                        mLocationInfo.setLng(MyLng + "");
                        mLocationInfo.setAddress(amapLocation.getAddress());
                        mLocationInfo.setTime(DateUtils.getDate());
                        mLocationInfo.save();//保存
                        mSaveNumber++;
                        MySLng = MyLng;
                        MySLat = MyLat;
                        if (mIsDrawerOpen) {
                            //添加到列表
                            mSumList.add(mLocationInfo);
                            mLatLngLiseAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    LOG(TAG, "距离未超过10米,不保存位置点");
                }
                if (isFirstLoc) {
                    //定位到自己的位置
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MyLat, MyLng), 18));
                    isFirstLoc = false;
                }
                islocation = true;
            } else {
                islocation = false;
            }
        }

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mlocationClient.setLocationOption(mLocationOption);
            mLocationOption.setOnceLocation(false);
            mLocationOption.setInterval(5 * 1000);
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();

        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (null != mlocationClient) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 检测权限
     *
     * @param context
     */
    private void checkPermissions(BaseActivity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new RxPermissions(context).request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {

                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    /**
     * 返回键实现home键盘
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.iv_query, R.id.bt_start, R.id.bt_clear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_query:
                startActivity(new Intent(this, QueryActivity.class));
                break;
            case R.id.bt_start:
                if (mIsStart == 1) {
                    mIsStart = 0;
                    btStart.setBackgroundResource(R.drawable.shape_start_bt_bg);
                    btStart.setText("开 启 轨 迹 记 录");
                    ToastUtil.showToast("已保存" + mSaveNumber + "条轨迹记录!");
                } else {
                    mIsStart = 1;
                    mSaveNumber = 0;
                    btStart.setBackgroundResource(R.drawable.shape_end_bt_bg);
                    btStart.setText("结 束 轨 迹 记 录");
                    ToastUtil.showToast("已开启轨迹记录!");
                }
                //保存状态
                SPUtils.getInstance().putInt(SPUtils.IS_START_LOCATION, mIsStart);
                break;
            case R.id.bt_clear:
                mMarkerMaps.clear();
                aMap.clear(true);
                ToastUtil.showToast("清除成功!");
                btClear.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
