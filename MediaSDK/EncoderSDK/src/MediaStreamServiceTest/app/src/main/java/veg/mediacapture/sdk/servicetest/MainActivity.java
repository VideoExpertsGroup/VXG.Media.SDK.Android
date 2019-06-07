package veg.mediacapture.sdk.servicetest;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCaptureConfig;

public class MainActivity extends AppCompatActivity {
	public static final String BROADCAST_ACTION = "veg.mediacapture.sdk.servicetest.ACTION";
	public static final String PARAM_STATUS = "STATUS";
	public static final String PARAM_STATUS_TEXT = "STATUS_TEXT";
	public static final String TAG = "Background";
	public static final int STATUS_SERVICE_STARTED = 0x100;
	public static final int STATUS_SERVICE_STOPPED = 0x200;
	public static final int STATUS_UPDATE = 0x300;
	private Button button_open;
	private Button button_stream_main_rtmp;
	private Button button_stream_sec_rtmp;
	private Button button_stream_main_rtsp;
	private Button button_stream_sec_rtsp;
	private Button button_rec_main;
	private Button button_rec_sec;
	private Intent serviceIntent = null;
	private MediaCaptureService service = null;
	private Boolean bound = false;
	static public int permissionRequestResultCode = RESULT_OK;
	static public Intent permissionRequestResultInent = null;

