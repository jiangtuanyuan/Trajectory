package org.xiaobai.trajectory.view.main;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;

import org.xiaobai.trajectory.R;
import org.xiaobai.trajectory.view.base.BaseActivity;
import org.xiaobai.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 显示轨迹的地图 提供截图保存功能
 * jiang
 */
public class MapTrackActivity extends BaseActivity implements LocationSource, AMapLocationListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.tv_stop)
    TextView tvStop;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.ll_layout)
    LinearLayout LLLayout;

    private String name = "";
    private String str8 = "";


    //地图相关
    private AMap aMap;//地图控制器
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation AmapLocation;
    private UiSettings mUiSettings;

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        try {
            mlocationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (null != mlocationClient) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
    }


    @Override
    protected void initVariables() {
        if (getIntent() != null) {
            str8 = getIntent().getStringExtra("latlngs");
            name = getIntent().getStringExtra("name");
        }
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_map_track;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initToolbarNav();
        setTitle("轨迹信息");
        setSupportActionBar(mToolbar);
        initMap(savedInstanceState); //初始化高德地图
        intTrack();
    }

    private void intTrack() {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.addAll(getLatLngs(str8));
        addPolyline(latLngs);
        //添加起点位置和终点位置
        aMap.addMarker(getTrackMarkeSnippet(latLngs.get(0), name + "的轨迹", BitmapDescriptorFactory.fromResource(R.drawable.ic_map_track_start)));
        aMap.addMarker(getTrackMarkeSnippet(latLngs.get(latLngs.size() - 1), name + "的轨迹", BitmapDescriptorFactory.fromResource(R.drawable.ic_map_track_end)));
        animateAmpCamera(latLngs);
    }

    /**
     * 平移的播放轨迹
     *
     * @param points
     */
    private SmoothMoveMarker smoothMarker;

    private void animateAmpCamera(List<LatLng> points) {
        try {
            LatLngBounds bounds = new LatLngBounds(points.get(0), points.get(points.size() - 2));
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            smoothMarker = new SmoothMoveMarker(aMap);
            //设置滑动的图标
            smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_person));

            LatLng drivePoint = points.get(0);
            Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
            points.set(pair.first, drivePoint);
            List<LatLng> subList = points.subList(pair.first, points.size());
            // 设置滑动的轨迹左边点
            smoothMarker.setPoints(subList);
            //设置滑动的总时间
            smoothMarker.setTotalDuration(40);

            smoothMarker.setMoveListener(new SmoothMoveMarker.MoveListener() {
                @Override
                public void move(final double v) {
                    Log.e("move", v + "");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvDistance.setText("剩余距离: " + (int) v + " m");
                        }
                    });
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast("该条轨迹数据异常!暂时无法查看!");
            LLLayout.setVisibility(View.GONE);
        }

    }

    @OnClick({R.id.tv_stop, R.id.tv_start})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_start://播放
                smoothMarker.startSmoothMove();
                break;
            case R.id.tv_stop://暂停
                smoothMarker.stopMove();
                break;
            default:
                break;
        }
    }


    /**
     * 根据经纬度的集合 在地图上面绘制成一条线 颜色随机
     */
    private Random random;

    private void addPolyline(List<LatLng> latLngs) {
        if (random == null) {
            random = new Random();
        }
        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.argb(random.nextInt(255), random.nextInt(255), random.nextInt(255), random.nextInt(255))));
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
        mUiSettings.setZoomControlsEnabled(false);//隐藏缩放组件
        mUiSettings.setCompassEnabled(false);//显示指南针
        mUiSettings.setLogoLeftMargin(-350);//设置高德地图的位置

        MyLocationStyle myLocationStyle = new MyLocationStyle(); // 自定义系统定位蓝点
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.strokeWidth(1);    //自定义精度范围的圆形边框宽度

        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationStyle(myLocationStyle); // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);    //设置为卫星地图
        aMap.setTrafficEnabled(true);//显示实时路况图层，aMap是地图控制器对象。
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_image:
                getMapView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                if (isFirstLoc) {
                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
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
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
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

    /**
     * 对地图进行截屏
     */
    public void getMapView() {
        showProgressDialog("正在保存成图片......");
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int status) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                if (null == bitmap) {
                    closeProgressDialog();
                    return;
                }
                try {
                    String imageUrl = Environment.getExternalStorageDirectory()
                            + File.separator + Environment.DIRECTORY_DCIM
                            + File.separator + "Camera" + File.separator + "Mill" + sdf.format(new Date()) + ".png";
                    FileOutputStream fos = new FileOutputStream(imageUrl);
                    boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    try {
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    StringBuffer buffer = new StringBuffer();
                    if (b)
                        buffer.append("截屏成功,");
                    else {
                        buffer.append("截屏失败,");
                    }
                    if (status != 0)
                        buffer.append("地图渲染完成,请到相册查看!");
                    else {
                        buffer.append("地图未渲染完成，截屏有网格!");
                    }

                    //发送刷新图片系统的广播
                    Uri uri = Uri.fromFile(new File(imageUrl));
                    MapTrackActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

                    ToastUtil.showToast(buffer.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                closeProgressDialog();
            }
        });
    }

    /**
     * @param latLng 图标的经纬度
     * @param icon   图标
     * @return marke
     */
    public static MarkerOptions getTrackMarkeSnippet(LatLng latLng, String snippet, BitmapDescriptor icon) {
        MarkerOptions Mark;
        //设置起点
        Mark = new MarkerOptions();
        Mark.anchor(0.5f, 0.5f);
        Mark.visible(true);
        Mark.draggable(false);
        Mark.title("");
        Mark.position(latLng);
        Mark.icon(icon);
        Mark.snippet(snippet);
        return Mark;
    }

    /**
     * 根据一组经纬度字符串 返回一组经纬度集合
     */
    public static List<LatLng> getLatLngs(String s) {
        List<LatLng> LatLngs = new ArrayList<>();
        String[] Lngs = s.split(";");
        for (int i = 0; i < Lngs.length; i++) {
            String[] latlng = Lngs[i].split(",");//再通过，号分割出第N组的经纬度
            if (latlng.length == 2) {
                //必须要等于2
                double longitude;
                double latitude;
                try {
                    longitude = Double.parseDouble(latlng[0]);//经度
                    latitude = Double.parseDouble(latlng[1]);//维度
                    LatLngs.add(new LatLng(latitude, longitude));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return LatLngs;
    }
}
