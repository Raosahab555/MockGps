package com.example.mockgps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.log4j.LogUtil;
import com.example.service.HistoryDBHelper;
import com.example.service.MockGpsService;
import com.example.service.SearchDBHelper;
import com.example.service.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mapapi.overlayutil.PoiOverlay;

import static com.example.service.MockGpsService.RunCode;
import static com.example.service.MockGpsService.StopCode;

import org.apache.log4j.Logger;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;

    //??????????????????
    //  latLngInfo  ??????&??????
    public static String latLngInfo = "104.06121778639009&30.544111926165282";
    private boolean isMockLocOpen = false;
    private MockGpsService mockGpsService;
    private MockServiceReceiver mockServiceReceiver = null;
    private boolean isServiceRun = false;
    private boolean isMockServStart = false;
    private boolean isGPSOpen = false;

    //sqlite??????
    //????????????
    private HistoryDBHelper historyDBHelper;
    private SQLiteDatabase locHistoryDB;
    //????????????
    private SearchDBHelper searchDBHelper;
    private SQLiteDatabase searchHistoryDB;

    private boolean isSQLiteStart = false;


    //http
    private RequestQueue mRequestQueue;
    private boolean isNetworkConnected = true;

    // ????????????
    LocationClient mLocClient = null;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private String mCurrentCity = "?????????";
    private String mCurrentAddr;
    /**
     * ??????????????????
     */
    public static LatLng currentPt = new LatLng(30.547743718042415, 104.07018449827267);
    public static BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);


    public MapView mMapView;
    public static BaiduMap mBaiduMap = null;

    // UI??????
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    RadioGroup.OnCheckedChangeListener radioButtonListener2;
    //Button requestLocButton;
    boolean isFirstLoc = true; // ??????????????????
    private MyLocationData locData;

    private RadioGroup grouploc;
    private RadioGroup groupmap;

    private FloatingActionButton fab;
    private FloatingActionButton fabStop;

    //??????????????????
    PoiSearch poiSearch;
    private SearchView searchView;
    private ListView searchlist;
    private ListView historySearchlist;
    private SimpleAdapter simAdapt;
    private LinearLayout mlinearLayout;
    private LinearLayout mHistorylinearLayout;
    private MenuItem searchItem;
    private boolean isSubmit;
    private SuggestionSearch mSuggestionSearch;
    //log debug
    private static Logger log = Logger.getLogger(MainActivity.class);
    ////////


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getPersimmions();

        try {
            LogUtil.configLog();
        } catch (Exception e) {
            Log.e("Log", "LogUtil config error");
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("PROGRESS", "onCreate");
        log.debug("PROGRESS: onCreate");

        //sqlite
        try {
            historyDBHelper = new HistoryDBHelper(getApplicationContext());
            locHistoryDB = historyDBHelper.getWritableDatabase();
            searchDBHelper = new SearchDBHelper(getApplicationContext());
            searchHistoryDB = searchDBHelper.getWritableDatabase();
            isSQLiteStart = true;
//            historyDBHelper.onUpgrade(locHistoryDB,locHistoryDB.getVersion(),locHistoryDB.getVersion());
        } catch (Exception e) {
            Log.e("DATABASE", "sqlite init error");
            log.error("DATABASE: sqlite init error");
            isSQLiteStart = false;
            e.printStackTrace();
        }

        //set fab listener
        setFabListener();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //http init
        mRequestQueue = Volley.newRequestQueue(this);

        //??????MockService???????????????
        try {
            mockServiceReceiver = new MockServiceReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.example.service.MockGpsService");
//            this.unregisterReceiver(mockServiceReceiver);
            this.registerReceiver(mockServiceReceiver, filter);
        } catch (Exception e) {
            Log.e("UNKNOWN", "registerReceiver error");
            e.printStackTrace();
        }

        /////
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//???????????????????????????
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        //RadioGroup
        setGroupListener();

        //??????????????????
        if (!isNetworkAvailable()) {
            DisplayToast("?????????????????????,???????????????????????????");
            isNetworkConnected = false;
        }

        //gps????????????
//        isGPSOpen=isGpsOpened();
        if (!(isGPSOpen = isGpsOpened())) {
            DisplayToast("GPS??????????????????????????????GPS????????????");
        }

        // ???????????????
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        initListener();

        // ??????????????????
        mBaiduMap.setMyLocationEnabled(true);

        //??????GPS?????????
        if (!isGPSOpen) {
            //???????????????GPS??????????????????????????????
            showGpsDialog();
        } else {
            //??????GPS????????????????????????????????????
            openLocateLayer();
        }

        //poi search ?????????
        poiSearch = PoiSearch.newInstance();
        //????????????
        searchView = (SearchView) findViewById(R.id.action_search);
        searchlist = (ListView) findViewById(R.id.search_list_view);
        mlinearLayout = (LinearLayout) findViewById(R.id.search_linear);

        historySearchlist = (ListView) findViewById(R.id.search_history_list_view);
        mHistorylinearLayout = (LinearLayout) findViewById(R.id.search_history_linear);

        // ????????????????????????
        isMockLocOpen = isAllowMockLocation();
        //??????????????????????????????
        if (!isMockLocOpen) {
            setDialog();
        }
        //?????????????????????
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                //??????Activity???????????????
                setFloatWindowDialog();
            }
        }
        //?????????POI????????????
        initPoiSearchResultListener();
        //?????????????????????????????????
        setSearchRetClickListener();
        //?????????????????????????????????
        setHistorySearchClickListener();
        //?????????????????????????????????
        setSugSearchListener();
        //????????????????????????
        randomFix();
        //????????????????????????????????????????????????????????????
        LatLng latLng = getLatestLocation(locHistoryDB, HistoryDBHelper.TABLE_NAME);
        MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(mapstatusupdate);

        //????????????????????????GPS??????????????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                // ????????????????????????
                while (!isGpsOpened()) {
                    Log.d("GPS", "gps not open");
                    log.debug("GPS: gps not open");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isGPSOpen = true;
                Log.d("GPS", "gps opened");
                log.debug("GPS: gps opened");
                //??????GPS?????????????????????????????????
                openLocateLayer();
            }
        }).start();

