package com.tss.quikpawn;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.tss.quikpawn.activities.LoginActivity;
import com.tss.quikpawn.models.DialogParamModel;
import com.tss.quikpawn.util.DialogUtil;

import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity {
	public static final int SHOW_MSG = 0;

	private int showLineNum = 0;

	private LinearLayout linearLayout;
	private ScrollView scrollView;
	private TextView textView1;
	private TextView textView2;

	public LinearLayout rightButArea = null;

	public AidlDeviceManager manager = null;


	public void showResponse(String status, final Context context) {
		if (status.equals( "401")) {
            ArrayList<String> msg = new ArrayList<>();
            msg.add("การยืนยันตัวตนล้มเหลว");
			DialogParamModel param = new DialogParamModel("ปัญหายืนยันตัวตน", msg,
							getString(R.string.text_ok),"");
			DialogUtil.showConfirmDialog(param, context, new DialogUtil.InputTextBackListerner() {
                @Override
                public void onClickConfirm(String result) {
                    if (DialogUtil.CONFIRM.equals(result)) {
                        finish();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            });
		} else if (status.equals("201")) {
            ArrayList<String> msg = new ArrayList<>();
            msg.add("กรุณาเพิ่มอุปกรณ์ในระบบ");
            DialogParamModel param = new DialogParamModel("ไม่พบอุปกรณ์ในระบบ", msg,
                    getString(R.string.text_ok),"");
            DialogUtil.showConfirmDialog(param, context, new DialogUtil.InputTextBackListerner() {
                @Override
                public void onClickConfirm(String result) {
                    if (DialogUtil.CONFIRM.equals(result)) {
                        finish();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            });
		} else if (status.equals("202")) {
            ArrayList<String> msg = new ArrayList<>();
            msg.add("แพ็คเกจหมดอายุ");
            DialogParamModel param = new DialogParamModel("กรุณาติดต่อผู้ดูแลระบบ", msg,
                    getString(R.string.text_ok),"");
            DialogUtil.showConfirmDialog(param, context, new DialogUtil.InputTextBackListerner() {
                @Override
                public void onClickConfirm(String result) {
                    if (DialogUtil.CONFIRM.equals(result)) {
                        finish();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            });
		} else if (status.equals("203") || status.equals("204")) {
			ArrayList<String> msg = new ArrayList<>();
			msg.add("ไม่พบรายการหรือสินค้านี้");
			DialogParamModel param = new DialogParamModel("กรุณาติดต่อผู้ดูแลระบบ", msg,
					getString(R.string.text_ok),"");
			DialogUtil.showConfirmDialog(param, context, new DialogUtil.InputTextBackListerner() {
				@Override
				public void onClickConfirm(String result) {
				}
			});
		}
		else {
			DialogUtil.showNotiDialog(context, getString(R.string.connect_error), getString(R.string.connect_error_please_reorder));
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String msg1 = bundle.getString("msg1");
			String msg2 = bundle.getString("msg2");
			int color = bundle.getInt("color");
//			updateView(msg1, msg2, color);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		bindService();
		
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		unbindService(connNew);
		unbindService(connNew2);
	}
	
 class MyBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("base", "action:" +intent.getAction());
		
	}
	 
 }

	public void bindService() {
		Intent intent = new Intent();
		intent.setPackage("com.centerm.smartposservice");
		intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
		bindService(intent, conn, Context.BIND_AUTO_CREATE);

		intent = new Intent();
		intent.setPackage("com.centerm.centermposoverseaservice");
		intent.setAction("com.centerm.CentermPosOverseaService.MANAGER_SERVICE");
		bindService(intent, connNew2, Context.BIND_AUTO_CREATE);

		intent = new Intent();
		intent.setPackage("com.centerm.smartposservice");
		intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
		bindService(intent, connNew, Context.BIND_AUTO_CREATE);


	}

	/**
	 * 服务连接桥
	 */
	public ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			manager = AidlDeviceManager.Stub.asInterface(service);
			com.centerm.centermposoversealib.util.LogUtil.print("success1");
			com.centerm.centermposoversealib.util.LogUtil.print("manager1 = " + manager);
			if (null != manager) {
				onPrintDeviceConnected(manager);
			}
		}
	};

	public ServiceConnection connNew2 = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			manager = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			manager = AidlDeviceManager.Stub.asInterface(service);
			if (null != manager) {
				onDeviceConnected(manager, false);
			}
		}
	};

	public ServiceConnection connNew = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			manager = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			manager = AidlDeviceManager.Stub.asInterface(service);
			if (null != manager) {
				onDeviceConnected(manager, true);
			}
		}
	};
	
	//清屏
	public void clear(){
		linearLayout.removeAllViews();
	}


	/**
	 * 更新UI
	 * 
	 * @param msg1
	 * @param msg2
	 * @param color
	 * @createtor：Administrator
	 * @date:2014-11-29 下午7:01:16
	 */
	public void showMessage(final String msg1, final String msg2,
                            final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", msg2);
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	// 显示单条信息
	public void showMessage(final String msg1, final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", "");
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public void showMessage(String str) {
		this.showMessage(str, Color.BLACK);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		if(!this.getClass().getName().equals(MainActivity.class)){	//首页不执行解除绑定服务操作
//			unbindService(conn);
//		}
	}

	/**
	 * 设备服务连接成功时回调
	 * @param deviceManager
	 * @createtor：Administrator
	 * @date:2015-5-4 下午1:52:13
	 */
	public abstract void onDeviceConnected(AidlDeviceManager deviceManager, boolean capy);
	public abstract void onPrintDeviceConnected(AidlDeviceManager deviceManager);

}
