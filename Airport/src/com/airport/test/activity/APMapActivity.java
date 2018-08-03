package com.airport.test.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airport.test.R;
import com.airport.test.adapter.FloorAdapter;
import com.airport.test.ar.ArManager;
import com.airport.test.ar.ArManager.ArLocation;
import com.airport.test.ar.ArShowActivity;
import com.airport.test.core.APActivity;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.AirData;
import com.airport.test.model.MsgData;
import com.airport.test.util.VibratorUtil;
import com.dingtao.libs.util.DTIOUtil;
import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.Floor;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RMLocationMode;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;

public class APMapActivity extends APActivity implements OnClickListener,
		RMLocationListener, OnItemClickListener, OnNavigationListener {

	private DrawerLayout mDrawer;
	private ImageView mMenu, mSearch;
	private MapView mMapView;
	private ListView mList;
	private FloorAdapter mAdater;
	private TextView mTitle, mLocTitle;
	private CompassLayer mCompassLayer;// 指南针图层
	private TapPOILayer mTapPoiLayer;
	private RouteLayer mRouteLayer;
	private POILayer mPoiLayer, mNaPoiLayer;
	private RelativeLayout mTitleLayout, mNaTitleLayout;
	
	private POI mAirNotify;// 需要弹出提示的notify
	private MsgData mBookmsg;
	
	private Dialog mPoiDialog;
	private RelativeLayout mCome, mGo, mPoiInfo;
	private TextView mPoiName;
	private TextView mPoiFloor;
	private ImageView mLocBtn, mArBtn;

	/**
	 * 导航路书布局
	 */
	private RelativeLayout mNaLayout;
	private ImageView mLeft, mRight;
	private TextView mContent;
	private ArrayList<String> mTextList;
	private ArrayList<Integer> mTextIndexList;

	private RelativeLayout mNavigateTopLayout, mNavigateBottomLayout;// 提示布局
	private ImageView mArrow;// 导航箭头
	private TextView mNaText;// 导航提示
	private TextView mNaDistance;// 导航距离
	private TextView mRecoverNavigate;

	private POI mPoiStart, mPoiEnd, mPoi;

	private Dialog mDialog;
	private TextView mSignText;
	private TextView mDialogOk, mDialogCancel;
	private Bitmap mBitmap;

	private ArrayList<POI> mPoiList;

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				Log.e("rtmap", "SDK进度码" + progress);
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
					Toast.makeText(getApplicationContext(), "地图下载成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
					Toast.makeText(getApplicationContext(), "地图下载失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Success) {
					Log.e("rtmap", "地图更新成功");
					Toast.makeText(getApplicationContext(), "地图更新成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Fail) {
					Log.e("rtmap", "地图更新失败");
					Toast.makeText(getApplicationContext(), "地图更新失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			case RouteLayer.NAVIGATE:
				int sign = msg.arg1;
				if (sign == RouteLayer.PLAN_ROUTE_START) {// 开始规划路线

				} else if (sign == RouteLayer.PLAN_ROUTE_ERROR) {// 规划路线请求服务器错误

				} else if (sign == RouteLayer.NAVIGATE_START) {// 开始导航

				} else if (sign == RouteLayer.NAVIGATE_CURRENT_POINT) {// 导航关键节点
					int distance = 0;
					for (int i = 0; i < mRouteLayer.getNavigateRoutePoints()
							.size(); i++) {
						NavigatePoint p = mRouteLayer.getNavigateRoutePoints()
								.get(i);
						if (p.isImportant())
							distance += p.getDistance();
					}
					Log.i("rtmap",
							mGson.toJson(mRouteLayer.getNavigateRoutePoints()));
					mNaDistance.setText((distance / 1000 + 1) + "米");
					NavigatePoint point = (NavigatePoint) msg.obj;
					// 1：直行，2：右前，3：右转，4：右后，5：左后，6：左转，
					// 7：左前，8：直梯上行，9：直梯下行，10：扶梯上行，11扶梯下行。
					if (point.getAction() == 1) {
						mArrow.setImageResource(R.drawable.zhixing);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "直行");
					} else if (point.getAction() == 2) {
						mArrow.setImageResource(R.drawable.youqian);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往右前方转");
					} else if (point.getAction() == 3) {
						mArrow.setImageResource(R.drawable.youzhuan);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往右转");
					} else if (point.getAction() == 4) {
						mArrow.setImageResource(R.drawable.youhou);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往右后方转");
					} else if (point.getAction() == 5) {
						mArrow.setImageResource(R.drawable.zuohou);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往左后方转");
					} else if (point.getAction() == 6) {
						mArrow.setImageResource(R.drawable.zuozhuan);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往左转");
					} else if (point.getAction() == 7) {
						mArrow.setImageResource(R.drawable.zuoqian);
						mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
								+ "米\n在" + point.getAroundPoiName() + "往左前方转");
					}
				} else if (sign == RouteLayer.NAVIGATE_STOP) {// 结束导航

				} else if (sign == RouteLayer.ARRIVED) {// 到达终点
					DTUIUtil.showToastSafe("已到达终点附近");
					mNaLayout.setVisibility(View.VISIBLE);
					mNavigateBottomLayout.setVisibility(View.GONE);
					mNavigateTopLayout.setVisibility(View.GONE);
				} else if (sign == RouteLayer.REPLAN_ROUTE_START) {// 已经偏离路线，重新规划
					Toast.makeText(getApplicationContext(), "已偏离路线，重新规划",
							Toast.LENGTH_LONG).show();
				} else if (sign == RouteLayer.NAVIGATE_FAIL) {// 导航无法开启

				}
				break;
			case 1:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(1)
							.getText());
					AirSqlite.getInstance().update(1);
					mDialogOk.setTag(1);
					mDialogOk.setText("前往安检");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(APMapActivity.this, 1000); // 震动100ms
				}
				break;
			case 2:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(2)
							.getText());
					AirSqlite.getInstance().update(2);
					mDialogOk.setTag(2);
					mDialogOk.setText("查看");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(APMapActivity.this, 1000); // 震动100ms
				}
				break;
			case 4:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(3)
							.getText());
					AirSqlite.getInstance().update(3);
					mDialogOk.setTag(3);
					mDialogOk.setText("前往乘机");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(APMapActivity.this, 1000); // 震动100ms
				}
				break;
			}
		}
	};

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, APMapActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		mMenu = (ImageView) findViewById(R.id.menu);
		mMenu.setOnClickListener(this);
		mSearch = (ImageView) findViewById(R.id.search);
		mSearch.setOnClickListener(this);
		mList = (ListView) findViewById(R.id.floor_list);
		mList.setOnItemClickListener(this);
		mAdater = new FloorAdapter();
		mTitle = (TextView) findViewById(R.id.title);
		mLocTitle = (TextView) findViewById(R.id.na_title);
		mTitleLayout = (RelativeLayout) findViewById(R.id.rl_title);
		mNaTitleLayout = (RelativeLayout) findViewById(R.id.na_title_layout);
		
		mAirNotify = mGson.fromJson(AirData.AIR_NOTIFY, POI.class);

		mLocBtn = (ImageView) findViewById(R.id.btn_my_location);
		mLocBtn.setOnClickListener(this);
		mArBtn = (ImageView) findViewById(R.id.ar_btn);
		mArBtn.setOnClickListener(this);
		findViewById(R.id.na_cancel).setOnClickListener(this);// 取消路线规划
		findViewById(R.id.na_start).setOnClickListener(this);// 开始导航

		mTextList = new ArrayList<String>();
		mTextIndexList = new ArrayList<Integer>();
		mPoiList = new ArrayList<POI>();

		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(getAssets()
							.open("t2.json"), "utf-8"));
					String line;
					StringBuilder builder = new StringBuilder();
					while ((line = br.readLine()) != null) {
						// 将文本打印到控制台
						builder.append(line);
					}
					br.close();
					String s = builder.toString();
					JSONObject o = new JSONObject(s);
					JSONArray l = o.getJSONArray("RECORDS");
					for (int i = 0; i < l.length(); i++) {
						JSONObject obj = l.getJSONObject(i);
						POI poi = new POI();
						poi.setPoiNo(Integer.parseInt(obj.getString("poi_no")));
						poi.setName(obj.getString("name"));
						poi.setFloor(obj.getString("floor"));
						poi.setBuildId(obj.getString("buildid"));
						poi.setAddress(obj.getString("address"));
						poi.setDesc(obj.getString("descipt"));
						poi.setLogoImage(obj.getString("logo"));
						poi.setPoiImage(obj.getString("image"));
						poi.setClassname(obj.getString("type"));
						poi.setHours(obj.getString("time"));
						poi.setCurrecy(obj.getString("currecy"));
						mPoiList.add(poi);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mNaLayout = (RelativeLayout) findViewById(R.id.na_layout);
		mNaLayout.setVisibility(View.GONE);

		mNavigateTopLayout = (RelativeLayout) findViewById(R.id.na_top_layout);
		mNavigateBottomLayout = (RelativeLayout) findViewById(R.id.na_bottom_layout);
		mArrow = (ImageView) findViewById(R.id.na_arrow);// 导航箭头
		mNaText = (TextView) findViewById(R.id.na_text);// 导航提示
		mNaDistance = (TextView) findViewById(R.id.distance);// 导航距离
		findViewById(R.id.stop_navigate).setOnClickListener(this);
		mRecoverNavigate = (TextView) findViewById(R.id.na_recover);
		mRecoverNavigate.setOnClickListener(this);

		mContent = (TextView) findViewById(R.id.na_content);
		mLeft = (ImageView) findViewById(R.id.left);
		mRight = (ImageView) findViewById(R.id.right);

		mLeft.setOnClickListener(this);
		mRight.setOnClickListener(this);

		mTitle.setText(mBuild.getBuildName());
		mAdater.addList(mBuild.getFloorlist());
		mList.setAdapter(mAdater);

		initMap();

		initPoiDialog();
		initDialog();
		if (AirSqlite.getInstance().getMsgInfo(1).getGone() == 0) {
			mHandler.sendEmptyMessageDelayed(1, 2 * 60 * 1000);
		}
		if (AirSqlite.getInstance().getMsgInfo(2).getGone() == 0)
			mHandler.sendEmptyMessageDelayed(2, 4 * 60 * 1000);
		if (AirSqlite.getInstance().getMsgInfo(4).getGone() == 0)
			mHandler.sendEmptyMessageDelayed(3, 5 * 60 * 1000);

	}

	private void initDialog() {
		mDialog = new Dialog(this, R.style.dialog);
		mDialog.setContentView(R.layout.msg_dialog);
		mDialog.setCanceledOnTouchOutside(true);
		mSignText = (TextView) mDialog.findViewById(R.id.sign);

		mDialogOk = (TextView) mDialog.findViewById(R.id.ok);
		mDialogCancel = (TextView) mDialog.findViewById(R.id.cancel);

		mDialogOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
				int sign = (Integer) v.getTag();
				if (sign == 1) {
					startNavigation();
				} else if (sign == 2) {
				} else if (sign == 3) {
				} else if (sign == 4) {

				}
			}
		});
		mDialogCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {
		MapView.MAP_SCREEN_SCALE = 5f;
		Handlerlist.getInstance().register(mHandler);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setMapSortRule(3);
		mMapView.addDrawPoiHighLevel(100001);
		mMapView.addDrawPoiHighLevel(130000);

		mMapView.setOnMapModeChangedListener(new OnMapModeChangedListener() {

			@Override
			public void onMapModeChanged() {
				if (mRouteLayer.isNavigating()) {
					if (mMapView.getLocationMode() == RMLocationMode.NORMAL) {
						mRecoverNavigate.setVisibility(View.VISIBLE);
						findViewById(R.id.textview8).setVisibility(View.GONE);
						mNaDistance.setVisibility(View.GONE);
					}
				}
			}
		});

		initLayers();// 初始化图层

		Drawable blue = getResources().getDrawable(R.drawable.da_marker_red);
		mBitmap = DTIOUtil.drawableToBitmap(blue);
		mTapPoiLayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的气泡view
				if (mNaLayout.getVisibility() == View.VISIBLE)
					return null;
				DTLog.i(new Gson().toJson(poi));
				// mNavigationList.add(poi);
				mPoiName.setText(poi.getName());
				for (Floor floor : mBuild.getFloorlist()) {
					if (floor.getFloor().equals(poi.getFloor())) {
						mPoiFloor.setText(poi.getFloor() + "-"
								+ floor.getDescription());
					}
				}
				mPoi = poi;
				for (int i = 0; i < mPoiList.size(); i++) {
					POI p = mPoiList.get(i);
					if (p.getFloor().equals(mPoi.getFloor())
							&& mPoi.getPoiNo() == p.getPoiNo()) {
						p.setX(mPoi.getX());
						p.setY(mPoi.getY());
						mPoi = p;
						break;
					}
				}
				if (DTStringUtil.isEmpty(mPoi.getLogoImage())
						|| DTStringUtil.isEmpty(mPoi.getPoiImage())) {
					mPoiInfo.setVisibility(View.GONE);
				} else {
					mPoiInfo.setVisibility(View.VISIBLE);
				}
				Window window = mPoiDialog.getWindow();
				window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
				WindowManager windowManager = getWindowManager();
				Display display = windowManager.getDefaultDisplay();
				WindowManager.LayoutParams lp = mPoiDialog.getWindow()
						.getAttributes();
				lp.width = (int) (display.getWidth()); // 设置宽度
				mPoiDialog.getWindow().setAttributes(lp);
				mPoiDialog.show();
				return mBitmap;

			}
		});
		mMapView.initMapConfig(mBuild.getBuildId(), mBuild.getFloorlist()
				.get(1).getFloor());
		mAdater.setCurrentIndex(mBuild.getFloorlist().get(1).getFloor());