//        func();
        //for debug

    }

    //for debug
    public void func() {
        // for test


    }

    //????????????GPS?????????
    private void showGpsDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Tips")//????????????????????????
                .setMessage("????????????GPS?????????????")//????????????????????????????????????
                .setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 0);
                            }
                        })
                .setNegativeButton("??????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .show();
    }

    //?????????????????????????????????
    public void showLatlngDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????????????????????(BD09?????????)");
        //    ??????LayoutInflater???????????????xml???????????????????????????View??????
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.latlng_dialog, null);
        //    ?????????????????????????????????????????????????????????Content
        builder.setView(view);

        final EditText dialog_lng = (EditText) view.findViewById(R.id.dialog_longitude);
        final EditText dialog_lat = (EditText) view.findViewById(R.id.dialog_latitude);

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dialog_lng_str = "", dialog_lat_str = "";
                try {
                    dialog_lng_str = dialog_lng.getText().toString().trim();
                    dialog_lat_str = dialog_lat.getText().toString().trim();
                    double dialog_lng_double = Double.valueOf(dialog_lng_str);
                    double dialog_lat_double = Double.valueOf(dialog_lat_str);
//                    DisplayToast("??????: " + dialog_lng_str + ", ??????: " + dialog_lat_str);
                    if (dialog_lng_double > 180.0 || dialog_lng_double < -180.0 || dialog_lat_double > 90.0 || dialog_lat_double < -90.0) {
                        DisplayToast("?????????????????????!\n-180.0<??????<180.0\n-90.0<??????<90.0");
                    } else {
                        currentPt = new LatLng(dialog_lat_double, dialog_lng_double);
                        MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(currentPt);
                        //?????????????????????????????????
                        mBaiduMap.setMapStatus(mapstatusupdate);
                        updateMapState();
                        transformCoordinate(dialog_lng_str, dialog_lat_str);
                    }
                } catch (Exception e) {
                    DisplayToast("?????????????????????,???????????????????????????");
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    //??????GPS????????????
    private boolean isGpsOpened() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //?????????????????????????????????
    private void openLocateLayer() {
        // ???????????????
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setOpenGps(true); // ??????gps
        option.setCoorType("bd09ll"); // ??????????????????
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    //??????IMEI
//    public String getIMEI(Context context, int slotId) {
//        try {
//            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            assert manager != null;
//            Method method = manager.getClass().getMethod("getImei", int.class);
//            String imei = (String) method.invoke(manager, slotId);
//            Log.d("IMEI", imei);
//            return imei;
//        } catch (Exception e) {
//            return "";
//        }
//    }

    //??????????????????
    private List<Map<String, Object>> getSearchHistory() {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        try {
            Cursor cursor = searchDBHelper.getWritableDatabase().query(SearchDBHelper.TABLE_NAME, null,
                    "ID > ?", new String[]{"0"},
                    null, null, "TimeStamp DESC", null);
            while (cursor.moveToNext()) {
                int ID = cursor.getInt(0);

                Map<String, Object> searchHistoryItem = new HashMap<String, Object>();
                searchHistoryItem.put("search_key", cursor.getString(1));
                searchHistoryItem.put("search_description", cursor.getString(2));
                searchHistoryItem.put("search_timestamp", "" + cursor.getInt(3));
                searchHistoryItem.put("search_isLoc", "" + cursor.getInt(4));
                searchHistoryItem.put("search_longitude", "" + cursor.getString(7));
                searchHistoryItem.put("search_latitude", "" + cursor.getString(8));

                data.add(searchHistoryItem);

            }
            // ????????????
            cursor.close();
        } catch (Exception e) {
            Log.e("DATABASE", "query error");
            log.error("DATABASE: query error");
            e.printStackTrace();
        }
        return data;
    }

    //WIFI????????????
    private boolean isWifiConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isAvailable();
        }
        return false;
    }

    //MOBILE??????????????????
    private boolean isMobileConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobileNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobileNetworkInfo != null) {
            return mMobileNetworkInfo.isAvailable();
        }
        return false;
    }

    //??????????????????
    private boolean isNetworkAvailable() {
        return isWifiConnected() || isMobileConnected();
    }

    //??????????????????
    private void randomFix() {
        double ra1 = Math.random() * 2.0 - 1.0;
        double ra2 = Math.random() * 2.0 - 1.0;
        double randLng = 104.07018449827267 + ra1 / 2000.0;
        double randLat = 30.547743718042415 + ra2 / 2000.0;
        currentPt = new LatLng(randLat, randLng);
        transformCoordinate(Double.toString(randLng), Double.toString(randLat));
    }

    //set group button listener
    private void setGroupListener() {
        grouploc = (RadioGroup) this.findViewById(R.id.RadioGroupLocType);
        radioButtonListener2 = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.normalloc) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                    MapStatus.Builder builder1 = new MapStatus.Builder();
                    builder1.overlook(0);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                }
                if (checkedId == R.id.trackloc) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.overlook(0);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                if (checkedId == R.id.compassloc) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                }
            }
        };
        grouploc.setOnCheckedChangeListener(radioButtonListener2);


        groupmap = (RadioGroup) this.findViewById(R.id.RadioGroup);
        radioButtonListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.normal) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                if (checkedId == R.id.statellite) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        };
        groupmap.setOnCheckedChangeListener(radioButtonListener);
    }

    //set float action button listener
    private void setFabListener() {
        //?????????????????????
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabStop = (FloatingActionButton) findViewById(R.id.fabStop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /////
                if (!isGPSOpen) {
                    //??????GPS?????????
                    showGpsDialog();
                } else {
                    if (!(isMockLocOpen = isAllowMockLocation())) {
                        setDialog();
                    } else {
                        if (!isMockServStart && !isServiceRun) {
                            Log.d("DEBUG", "current pt is " + currentPt.longitude + "  " + currentPt.latitude);
                            log.debug("current pt is " + currentPt.longitude + "  " + currentPt.latitude);
                            updateMapState();
                            //start mock location service
                            Intent mockLocServiceIntent = new Intent(MainActivity.this, MockGpsService.class);
                            mockLocServiceIntent.putExtra("key", latLngInfo);
                            //isFisrtUpdate=false;
                            //save record
                            updatePositionInfo();
                            //insert end
                            if (Build.VERSION.SDK_INT >= 26) {
                                startForegroundService(mockLocServiceIntent);
                                Log.d("DEBUG", "startForegroundService: MOCK_GPS");
                                log.debug("startForegroundService: MOCK_GPS");
                            } else {
                                startService(mockLocServiceIntent);
                                Log.d("DEBUG", "startService: MOCK_GPS");
                                log.debug("startService: MOCK_GPS");
                            }
                            isMockServStart = true;
                            Snackbar.make(view, "?????????????????????", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            //fab.setVisibility(View.INVISIBLE);
                            fab.hide();
                            //fabStop.setVisibility(View.VISIBLE);
                            fabStop.show();
                            //track
                            grouploc.check(R.id.trackloc);
                        } else {
                            Snackbar.make(view, "????????????????????????", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            //fab.setVisibility(View.INVISIBLE);
                            fab.hide();
                            //fabStop.setVisibility(View.VISIBLE);
                            fabStop.show();
                            isMockServStart = true;
                        }
                    }
                }

            }
        });

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMockServStart) {
                    //end mock location
                    Intent mockLocServiceIntent = new Intent(MainActivity.this, MockGpsService.class);
                    stopService(mockLocServiceIntent);
                    Snackbar.make(v, "????????????????????????", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //service finish
                    isMockServStart = false;
                    //fab.setVisibility(View.VISIBLE);
                    fab.show();
                    //fabStop.setVisibility(View.INVISIBLE);
                    fabStop.hide();
                    //????????????
                    mLocClient.stop();
                    mLocClient.start();
                    //normal
                    grouploc.check(R.id.normalloc);
                    //clear
//                    mBaiduMap.clear();
                }
            }
        });
    }

    //??????search list ????????????
    private void setSearchRetClickListener() {
        searchlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String lng = ((TextView) view.findViewById(R.id.poi_longitude)).getText().toString();
                String lat = ((TextView) view.findViewById(R.id.poi_latitude)).getText().toString();
//                DisplayToast("lng is "+lng+"lat is "+lat);
                currentPt = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
                MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(currentPt);
                //????????????????????????????????????
                mBaiduMap.setMapStatus(mapstatusupdate);
                updateMapState();
                transformCoordinate(lng, lat);
//                searchlist.setVisibility(View.GONE);

                //???????????? ????????????
                ContentValues contentValues = new ContentValues();
                contentValues.put("SearchKey", ((TextView) view.findViewById(R.id.poi_name)).getText().toString());
                contentValues.put("Description", ((TextView) view.findViewById(R.id.poi_addr)).getText().toString());
                contentValues.put("IsLocate", 1);
                contentValues.put("BD09Longitude", lng);
                contentValues.put("BD09Latitude", lat);
                String wgsLatLngStr[] = latLngInfo.split("&");
                contentValues.put("WGS84Longitude", wgsLatLngStr[0]);
                contentValues.put("WGS84Latitude", wgsLatLngStr[1]);
                contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);

                if (!insertHistorySearchTable(searchHistoryDB, SearchDBHelper.TABLE_NAME, contentValues)) {
                    Log.e("DATABASE", "insertHistorySearchTable[SearchHistory] error");
                    log.error("DATABASE: insertHistorySearchTable[SearchHistory] error");
                } else {
                    Log.d("DATABASE", "insertHistorySearchTable[SearchHistory] success");
                    log.debug("DATABASE: insertHistorySearchTable[SearchHistory] success");
                }

                mlinearLayout.setVisibility(View.INVISIBLE);
                searchItem.collapseActionView();