	private boolean isServiceRunning() {
		Class serviceClass = MediaCaptureService.class;
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean opened = false;
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra(PARAM_STATUS, 0);

			switch (status) {
				case STATUS_SERVICE_STARTED:
					Log.d(TAG, "Service started\n");
					((TextView) findViewById(R.id.satus_text)).setText("Service started");
					opened = true;
					update_ui();
					break;
				case STATUS_SERVICE_STOPPED:
					Log.d(TAG, "Service stopped\n");
					((TextView) findViewById(R.id.satus_text)).setText("Service stopped");
					opened = false;
					update_ui();
					break;
				case STATUS_UPDATE:
					String text = intent.getStringExtra(PARAM_STATUS_TEXT);
					((TextView) findViewById(R.id.satus_text)).setText(text);
					break;
				default:
					Log.d(TAG, "Unknown status");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");

		button_open = (Button) findViewById(R.id.button_open);
		button_stream_main_rtmp = (Button) findViewById(R.id.button_start_stream_main);
		button_stream_sec_rtmp = (Button) findViewById(R.id.button_start_stream_sec);
		button_stream_main_rtsp = (Button) findViewById(R.id.button_start_stream_main_rtsp);
		button_stream_sec_rtsp = (Button) findViewById(R.id.button_start_stream_sec_rtsp);
		button_rec_main = (Button) findViewById(R.id.button_start_rec_main);
		button_rec_sec = (Button) findViewById(R.id.button_start_rec_sec);
		update_ui();

		serviceIntent = new Intent(this, MediaCaptureService.class);
		if (isServiceRunning()) {
			opened = true;
			Log.d(TAG, "Service started\n");
			((TextView) findViewById(R.id.satus_text)).setText("Service started");
			update_ui();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
			) {
				ActivityCompat.requestPermissions(this, new String[]{
						Manifest.permission.CAMERA,
						Manifest.permission.RECORD_AUDIO,
						Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				}, 1);
			}
		}

		button_open.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!opened) {
					opened = true;
					startService(serviceIntent);
					bindService(serviceIntent, connection, 0);
				} else {
					Log.d(TAG, "Stopping service");
					opened = false;
					stopService(serviceIntent);
					update_ui();
				}
			}
		});

		button_stream_main_rtmp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null) {
					Log.d(TAG, "button_stream_main Service not avail service=" + service + " service.capturer=" + (service != null ? service.capturer : null));
					return;
				}
				if (!service.capturer.getConfig().isStreaming() || !service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), false)) {
					service.capturer.StartStreaming(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val());
				} else {
					service.capturer.StopStreaming(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val());
				}
				update_ui();
			}
		});

		button_stream_sec_rtmp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null)
					return;
				if (!service.capturer.getConfig().isStreaming() || !service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), true)) {
					service.capturer.StartStreamingSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val());
				} else {
					service.capturer.StopStreamingSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val());
				}
				update_ui();
			}
		});

		button_stream_main_rtsp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null) {
					Log.d(TAG, "button_stream_main Service not avail service=" + service + " service.capturer=" + (service != null ? service.capturer : null));
					return;
				}
				if (!service.capturer.getConfig().isStreaming() || !service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), false)) {
					service.capturer.StartStreaming(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
				} else {
					service.capturer.StopStreaming(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
				}
				update_ui();
			}
		});

		button_stream_sec_rtsp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null)
					return;
				if (!service.capturer.getConfig().isStreaming() || !service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), true)) {
					service.capturer.StartStreamingSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
				} else {
					service.capturer.StopStreamingSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
				}
				update_ui();
			}
		});

		button_rec_main.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null)
					return;
				if (!service.capturer.getConfig().isRecording() || !service.capturer.getConfig().isUseMainRecording()) {
					service.capturer.StartRecording();
				} else {
					service.capturer.StopRecording();
				}
				update_ui();
			}
		});

		button_rec_sec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null || service.capturer == null)
					return;
				if (!service.capturer.getConfig().isRecording() || !service.capturer.getConfig().isUseSecRecord()) {
					service.capturer.StartRecordingSec();
				} else {
					service.capturer.StopRecordingSec();
				}
				update_ui();
			}
		});

	}


	ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder s) {
			MediaCaptureService.MyBinder binder = (MediaCaptureService.MyBinder) s;
			service = binder.getService();
			bound = true;
			Log.d(TAG, "onServiceConnected service=" + service);
			update_ui();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
			Log.d(TAG, "onServiceDisconnected service=" + service);
		}
	};

	boolean isCapturer() {
		return (service != null && service.capturer != null);
	}

	private void update_ui() {
		button_stream_main_rtmp.setEnabled(opened);
		button_stream_main_rtmp.setText((isCapturer() && service.capturer.getConfig().isStreaming() && service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), false)) ? "Stop Stream Main RTMP" : "Start Stream Main RTMP");

		button_stream_sec_rtmp.setEnabled(opened);
		button_stream_sec_rtmp.setText((isCapturer() && service.capturer.getConfig().isStreaming() && service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), true)) ? "Stop Stream Sec RTMP" : "Start Stream Sec RTMP");

		button_stream_main_rtsp.setEnabled(opened);
		button_stream_main_rtsp.setText((isCapturer() && service.capturer.getConfig().isStreaming() && service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), false)) ? "Stop Stream Main RTSP" : "Start Stream Main RTSP");

		button_stream_sec_rtsp.setEnabled(opened);
		button_stream_sec_rtsp.setText((isCapturer() && service.capturer.getConfig().isStreaming() && service.capturer.getConfig()._getStreamingOn(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), true)) ? "Stop Stream Sec RTSP" : "Start Stream Sec RTSP");

		button_rec_main.setEnabled(opened);
		button_rec_main.setText((isCapturer() && service.capturer.getConfig().isRecording() && service.capturer.getConfig().isUseMainRecording()) ? "Stop Rec Main" : "Start Rec Main");

		button_rec_sec.setEnabled(opened);
		button_rec_sec.setText((isCapturer() && service.capturer.getConfig().isRecording() && service.capturer.getConfig().isUseSecRecord()) ? "Stop Rec Sec" : "Start Rec Sec");

		button_open.setText(opened ? "Close service" : "Open service");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
		registerReceiver(broadcastReceiver, intentFilter);

		if (service == null && isServiceRunning()) {
			bindService(serviceIntent, connection, 0);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		unregisterReceiver(broadcastReceiver);

		if (connection != null) {
			unbindService(connection);
		}
		service = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
}