//		mMapView.startSensor();// 开启指针方向
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mTapPoiLayer = new TapPOILayer(mMapView);
		mMapView.addMapLayer(mTapPoiLayer);
		mMapView.addMapLayer(mCompassLayer);
		Drawable poiicon = getResources().getDrawable(
				R.drawable.icon_gcoding_red);
		mPoiLayer = new POILayer(mMapView, DTIOUtil.drawableToBitmap(poiicon));
		mMapView.addMapLayer(mPoiLayer);
		Drawable start = getResources().getDrawable(R.drawable.navi_start);
		mNaPoiLayer = new POILayer(mMapView, DTIOUtil.drawableToBitmap(start));
		mMapView.addLayer(mNaPoiLayer);
		Drawable end = getResources().getDrawable(R.drawable.navi_end);
		mRouteLayer = new RouteLayer(mMapView,
				DTIOUtil.drawableToBitmap(start),
				DTIOUtil.drawableToBitmap(end), null);
		mMapView.addMapLayer(mRouteLayer);
	}

	private void initPoiDialog() {
		mPoiDialog = new Dialog(this, R.style.dialog_white);
		mPoiDialog.setContentView(R.layout.poi_tap_dialog);
		mPoiDialog.setCanceledOnTouchOutside(true);
		mCome = (RelativeLayout) mPoiDialog.findViewById(R.id.come);
		mGo = (RelativeLayout) mPoiDialog.findViewById(R.id.go);
		mPoiInfo = (RelativeLayout) mPoiDialog.findViewById(R.id.info);
		mPoiName = (TextView) mPoiDialog.findViewById(R.id.poi_name);
		mPoiFloor = (TextView) mPoiDialog.findViewById(R.id.floor_info);

		mCome.setOnClickListener(this);
		mGo.setOnClickListener(this);
		mPoiInfo.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBookmsg = AirSqlite.getInstance().getMsgInfo(4);
		LocationApp.getInstance().registerLocationListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(1);
		mHandler.removeMessages(2);
		mHandler.removeMessages(3);
		Handlerlist.getInstance().remove(mHandler);
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(final RMLocation result) {
//		result.setX(383f);
//		result.setY(200f);
//		result.setError(0);
//		result.setBuildID(mBuild.getBuildId());
//		result.setFloor("F2");
		if (result.getError() == 0) {
			ArManager.instance().notifyLocationChanged(
					new ArLocation(mBuild.getBuildId(), result.getFloor(),
							result.getCoordX(), result.getCoordY(), 0));
			// Log.i("rtmap",
			// "result : " + result.getCoordX() + "    "
			// + result.getCoordY() + "   " + result.getFloorID());
			// *********如果固定在某一建筑物的某一楼层定位，则这段代码可以写在onCreate中
			if (mLocation != null && mLocation.getError() == 0
					&& result.getBuildID().equals(mBuild.getBuildId())
					&& !result.getFloor().equals(mLocation.getFloor())) {
				mAdater.setCurrentIndex(result.getFloor());
				mAdater.notifyDataSetChanged();
				mMapView.initMapConfig(result.getBuildID(), result.getFloor());
			}
			RMPoiDetailUtil.getPoiInfo(result, null,
					new RMPoiDetailUtil.OnGetPoiDetailListener() {

						@Override
						public void onFinished(RMPoiDetail r) {
							if (r.getError_code() == 0) {

								mTitle.setText(mBuild.getBuildName() + "-"
										+ result.getFloor() + "\n"
										+ r.getPoi().getName() + "附近");
								mLocTitle.setText(mBuild.getBuildName() + "-"
										+ result.getFloor() + "\n"
										+ r.getPoi().getName() + "附近");
							}
						}
					});

			double dis = RMathUtils.distance(result.x, result.y,
					mAirNotify.getX(), mAirNotify.getY());
			if (mBookmsg.getGone() == 0 && dis < 20
					&& result.getFloor().equals(mAirNotify.getFloor())) {
				// mHandler.sendEmptyMessageDelayed(8, 1000);
				AirSqlite.getInstance().update(mBookmsg.getMid());
				mBookmsg.setGone(1);
				if (!mDialog.isShowing()) {
					mSignText.setText("中信书店有活动哦");
					AirSqlite.getInstance().update(4);
					mDialogOk.setTag(4);
					mDialogOk.setText("确定");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(APMapActivity.this, 1000); // 震动100ms
				}
				DTUIUtil.showToastSafe("你已到达" + mAirNotify.getName());
			}
		} else {
			// Log.i("rtmap", result.getErrorInfo());
		}
		mMapView.setMyCurrentLocation(result);
		mLocation = result;
	}

	private int mIndex = 0;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu:
			mDrawer.openDrawer(Gravity.LEFT);
			break;
		case R.id.search:
			Intent intent = new Intent(this, APSearchActivity.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.na_start:// 开始导航
			mRouteLayer.startNavigate();
			mNavigateBottomLayout.setVisibility(View.VISIBLE);
			mNavigateTopLayout.setVisibility(View.VISIBLE);
			mNaLayout.setVisibility(View.GONE);
			break;
		case R.id.stop_navigate:
			mRouteLayer.stopNavigate();
			mNaLayout.setVisibility(View.VISIBLE);
			mNavigateBottomLayout.setVisibility(View.GONE);
			mNavigateTopLayout.setVisibility(View.GONE);
			break;
		case R.id.na_recover:// 恢复导航
			mRecoverNavigate.setVisibility(View.GONE);
			findViewById(R.id.textview8).setVisibility(View.VISIBLE);
			mNaDistance.setVisibility(View.VISIBLE);
			mMapView.setLocationMode(RMLocationMode.COMPASS);
			break;
		case R.id.come:
			mPoiDialog.cancel();
			mPoiStart = mPoi;
			if (mPoiStart != null) {
				mTapPoiLayer.destroyLayer();
				mNaPoiLayer.destroyLayer();
			}
			mNaPoiLayer.addPoi(mPoiStart);
			mMapView.refreshMap();
			startNavigation();
			break;
		case R.id.btn_my_location:
			if (mLocation != null && mLocation.getError() == 0)
				mMapView.setCenter(mLocation.getX(), mLocation.getY());
			break;
		case R.id.na_cancel:
			mPoiEnd = null;
			mPoiStart = null;
			mRouteLayer.destroyLayer();
			mMapView.refreshMap();
			mTitleLayout.setVisibility(View.VISIBLE);
			mNaTitleLayout.setVisibility(View.GONE);
			mArBtn.setVisibility(View.VISIBLE);
			mLocBtn.setVisibility(View.VISIBLE);
			mNaLayout.setVisibility(View.GONE);
			break;
		case R.id.ar_btn:
			Intent intent2 = new Intent(this, ArShowActivity.class);
			intent2.putExtra(ArShowActivity.KEY_MAP_DEGREE,
					mMapView.getMapAngle());
			startActivity(intent2);
			break;
		case R.id.left:
			if (mIndex > 0) {
				mIndex--;
				changeNavigationText();
			}
			break;
		case R.id.right:
			if (mIndex < mTextList.size() - 2) {
				mIndex++;
				changeNavigationText();
			}
			break;
		case R.id.go:
			mPoiDialog.cancel();
			mPoiEnd = mPoi;
			startNavigation();
			break;
		case R.id.info:// 详情
			Intent intent1 = new Intent(this, APoiInfoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("poi", mPoi);
			intent1.putExtras(bundle);
			startActivityForResult(intent1, 111);
			mPoiDialog.cancel();
			break;
		}
	}

	/**
	 * 改变导航路书
	 */
	private void changeNavigationText() {
		NavigatePoint point = mRouteLayer.getNavigatePoints().get(
				mTextIndexList.get(mIndex));
		if (!mMapView.getBuildId().equals(point.getBuildId())
				|| !mMapView.getFloor().equals(point.getFloor())) {
			for (int i = 0; i < mBuild.getFloorlist().size(); i++) {
				if (mBuild.getFloorlist().get(i).getFloor()
						.equals(point.getFloor())) {
					// mFloorText.setText(point.getFloor());
					break;
				}
			}
			mMapView.initMapConfig(point.getBuildId(), point.getFloor());
		}
		mRouteLayer.setKeyRouteIndex(mTextIndexList.get(mIndex),
				mTextIndexList.get(mIndex + 1));
		mContent.setText((mIndex + 1) + "/" + (mTextList.size() - 1) + "   从"
				+ mTextList.get(mIndex) + "到" + mTextList.get(mIndex + 1));
	}

	/**
	 * 开始导航
	 */
	private void startNavigation() {
		if (mPoiEnd != null) {
			if (mPoiStart == null && mLocation != null
					&& mLocation.getError() == 0
					&& mLocation.getBuildID().equals(mBuild.getBuildId())) {
				mPoiStart = new POI(0, "我的位置", mLocation.getBuildID(),
						mLocation.getFloor(), mLocation.getX(),
						mLocation.getY());
			}
			if (mPoiStart != null) {
				mTapPoiLayer.destroyLayer();
				mLoadDialog.setMessage("正在导航..");
				mLoadDialog.show();
				RMNavigationUtil.requestNavigation(XunluMap.getInstance()
						.getApiKey(), mBuild.getBuildId(), mPoiStart, mPoiEnd,
						null, false, this);
			}
		}
	}

	@Override
	public void onFinished(RMRoute route) {
		mLoadDialog.cancel();
		mLoadDialog.setMessage("");
		DTLog.i(mGson.toJson(route));
		if (route.getError_code() == 0) {
			mTitleLayout.setVisibility(View.GONE);
			mNaTitleLayout.setVisibility(View.VISIBLE);
			// mLocTitle.setText("当前位置->\n" + mAirGate.getName());
			mTextList.clear();
			mTextIndexList.clear();
			mNaPoiLayer.destroyLayer();
			for (int i = 0; i < route.getPointlist().size(); i++) {
				NavigatePoint p = route.getPointlist().get(i);
				if (!DTStringUtil.isEmpty(p.getAroundPoiName())) {
					mTextIndexList.add(i);
					mTextList.add(p.getAroundPoiName());
				}
			}
			mRouteLayer.setNavigatePoints(route.getPointlist());
			if (mTextList.size() > 0) {
				mNaLayout.setVisibility(View.VISIBLE);
				mArBtn.setVisibility(View.GONE);
				mLocBtn.setVisibility(View.GONE);
				mIndex = 0;
				changeNavigationText();
			}
			// mNavigationList.clear();
			mMapView.refreshMap();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mAdater.setCurrentIndex(mAdater.getItem(arg2).getFloor());
		mMapView.initMapConfig(mBuild.getBuildId(),
				mBuild.getFloorlist().get(arg2).getFloor());
		mAdater.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg1 == Activity.RESULT_OK) {
			if (arg0 == 111) {
				int code = arg2.getIntExtra("type", 1);
				if (code == 1) {
					mPoiStart = mPoi;
					if (mPoiStart != null) {
						mTapPoiLayer.destroyLayer();
						mNaPoiLayer.destroyLayer();
						mMapView.refreshMap();
					}
				} else {
					mPoiEnd = mPoi;
					startNavigation();
				}
			}else if(arg0==100){
				POI poi= (POI) arg2.getExtras().getSerializable("poi");
				mTapPoiLayer.setPOI(poi);
				if(!mMapView.getFloor().equals(poi.getFloor())){
					mMapView.initMapConfig(mBuild.getBuildId(), poi.getFloor());
				}
				mMapView.refreshMap();
			}
		}
	}
}