//                transformCoordinate();
            }
        });
    }


    //??????history search list ????????????
    private void setHistorySearchClickListener() {
        historySearchlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String searchDescription = ((TextView) view.findViewById(R.id.search_description)).getText().toString();
                String searchKey = ((TextView) view.findViewById(R.id.search_key)).getText().toString();
                String searchIsLoc = ((TextView) view.findViewById(R.id.search_isLoc)).getText().toString();

                //?????????????????????
                if (searchIsLoc.equals("1")) {
                    String lng = ((TextView) view.findViewById(R.id.search_longitude)).getText().toString();
                    String lat = ((TextView) view.findViewById(R.id.search_latitude)).getText().toString();
//                    DisplayToast("lng is " + lng + "lat is " + lat);
                    currentPt = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
                    MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(currentPt);
                    //?????????????????????????????????
                    mBaiduMap.setMapStatus(mapstatusupdate);
                    updateMapState();
                    transformCoordinate(lng, lat);
                    //?????????????????????
                    mHistorylinearLayout.setVisibility(View.INVISIBLE);
                    searchItem.collapseActionView();
                    //?????????
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("SearchKey", searchKey);
                    contentValues.put("Description", searchDescription);
                    contentValues.put("IsLocate", 1);
                    contentValues.put("BD09Longitude", lng);
                    contentValues.put("BD09Latitude", lat);
                    String wgsLatLngStr[] = latLngInfo.split("&");
                    contentValues.put("WGS84Longitude", wgsLatLngStr[0]);
                    contentValues.put("WGS84Latitude", wgsLatLngStr[1]);
                    contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                    if (!insertHistorySearchTable(searchHistoryDB, SearchDBHelper.TABLE_NAME, contentValues)) {
                        Log.e("DATABASE", "insertHistorySearchTable[SearchHistory] error");
                        log.error("DATABASE: insertHistorySearchTable[SearchHistory] error");
                    } else {
                        Log.d("DATABASE", "insertHistorySearchTable[SearchHistory] success");
                        log.debug("DATABASE: insertHistorySearchTable[SearchHistory] success");
                    }
                }
                //?????????????????????
                else if (searchIsLoc.equals("0")) {
                    try {
//                        resetMap();
                        isSubmit = true;
                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())

                                .keyword(searchKey)
                                .city(mCurrentCity)

                        );
                        mBaiduMap.clear();
                        mHistorylinearLayout.setVisibility(View.INVISIBLE);
                        searchItem.collapseActionView();

                        //?????????
                        //???????????? ????????????
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("SearchKey", searchKey);
                        contentValues.put("Description", "??????...");
                        contentValues.put("IsLocate", 0);
                        contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                        if (!insertHistorySearchTable(searchHistoryDB, SearchDBHelper.TABLE_NAME, contentValues)) {
                            Log.e("DATABASE", "insertHistorySearchTable[SearchHistory] error");
                            log.error("DATABASE: insertHistorySearchTable[SearchHistory] error");
                        } else {
                            Log.d("DATABASE", "insertHistorySearchTable[SearchHistory] success");
                            log.debug("DATABASE: insertHistorySearchTable[SearchHistory] success");
                        }

                    } catch (Exception e) {
                        DisplayToast("????????????????????????????????????");
                        Log.d("HTTP", "????????????????????????????????????");
                        log.debug("????????????????????????????????????");
                        e.printStackTrace();
                    }
                }
                //????????????
                else {
                    Log.d("HTTP", "illegal parameter");
                    log.debug("???????????????????????????");
                }


            }
        });
        historySearchlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Warning")//????????????????????????
                        .setMessage("?????????????????????????????????????")//????????????????????????????????????
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String searchKey = ((TextView) view.findViewById(R.id.search_key)).getText().toString();
                                        try {
                                            searchHistoryDB.delete(SearchDBHelper.TABLE_NAME, "SearchKey = ?", new String[]{searchKey});
                                            //????????????
                                            //??????????????????
                                            List<Map<String, Object>> data = getSearchHistory();
                                            if (data.size() > 0) {
                                                simAdapt = new SimpleAdapter(
                                                        MainActivity.this,
                                                        data,
                                                        R.layout.history_search_item,
                                                        new String[]{"search_key", "search_description", "search_timestamp", "search_isLoc", "search_longitude", "search_latitude"},// ????????????????????????????????????
                                                        new int[]{R.id.search_key, R.id.search_description, R.id.search_timestamp, R.id.search_isLoc, R.id.search_longitude, R.id.search_latitude});
                                                historySearchlist.setAdapter(simAdapt);
                                                mHistorylinearLayout.setVisibility(View.VISIBLE);
                                            }

                                        } catch (Exception e) {
                                            Log.e("DATABASE", "delete error");
                                            log.error("DATABASE: delete error");
                                            DisplayToast("DELETE ERROR[UNKNOWN]");
                                            e.printStackTrace();
                                        }
//                                        String locID=(String) ((TextView) view.findViewById(R.id.LocationID)).getText();
//                                        boolean deleteRet=deleteRecord(sqLiteDatabase,HistoryDBHelper.TABLE_NAME,Integer.valueOf(locID));
//                                        if (deleteRet){
//                                            DisplayToast("????????????!");
//                                            initListView();
//                                        }
                                    }
                                })
                        .setNegativeButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();

                return true;
            }
        });
    }


    //poi???????????????
    private void initPoiSearchResultListener() {
        OnGetPoiSearchResultListener poiSearchListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// ????????????????????????
                    DisplayToast("????????????????????????");
                    Log.d("BDLOC", "????????????????????????");
                    log.debug("BDLOC: ????????????????????????");
                    return;
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// ????????????????????????

                    if (isSubmit) {
//                        mBaiduMap.clear();
                        MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
                        poiOverlay.setData(poiResult);// ??????POI??????
                        mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.addToMap();// ????????????overlay??????????????????
                        poiOverlay.zoomToSpan();
                        mlinearLayout.setVisibility(View.INVISIBLE);
                        //??????????????? ??????????????????
//                        searchView.clearFocus();  //??????????????????
                        searchItem.collapseActionView(); //??????????????????
                        isSubmit = false;

                    } else {
                        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                        int retCnt = poiResult.getAllPoi().size();
                        for (int i = 0; i < retCnt; i++) {
                            Map<String, Object> testitem = new HashMap<String, Object>();
                            testitem.put("key_name", poiResult.getAllPoi().get(i).name);
                            testitem.put("key_addr", poiResult.getAllPoi().get(i).address);
                            testitem.put("key_lng", "" + poiResult.getAllPoi().get(i).location.longitude);
                            testitem.put("key_lat", "" + poiResult.getAllPoi().get(i).location.latitude);
                            data.add(testitem);
                        }
                        simAdapt = new SimpleAdapter(
                                MainActivity.this,
                                data,
                                R.layout.poi_search_item,
                                new String[]{"key_name", "key_addr", "key_lng", "key_lat"},// ????????????????????????????????????
                                new int[]{R.id.poi_name, R.id.poi_addr, R.id.poi_longitude, R.id.poi_latitude});
                        searchlist.setAdapter(simAdapt);
//                    searchlist.setVisibility(View.VISIBLE);
                        mlinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                DisplayToast(poiDetailResult.name);
//                Log.d("DETAIL",poiDetailResult.address);
//                Log.d("DETAIL",poiDetailResult.name);
//                Log.d("DETAIL",poiDetailResult.tag);
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }

        };
        poiSearch.setOnGetPoiSearchResultListener(poiSearchListener);
    }

    //????????????
    private void setSugSearchListener() {
        mSuggestionSearch = SuggestionSearch.newInstance();
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
            public void onGetSuggestionResult(SuggestionResult res) {

                if (res == null || res.getAllSuggestions() == null) {
                    //?????????????????????
                    DisplayToast("????????????????????????");
                    return;
                }
                //??????????????????????????????
                else {
                    if (isSubmit) {
//                        mBaiduMap.clear();
                        //normal
                        grouploc.check(R.id.normalloc);
                        MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
                        poiOverlay.setSugData(res);// ??????POI??????
                        mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.addToMap();// ????????????overlay??????????????????
                        poiOverlay.zoomToSpan();
                        mlinearLayout.setVisibility(View.INVISIBLE);
                        //??????????????? ??????????????????
//                        searchView.clearFocus();  //??????????????????
                        searchItem.collapseActionView(); //??????????????????
                        isSubmit = false;

                    } else {
                        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                        int retCnt = res.getAllSuggestions().size();
                        for (int i = 0; i < retCnt; i++) {
                            if (res.getAllSuggestions().get(i).pt == null) {
                                continue;
                            }
                            Map<String, Object> poiItem = new HashMap<String, Object>();
                            poiItem.put("key_name", res.getAllSuggestions().get(i).key);
                            poiItem.put("key_addr", res.getAllSuggestions().get(i).city + " " + res.getAllSuggestions().get(i).district);
                            poiItem.put("key_lng", "" + res.getAllSuggestions().get(i).pt.longitude);
                            poiItem.put("key_lat", "" + res.getAllSuggestions().get(i).pt.latitude);
                            data.add(poiItem);
                        }
                        simAdapt = new SimpleAdapter(
                                MainActivity.this,
                                data,
                                R.layout.poi_search_item,
                                new String[]{"key_name", "key_addr", "key_lng", "key_lat"},// ????????????????????????????????????
                                new int[]{R.id.poi_name, R.id.poi_addr, R.id.poi_longitude, R.id.poi_latitude});
                        searchlist.setAdapter(simAdapt);
//                    searchlist.setVisibility(View.VISIBLE);
                        mlinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);

    }

    //???????????????
    private void citySearch(int page, String city, String key) {
        // ??????????????????
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
        citySearchOption.city(city);// ??????
        citySearchOption.keyword(key);// ?????????
        citySearchOption.pageCapacity(15);// ????????????10???
        citySearchOption.pageNum(page);// ????????????
        // ??????????????????
        poiSearch.searchInCity(citySearchOption);
    }


    //????????????
    private void boundSearch(int page, double longitude, double latitude, String key) {
        PoiBoundSearchOption boundSearchOption = new PoiBoundSearchOption();
        LatLng southwest = new LatLng(latitude - 0.01, longitude - 0.012);// ??????
        LatLng northeast = new LatLng(latitude + 0.01, longitude + 0.012);// ??????
        LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
                .include(northeast).build();// ??????????????????????????????
        boundSearchOption.bound(bounds);// ??????poi????????????
        boundSearchOption.keyword(key);// ???????????????
        boundSearchOption.pageNum(page);
        poiSearch.searchInBound(boundSearchOption);// ??????poi??????????????????
    }


    //????????????
    private void nearbySearch(int page, double longitude, double latitude, String key) {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(new LatLng(latitude, longitude));
        nearbySearchOption.keyword(key);
        nearbySearchOption.radius(1000);// ???????????????????????????
        nearbySearchOption.pageNum(page);
        poiSearch.searchNearby(nearbySearchOption);// ????????????????????????
    }

    //sqlite ?????? ??????HistoryLocation
    private boolean insertHistoryLocTable(SQLiteDatabase sqLiteDatabase, String tableName, ContentValues contentValues) {
        boolean insertRet = true;
        try {
            sqLiteDatabase.insert(tableName, null, contentValues);
        } catch (Exception e) {
            Log.e("DATABASE", "insert error");
            log.error("DATABASE: insert error");
            insertRet = false;
            e.printStackTrace();
        }
        return insertRet;
    }

    //sqlite ?????? ??????HistoryLocation
    private boolean insertHistorySearchTable(SQLiteDatabase sqLiteDatabase, String tableName, ContentValues contentValues) {
        boolean insertRet = true;
        try {

            String searchKey = contentValues.get("SearchKey").toString();
            sqLiteDatabase.delete(tableName, "SearchKey = ?", new String[]{searchKey});
            sqLiteDatabase.insert(tableName, null, contentValues);
        } catch (Exception e) {
            Log.e("DATABASE", "insert error");
            log.error("DATABASE: insert error");
            insertRet = false;
            e.printStackTrace();
        }
        return insertRet;
    }


    //sqlite ???????????????????????????
    private LatLng getLatestLocation(SQLiteDatabase sqLiteDatabase, String tableName) {
        try {
            Cursor cursor = sqLiteDatabase.query(tableName, null,
                    "ID > ?", new String[]{"0"},
                    null, null, "TimeStamp DESC", "1");
            if (cursor.getCount() == 0) {
                randomFix();
                return MainActivity.currentPt;
            } else {
                cursor.moveToNext();
                String BD09Longitude = cursor.getString(5);
                String BD09Latitude = cursor.getString(6);
                return new LatLng(Double.valueOf(BD09Latitude), Double.valueOf(BD09Longitude));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return MainActivity.currentPt;
    }

    //?????????????????????????????????
    private void setDialog() {
        //?????????????????????????????????
//        boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) > 0);
//        if (!enableAdb) {
//            DisplayToast("???????????????????????????");
//            return;
//        }


        new AlertDialog.Builder(this)
                .setTitle("??????????????????")//????????????????????????
                .setMessage("??????\"????????????????????????????????????????????????\"???????????????")//????????????????????????????????????
                .setPositiveButton("??????",//??????string??????????????????????????????
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    DisplayToast("??????????????????????????????,????????????????????????????????????????????????");
                                    e.printStackTrace();
                                }
                            }
                        })//setPositiveButton?????????onClick????????????????????????
                .setNegativeButton("??????",//??????string??????????????????????????????
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })//setNegativeButton?????????onClick????????????????????????????????????
                .show();
    }


    //??????????????????????????????
    private void setFloatWindowDialog() {
        new AlertDialog.Builder(this)
                .setTitle("???????????????")//????????????????????????
                .setMessage("?????????????????????????????????????????????\"???????????????\"??????")//????????????????????????????????????
                .setPositiveButton("??????",//??????string??????????????????????????????
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    DisplayToast("??????????????????????????????????????????????????????????????????????????????");
                                    e.printStackTrace();
                                }
                            }
                        })//setPositiveButton?????????onClick????????????????????????
                .setNegativeButton("??????",//??????string??????????????????????????????
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })//setNegativeButton?????????onClick????????????????????????????????????
                .show();
    }

    //??????????????????????????????
    public boolean isAllowMockLocation() {
//        return true;
        boolean canMockPosition = false;
        if (Build.VERSION.SDK_INT <= 22) {//6.0??????
            canMockPosition = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        } else {
            try {
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);//??????LocationManager??????
                String providerStr = LocationManager.GPS_PROVIDER;
                LocationProvider provider = locationManager.getProvider(providerStr);
                // ??????????????????testProvider??????????????????addTestProvider????????????????????????testProvider
                try {
                    locationManager.removeTestProvider(providerStr);
                    Log.d("PERMISSION", "try to move test provider");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("PERMISSION", "try to move test provider");
                }
                if (provider != null) {
                    try {
                        locationManager.addTestProvider(
                                provider.getName()
                                , provider.requiresNetwork()
                                , provider.requiresSatellite()
                                , provider.requiresCell()
                                , provider.hasMonetaryCost()
                                , provider.supportsAltitude()
                                , provider.supportsSpeed()
                                , provider.supportsBearing()
                                , provider.getPowerRequirement()
                                , provider.getAccuracy());
                        canMockPosition = true;
                    } catch (Exception e) {
                        Log.e("FUCK", "add origin gps test provider error");
                        canMockPosition = false;
                        e.printStackTrace();
                    }
                } else {
                    try {
                        locationManager.addTestProvider(
                                providerStr
                                , true, true, false, false, true, true, true
                                , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                        canMockPosition = true;
                    } catch (Exception e) {
                        Log.e("FUCK", "add gps test provider error");
                        canMockPosition = false;
                        e.printStackTrace();
                    }
                }

                // ??????????????????
                if (canMockPosition) {
                    locationManager.setTestProviderEnabled(providerStr, true);
                    locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                    //remove test provider
                    locationManager.setTestProviderEnabled(providerStr, false);
                    locationManager.removeTestProvider(providerStr);
                }
            } catch (SecurityException e) {
                canMockPosition = false;
                e.printStackTrace();
            }
        }
        return canMockPosition;
    }

    //???????????????????????????
    public void setTraffic(View view) {
        mBaiduMap.setTrafficEnabled(((CheckBox) view).isChecked());
    }

    //?????????????????????????????????
    public void setBaiduHeatMap(View view) {
        mBaiduMap.setBaiduHeatMapEnabled(((CheckBox) view).isChecked());
    }

    //??????????????????????????????
    private void initListener() {
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

            }
        });


        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            /**
             * ????????????
             */
            public void onMapClick(LatLng point) {
                currentPt = point;
//                DisplayToast("BD09\n[??????:" + point.latitude + "]\n[??????:" + point.longitude + "]");
                //??????????????????wgs?????????
                transformCoordinate(String.valueOf(point.longitude), String.valueOf(point.latitude));
                updateMapState();

            }

            /**
             * ??????????????????POI???
             */
            public boolean onMapPoiClick(MapPoi poi) {
                currentPt = poi.getPosition();
//                DisplayToast("BD09\n[??????:" + poi.getPosition().latitude + "]\n[??????:" + poi.getPosition().longitude + "]");
                //??????????????????wgs?????????
                transformCoordinate(String.valueOf(poi.getPosition().longitude), String.valueOf(poi.getPosition().latitude));
                updateMapState();
                return false;
            }
        });
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            /**
             * ????????????
             */
            public void onMapLongClick(LatLng point) {
                currentPt = point;
//                DisplayToast("BD09\n[??????:" + point.latitude + "]\n[??????:" + point.longitude + "]");
                //??????????????????wgs?????????
                transformCoordinate(String.valueOf(point.longitude), String.valueOf(point.latitude));
                updateMapState();
            }
        });
        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            /**
             * ????????????
             */
            public void onMapDoubleClick(LatLng point) {
                currentPt = point;
//                DisplayToast("BD09\n[??????:" + point.latitude + "]\n[??????:" + point.longitude + "]");
                //??????????????????wgs?????????
                transformCoordinate(String.valueOf(point.longitude), String.valueOf(point.latitude));
                updateMapState();
            }
        });

        /**
         * ????????????????????????
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus status) {
//                updateMapState();
            }

            @Override
            public void onMapStatusChangeStart(MapStatus status, int reason) {

            }

            public void onMapStatusChangeFinish(MapStatus status) {
//                updateMapState();
            }

            public void onMapStatusChange(MapStatus status) {
//                updateMapState();
            }
        });
    }

    //??????????????????????????????
    private void updateMapState() {
        Log.d("DEBUG", "updateMapState");
        log.debug("DEBUG: updateMapState");
        if (currentPt != null) {
            MarkerOptions ooA = new MarkerOptions().position(currentPt).icon(bdA);
            mBaiduMap.clear();
            mBaiduMap.addOverlay(ooA);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // ?????????????????????????????????????????????????????????0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //??????SDK????????????
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ???????????????????????????????????????
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentAddr = location.getAddrStr();
            mCurrentCity = location.getCity();
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // ?????????????????????????????????????????????????????????0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        Log.d("PROGRESS", "onPause");
        log.debug("PROGRESS: onPause");
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("PROGRESS", "onPause");
        log.debug("PROGRESS: onPause");
        mMapView.onResume();
        super.onResume();
        //??????????????????????????????????????????
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        Log.d("PROGRESS", "onStop");
        log.debug("PROGRESS: onStop");
        //???????????????????????????
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("PROGRESS", "onDestroy");
        if (isMockServStart) {
            Intent mockLocServiceIntent = new Intent(MainActivity.this, MockGpsService.class);
            stopService(mockLocServiceIntent);
        }
        // ?????????????????????
        mLocClient.stop();
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        //poi search destroy
        poiSearch.destroy();
        mSuggestionSearch.destroy();
        //close db
        locHistoryDB.close();
        searchHistoryDB.close();
        super.onDestroy();
    }

    public void DisplayToast(String str) {
        Toast toast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 220);
        toast.show();
    }


    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * ??????????????????????????????????????????????????????????????????????????????
             */
            // ??????????????????
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            //?????????
//            if (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
//            }
            /*
             * ????????????????????????????????????????????????(????????????)???????????????????????????????????????????????????????????????
             */
            // ????????????
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (addPermission(permissions, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.READ_EXTERNAL_STORAGE Deny \n";
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // ????????????????????????
//            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
//                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
//            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // ????????????????????????????????????,?????????????????????,??????????????????
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //??????searchView
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setIconified(false);//??????searchView??????????????????
        searchView.onActionViewExpanded();// ?????????????????????????????????????????????????????????
        searchView.setIconifiedByDefault(true);//?????????true??????????????????false????????????
        searchView.setSubmitButtonEnabled(true);//??????????????????

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                menu.setGroupVisible(0, true);
//                searchlist.setVisibility(View.GONE);
                mlinearLayout.setVisibility(View.INVISIBLE);
                mHistorylinearLayout.setVisibility(View.INVISIBLE);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                menu.setGroupVisible(0, false);
                mlinearLayout.setVisibility(View.INVISIBLE);
                //??????????????????
                List<Map<String, Object>> data = getSearchHistory();
                if (data.size() > 0) {
                    simAdapt = new SimpleAdapter(
                            MainActivity.this,
                            data,
                            R.layout.history_search_item,
                            new String[]{"search_key", "search_description", "search_timestamp", "search_isLoc", "search_longitude", "search_latitude"},// ????????????????????????????????????
                            new int[]{R.id.search_key, R.id.search_description, R.id.search_timestamp, R.id.search_isLoc, R.id.search_longitude, R.id.search_latitude});
                    historySearchlist.setAdapter(simAdapt);
                    mHistorylinearLayout.setVisibility(View.VISIBLE);
                }


                return true;  // Return true to expand action view
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //???????????????????????????
                //do search
                try {
                    isSubmit = true;
//                    poiSearch.searchInCity((new PoiCitySearchOption())
//                            .city(mCurrentCity)
//                            .keyword(query)
//                            .pageCapacity(10)
//                            .pageNum(0));
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())

                            .keyword(query)
                            .city(mCurrentCity)

                    );
                    //???????????? ????????????
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("SearchKey", query);
                    contentValues.put("Description", "??????...");
                    contentValues.put("IsLocate", 0);
                    contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                    if (!insertHistorySearchTable(searchHistoryDB, SearchDBHelper.TABLE_NAME, contentValues)) {
                        Log.e("DATABASE", "insertHistorySearchTable[SearchHistory] error");
                        log.error("DATABASE: insertHistorySearchTable[SearchHistory] error");
                    } else {
                        Log.d("DATABASE", "insertHistorySearchTable[SearchHistory] success");
                        log.debug("DATABASE: insertHistorySearchTable[SearchHistory] success");
                    }

                    mBaiduMap.clear();
                    mlinearLayout.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    DisplayToast("????????????????????????????????????");
                    Log.d("HTTP", "????????????????????????????????????");
                    log.debug("HTTP: ????????????????????????????????????");
                    e.printStackTrace();
                }
                //
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //???????????????????????????????????????

                //???????????????????????????
                mHistorylinearLayout.setVisibility(View.INVISIBLE);

                if (!newText.equals("")) {
                    //do search
                    //WATCH ME
                    try {
                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                                .keyword(newText)
                                .city(mCurrentCity)
                        );
//                        poiSearch.searchInCity((new PoiCitySearchOption())
//                                .city(mCurrentCity)
//                                .keyword(newText)
//                                .pageCapacity(30)
//                                .pageNum(0));
                    } catch (Exception e) {
                        DisplayToast("????????????????????????????????????");
                        Log.d("HTTP", "????????????????????????????????????");
                        log.debug("HTTP: ????????????????????????????????????");
                        e.printStackTrace();
                    }
                    //
                }
                return true;
            }
        });

        return true;
    }

    //????????????
    private void resetMap() {
        mBaiduMap.clear();
        MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(new LatLng(mCurrentLat, mCurrentLon));
        //?????????????????????????????????
        mBaiduMap.setMapStatus(mapstatusupdate);
        //??????????????????
        currentPt = new LatLng(mCurrentLat, mCurrentLon);
        transformCoordinate(Double.toString(currentPt.longitude), Double.toString(currentPt.latitude));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_setting) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                DisplayToast("??????????????????????????????,????????????????????????????????????????????????");
                e.printStackTrace();
            }
            //?????????????????????????????????
//            boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
//            if (!enableAdb) {
//                DisplayToast("???????????????????????????");
//            } else {
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
//                startActivity(intent);
//            }
            return true;
        } else if (id == R.id.action_resetMap) {
            resetMap();
        } else if (id == R.id.action_input) {
            showLatlngDialog();
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            // Handle
        } else if (id == R.id.nav_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_localmap) {
            Intent intent = new Intent(MainActivity.this, OfflineMapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        } else if (id == R.id.nav_bug_report) {
            //???????????????????????????
            DisplayToast("???????????????");

        } else if (id == R.id.nav_send) {
            Intent i = new Intent(Intent.ACTION_SEND);
            // i.setType("text/plain"); //????????????????????????
            i.setType("message/rfc822"); // ?????????????????????
            i.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"hilavergil@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "SUGGESTION");
            startActivity(Intent.createChooser(i,
                    "Select email application."));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //????????????
    private void transformCoordinate(final String longitude, final String latitude) {
        //??????????????????bd09
//        boolean isInCHN=false;
        final double error = 0.00000001;
        final String mcode = getResources().getString(R.string.safecode);
        final String ak = getResources().getString(R.string.ak);
        //??????bd09?????????????????????
        String mapApiUrl = "https://api.map.baidu.com/geoconv/v1/?coords=" + longitude + "," + latitude +
                "&from=5&to=3&ak=" + ak + "&mcode=" + mcode;
        Log.d("HTTP", mapApiUrl);
        log.debug("HTTP: " + mapApiUrl);
        //bd09?????????gcj02
        StringRequest stringRequest = new StringRequest(mapApiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject getRetJson = new JSONObject(response);
                            //??????api??????????????????
                            if (Integer.valueOf(getRetJson.getString("status")) == 0) {
                                Log.d("HTTP", "call api[bd09_to_gcj02] success");
                                log.debug("HTTP: call api[bd09_to_gcj02] success");
                                JSONArray coordinateArr = getRetJson.getJSONArray("result");
                                JSONObject coordinate = coordinateArr.getJSONObject(0);
                                String gcj02Longitude = coordinate.getString("x");
                                String gcj02Latitude = coordinate.getString("y");

                                Log.d("DEBUG", "bd09Longitude is " + longitude);
                                Log.d("DEBUG", "bd09Latitude is " + latitude);

                                Log.d("DEBUG", "gcj02Longitude is " + gcj02Longitude);
                                Log.d("DEBUG", "gcj02Latitude is " + gcj02Latitude);

                                log.debug("bd09Longitude is " + longitude + ", " + "bd09Latitude is " + latitude);
                                log.debug("gcj02Longitude is " + gcj02Longitude + ", " + "gcj02Latitude is " + gcj02Latitude);

                                BigDecimal bigDecimalGcj02Longitude = new BigDecimal(Double.valueOf(gcj02Longitude));
                                BigDecimal bigDecimalGcj02Latitude = new BigDecimal(Double.valueOf(gcj02Latitude));

                                BigDecimal bigDecimalBd09Longitude = new BigDecimal(Double.valueOf(longitude));
                                BigDecimal bigDecimalBd09Latitude = new BigDecimal(Double.valueOf(latitude));

                                double gcj02LongitudeDouble = bigDecimalGcj02Longitude.setScale(9, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double gcj02LatitudeDouble = bigDecimalGcj02Latitude.setScale(9, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double bd09LongitudeDouble = bigDecimalBd09Longitude.setScale(9, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double bd09LatitudeDouble = bigDecimalBd09Latitude.setScale(9, BigDecimal.ROUND_HALF_UP).doubleValue();


                                Log.d("DEBUG", "gcj02LongitudeDouble is " + gcj02LongitudeDouble);
                                Log.d("DEBUG", "gcj02LatitudeDouble is " + gcj02LatitudeDouble);
                                Log.d("DEBUG", "bd09LongitudeDouble is " + bd09LongitudeDouble);
                                Log.d("DEBUG", "bd09LatitudeDouble is " + bd09LatitudeDouble);

                                log.debug("gcj02LongitudeDouble is " + gcj02LongitudeDouble + ", " + "gcj02LatitudeDouble is " + gcj02LatitudeDouble);
                                log.debug("bd09LongitudeDouble is " + bd09LongitudeDouble + ", " + "bd09LatitudeDouble is " + bd09LatitudeDouble);


                                //??????bd09???gcj02 ??????????????????  ????????????????????????
                                if ((Math.abs(gcj02LongitudeDouble - bd09LongitudeDouble)) <= error && (Math.abs(gcj02LatitudeDouble - bd09LatitudeDouble)) <= error) {
                                    //?????????????????????
                                    latLngInfo = longitude + "&" + latitude;
                                    Log.d("DEBUG", "OUT OF CHN, NO NEED TO TRANSFORM COORDINATE");
                                    log.debug("OUT OF CHN, NO NEED TO TRANSFORM COORDINATE");
//                                    DisplayToast("OUT OF CHN, NO NEED TO TRANSFORM COORDINATE");
                                } else {
                                    //?????????????????????
//                                    double latLng[] = Utils.bd2wgs(Double.valueOf(longitude), Double.valueOf(latitude));
                                    double latLng[] = Utils.gcj02towgs84(Double.valueOf(gcj02Longitude), Double.valueOf(gcj02Latitude));
                                    latLngInfo = latLng[0] + "&" + latLng[1];
                                    Log.d("DEBUG", "IN CHN, NEED TO TRANSFORM COORDINATE");
                                    log.debug("IN CHN, NEED TO TRANSFORM COORDINATE");
//                                    DisplayToast("IN CHN, NEED TO TRANSFORM COORDINATE");
                                }
                            }
                            //api?????????????????? ???????????????
                            else {
                                //?????????????????????
                                double latLng[] = Utils.bd2wgs(Double.valueOf(longitude), Double.valueOf(latitude));
                                latLngInfo = latLng[0] + "&" + latLng[1];
                                Log.d("DEBUG", "IN CHN, NEED TO TRANSFORM COORDINATE");
                                log.debug("IN CHN, NEED TO TRANSFORM COORDINATE");
//                                DisplayToast("BD Map Api Return not Zero, ASSUME IN CHN, NEED TO TRANSFORM COORDINATE");
                            }

                        } catch (JSONException e) {
                            Log.e("JSON", "resolve json error");
                            log.error("JSON: resolve json error");
                            e.printStackTrace();
                            //?????????????????????
                            double latLng[] = Utils.bd2wgs(Double.valueOf(longitude), Double.valueOf(latitude));
                            latLngInfo = latLng[0] + "&" + latLng[1];
                            Log.d("DEBUG", "IN CHN, NEED TO TRANSFORM COORDINATE");
                            log.debug("IN CHN, NEED TO TRANSFORM COORDINATE");
//                            DisplayToast("Resolve JSON Error, ASSUME IN CHN, NEED TO TRANSFORM COORDINATE");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //http ????????????
                Log.e("HTTP", "HTTP GET FAILED");
                log.error("HTTP: HTTP GET FAILED");
                //?????????????????????
                double latLng[] = Utils.bd2wgs(Double.valueOf(longitude), Double.valueOf(latitude));
                latLngInfo = latLng[0] + "&" + latLng[1];
                Log.d("DEBUG", "IN CHN, NEED TO TRANSFORM COORDINATE");
                log.debug("IN CHN, NEED TO TRANSFORM COORDINATE");
//                DisplayToast("HTTP Get Failed, ASSUME IN CHN, NEED TO TRANSFORM COORDINATE");
            }
        });
        // ???????????????tag
        stringRequest.setTag("MapAPI");
        // ??????tag???????????????
        mRequestQueue.add(stringRequest);
        //?????????????????????
//        double latLng[]= Utils.bd2wgs(Double.valueOf(longitude),Double.valueOf(latitude));
//        latLngInfo=longitude+"&"+latitude;
//        latLngInfo=latLng[0]+"&"+latLng[1];
    }

    //????????????????????????????????? ?????????
    private void updatePositionInfo() {
        //??????????????????bd09
        final String mcode = getResources().getString(R.string.safecode);
        final String ak = getResources().getString(R.string.ak);
        final String mapType = "bd09ll";
        //bd09?????????????????????
//        String mapApiUrl = "https://api.map.baidu.com/geocoder/v2/?location=" + currentPt.latitude + "," + currentPt.longitude + "&output=json&pois=1&ak=" + ak + "&mcode=" + mcode;
        String mapApiUrl = "https://api.map.baidu.com/reverse_geocoding/v3/?ak=" + ak + "&output=json&coordtype=" + mapType + "&location=" + currentPt.latitude + "," + currentPt.longitude + "&mcode=" + mcode;
        Log.d("MAPAPI", mapApiUrl);
        StringRequest stringRequest = new StringRequest(mapApiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject getRetJson = new JSONObject(response);
                            //??????????????????
                            if (Integer.valueOf(getRetJson.getString("status")) == 0) {
                                Log.d("HTTP", "call api[get_poisition_info] success");
                                log.debug("HTTP: call api[get_poisition_info] success");
                                JSONObject posInfoJson = getRetJson.getJSONObject("result");
                                String formatted_address = posInfoJson.getString("formatted_address");
//                                DisplayToast(tmp);
                                Log.d("ADDR", formatted_address);
                                log.debug(formatted_address);

                                //????????????
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("Location", formatted_address);
                                String latLngStr[] = latLngInfo.split("&");
                                contentValues.put("WGS84Longitude", latLngStr[0]);
                                contentValues.put("WGS84Latitude", latLngStr[1]);
                                contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                                contentValues.put("BD09Longitude", "" + currentPt.longitude);
                                contentValues.put("BD09Latitude", "" + currentPt.latitude);

                                if (!insertHistoryLocTable(locHistoryDB, HistoryDBHelper.TABLE_NAME, contentValues)) {
                                    Log.e("DATABASE", "insertHistoryLocTable[HistoryLocation] error");
                                    log.error("DATABASE: insertHistoryLocTable[HistoryLocation] error");
                                } else {
                                    Log.d("DATABASE", "insertHistoryLocTable[HistoryLocation] success");
                                    log.debug("DATABASE: insertHistoryLocTable[HistoryLocation] success");
                                }
                            }
                            //??????????????????
                            else {
                                //????????????
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("Location", "NULL");
                                String latLngStr[] = latLngInfo.split("&");
                                contentValues.put("WGS84Longitude", latLngStr[0]);
                                contentValues.put("WGS84Latitude", latLngStr[1]);
                                contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                                contentValues.put("BD09Longitude", "" + currentPt.longitude);
                                contentValues.put("BD09Latitude", "" + currentPt.latitude);

                                if (!insertHistoryLocTable(locHistoryDB, HistoryDBHelper.TABLE_NAME, contentValues)) {
                                    Log.e("DATABASE", "insertHistoryLocTable[HistoryLocation] error");
                                    log.error("DATABASE: insertHistoryLocTable[HistoryLocation] error");
                                } else {
                                    Log.d("DATABASE", "insertHistoryLocTable[HistoryLocation] success");
                                    log.debug("DATABASE: insertHistoryLocTable[HistoryLocation] success");
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("JSON", "resolve json error");
                            log.error("JSON: resolve json error");
                            //????????????
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("Location", "NULL");
                            String latLngStr[] = latLngInfo.split("&");
                            contentValues.put("WGS84Longitude", latLngStr[0]);
                            contentValues.put("WGS84Latitude", latLngStr[1]);
                            contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                            contentValues.put("BD09Longitude", "" + currentPt.longitude);
                            contentValues.put("BD09Latitude", "" + currentPt.latitude);

                            if (!insertHistoryLocTable(locHistoryDB, HistoryDBHelper.TABLE_NAME, contentValues)) {
                                Log.e("DATABASE", "insertHistoryLocTable[HistoryLocation] error");
                                log.error("DATABASE: insertHistoryLocTable[HistoryLocation] error");
                            } else {
                                Log.d("DATABASE", "insertHistoryLocTable[HistoryLocation] success");
                                log.debug("DATABASE: insertHistoryLocTable[HistoryLocation] success");
                            }
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //http ????????????
                Log.e("HTTP", "HTTP GET FAILED");
                log.error("HTTP: HTTP GET FAILED");
                //????????????
                ContentValues contentValues = new ContentValues();
                contentValues.put("Location", "NULL");
                String latLngStr[] = latLngInfo.split("&");
                contentValues.put("WGS84Longitude", latLngStr[0]);
                contentValues.put("WGS84Latitude", latLngStr[1]);
                contentValues.put("TimeStamp", System.currentTimeMillis() / 1000);
                contentValues.put("BD09Longitude", "" + currentPt.longitude);
                contentValues.put("BD09Latitude", "" + currentPt.latitude);

                if (!insertHistoryLocTable(locHistoryDB, HistoryDBHelper.TABLE_NAME, contentValues)) {
                    Log.e("DATABASE", "insertHistoryLocTable[HistoryLocation] error");
                    log.error("DATABASE: insertHistoryLocTable[HistoryLocation] error");
                } else {
                    Log.d("DATABASE", "insertHistoryLocTable[HistoryLocation] success");
                    log.debug("DATABASE: insertHistoryLocTable[HistoryLocation] success");
                }
            }
        });
        // ???????????????tag
        stringRequest.setTag("MapAPI");
        // ??????tag???????????????
        mRequestQueue.add(stringRequest);
    }

    public class MockServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int statusCode;
            Bundle bundle = intent.getExtras();
            assert bundle != null;
            statusCode = bundle.getInt("statusCode");
            Log.d("DEBUG", "BroadcastReceiver statusCode: " + statusCode + "");
            log.debug("DEBUG: BroadcastReceiver statusCode: " + statusCode + "");
            if (statusCode == RunCode) {
                isServiceRun = true;
            } else if (statusCode == StopCode) {
                isServiceRun = false;
            }
        }
    }

    public static boolean setHistoryLocation(String bd09Longitude, String bd09Latitude, String wgs84Longitude, String wgs84Latitude) {
        boolean ret = true;
        try {
            if (!bd09Longitude.isEmpty() && !bd09Latitude.isEmpty()) {
                currentPt = new LatLng(Double.valueOf(bd09Latitude), Double.valueOf(bd09Longitude));
                MarkerOptions ooA = new MarkerOptions().position(currentPt).icon(bdA);
                mBaiduMap.clear();
                mBaiduMap.addOverlay(ooA);
                MapStatusUpdate mapstatusupdate = MapStatusUpdateFactory.newLatLng(currentPt);
                mBaiduMap.setMapStatus(mapstatusupdate);
                latLngInfo = wgs84Longitude + "&" + wgs84Latitude;

            }
        } catch (Exception e) {
            ret = false;
            Log.e("UNKNOWN", "setHistoryLocation error");
            log.error("UNKNOWN: setHistoryLocation error");
            e.printStackTrace();
        }
        return ret;
    }

    class MyPoiOverlay extends PoiOverlay {
        private MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }

        @Override
        public boolean onPoiClick(int arg0) {
            super.onPoiClick(arg0);
            PoiResult poiResult = getPoiResult();
            if (poiResult != null && poiResult.getAllPoi() != null) {
                PoiInfo poiInfo;
                poiInfo = poiResult.getAllPoi().get(arg0);
                currentPt = poiInfo.location;
                transformCoordinate(Double.toString(currentPt.longitude), Double.toString(currentPt.latitude));
                // ??????poi????????????
                poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                        .poiUid(poiInfo.uid));
            }
            SuggestionResult suggestionResult = getSugResult();
            if (suggestionResult != null && suggestionResult.getAllSuggestions() != null) {
                SuggestionResult.SuggestionInfo suggestionInfo;
                suggestionInfo = suggestionResult.getAllSuggestions().get(arg0);
                currentPt = suggestionInfo.pt;
                transformCoordinate(Double.toString(currentPt.longitude), Double.toString(currentPt.latitude));
                // ??????sug????????????
                poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                        .poiUid(suggestionInfo.uid));
            }
            return true;
        }
    }

}
