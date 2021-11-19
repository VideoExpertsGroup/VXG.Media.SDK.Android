/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */

package veg.mediaplayer.sdk.drawovervideo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerProperties;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.Position;
import veg.mediaplayer.sdk.MediaPlayerConfig;
import veg.mediaplayer.sdk.PlaySegment;


public class MainActivity extends Activity
        implements MediaPlayer.MediaPlayerCallback, MediaPlayer.MediaPlayerCallbackSubtitle, SurfaceHolder.Callback2, MediaPlayer.MediaPlayerCallbackData {
    private static final String TAG = "DrawOverVideo.MediaPlayerTest";

    public static AutoCompleteTextView edtIpAddress;
    public static ArrayAdapter<String> edtIpAddressAdapter;
    public static ArrayList<String> edtIpAddressHistory = new ArrayList<String>();
    private Button btnConnect;
    private Button btnHistory;
    private Button btnShot;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private boolean playing = false;
    private MediaPlayer player = null;
    private MainActivity mthis = null;

    private RelativeLayout playerStatus = null;
    private TextView playerStatusText = null;
    private TextView playerHwStatus = null;

    private LinearLayout playerViewLayout = null;
    private RelativeLayout layoutSeekBar = null;
    private RelativeLayout layoutPlaybackBar = null;
    private LinearLayout layoutConnectionUrlBar = null;
    private LinearLayout layoutSettingsBar = null;
    private RelativeLayout layoutConnectionBar = null;

    private TextView textPlaybackPositionCurrent = null;
    private TextView textPlaybackPositionMax = null;
    private SeekBar seekPlaybackPosition = null;
   
    private RelativeLayout playerLayoutMulti = null;
    private Spinner playerMultiSubtitles = null;
    private Spinner playerMultiAudio = null;
    private ArrayAdapter<String> spinnerArrayAdapterMultiSubtitles = null;
    private ArrayAdapter<String> spinnerArrayAdapterMultiAudio = null;

    private MulticastLock multicastLock = null;
    private ProgressDialog progress = null;

    private ToggleButton buttonSynchro = null;
    //private ToggleButton buttonOmx = null;
    private ToggleButton buttonDropFrames = null;
    private ToggleButton buttonLogging = null;

    private static int LOG_LEVEL = -1;

    private String autoStartUrl = "";

    private boolean autoStart = false;
    private int autoStartDelay = 3000;

    private boolean autoClose = false;
    private int autoCloseAfterDelay = 60000;

    private boolean autoEnableOMX = false;
    private boolean autoEnableSynchro = true;
    private boolean autoEnableSynchroDrop = true;

    private Timer autoStartTimer = null;
    private Timer autoCloseTimer = null;

    private SurfaceView externalView = null;
    private Surface surface = null;

    private ImageView subtitleImageView = null;
    private TextView subtitleTextView = null;
    private FrameLayout barFrameLayout = null;

    private enum PlayerStates {
        Busy, ReadyForUse
    };

    private enum PlayerConnectType {
        Normal, Reconnecting
    };

    private Object waitOnMe = new Object();
    private PlayerStates player_state = PlayerStates.ReadyForUse;
    private PlayerConnectType reconnect_type = PlayerConnectType.Normal;
    private int mOldMsg = 0;

    private Toast toastShot = null;
    private String versionName = "";

    private Timer timerPlayerPosition = null;
    
    boolean isSeekPlaybackPositionThumbCaptured = false;

    private int drawDelay = 2000;
    private Timer drawTimer = null;

    // Event handler
    private Handler handler = new Handler() {
        String strText = "Connecting";

        String sText;
        String sCode;

        @Override
        public void handleMessage(Message msg) {
            PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
            switch (status) {
            case CP_CONNECT_STARTING:
                if (reconnect_type == PlayerConnectType.Reconnecting)
                    strText = "Reconnecting";
                else
                    strText = "Connecting";

                player_state = PlayerStates.Busy;
                showStatusView();

                reconnect_type = PlayerConnectType.Normal;
                setShowPlaybackControls();
                break;

            case PLP_BUILD_SUCCESSFUL:
                sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
                Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText=" + sText + " sCode=" + sCode);
                break;

            case VRP_NEED_SURFACE:
                player_state = PlayerStates.Busy;
                showVideoView();
                break;

            case PLP_PLAY_SUCCESSFUL:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("");
                setTitle(getResources().getText(R.string.app_name) + versionName);

                updateLayoutMulti();
                updateLayoutSeekbar(true);

                drawTimer = new Timer();
                drawTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                testDrawRects(player.getVideoWidth(), player.getVideoHeight());
                            }
                        });
                    }

                }, drawDelay);

                break;

            case PLP_CLOSE_STARTING:
                player_state = PlayerStates.Busy;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setUIDisconnected();
                break;

            case PLP_CLOSE_SUCCESSFUL:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                System.gc();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case PLP_CLOSE_FAILED:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case CP_CONNECT_FAILED:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case PLP_BUILD_FAILED:
                sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
                Log.i(TAG, "=Status PLP_BUILD_FAILED: Response sText=" + sText + " sCode=" + sCode);

                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case PLP_PLAY_FAILED:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case PLP_ERROR:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case CP_INTERRUPTED:
                player_state = PlayerStates.ReadyForUse;
                playerStatusText.setText("Disconnected");
                showStatusView();
                setShowConnectionControls();
                setUIDisconnected();
                break;

            case CP_START_BUFFERING:
                // if (progress == null)
                // progress = ProgressDialog.show(mthis, null,
                // "buffering...", true);
                break;

            case CP_STOP_BUFFERING:
                // if (progress != null)
                // {
                // progress.dismiss();
                // progress = null;
                // }
                break;

            // case CP_CONNECT_AUTH_SUCCESSFUL:
            // Log.v(TAG, "=handleMessage CP_CONNECT_AUTH_SUCCESSFUL");
            // {
            // Toast.makeText(getApplicationContext(),"Authentification ok!",
            // Toast.LENGTH_SHORT).show();
            // }
            // break;
            //
            // case CP_CONNECT_AUTH_FAILED:
            // Log.v(TAG, "=handleMessage CP_CONNECT_AUTH_FAILED");
            // {
            // Toast.makeText(getApplicationContext(),"Authentification
            // failed!", Toast.LENGTH_SHORT).show();
            // }
            // break;

            // case CONTENT_PROVIDER_ERROR_DISCONNECTED:
            case CP_STOPPED:
            case VDP_STOPPED:
            case VRP_STOPPED:
            case ADP_STOPPED:
            case ARP_STOPPED:
                if (player_state != PlayerStates.Busy) {
                    player_state = PlayerStates.Busy;
                    if (toastShot != null)
                        toastShot.cancel();
                    player.Close();
                    playerStatusText.setText("Disconnected");
                    showStatusView();
                    player_state = PlayerStates.ReadyForUse;
                    setShowConnectionControls();
                    setUIDisconnected();
                }
                break;

            case CP_ERROR_DISCONNECTED:
                // if (player_state != PlayerStates.Busy)
                // {
                // player_state = PlayerStates.Busy;
                // if (toastShot != null)
                // toastShot.cancel();
                // player.Close();
                //
                // playerStatusText.setText("Disconnected");
                // showStatusView();
                // player_state = PlayerStates.ReadyForUse;
                // setUIDisconnected();
                //
                // Toast.makeText(getApplicationContext(), "Demo Version!",
                // Toast.LENGTH_SHORT).show();
                //
                // }
                break;
            default:
                player_state = PlayerStates.Busy;
            }
        }
    };

    // callback from Native Player
    @Override
    public int OnReceiveData(ByteBuffer buffer, int size, long pts) {
        Log.e(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
        return 0;
    }

    // All event are sent to event handlers
    @Override
    public int Status(int arg) {

        PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
        if (handler == null || status == null)
            return 0;

        Log.e(TAG, "Form Native Player status: " + arg);
        switch (PlayerNotifyCodes.forValue(arg)) {
        case PLP_EOS:
        case CP_ERROR_DISCONNECTED:
            // runOnUiThread(new Runnable()
            // {
            // @Override
            // public void run()
            // {
            // restartPlayer();
            // }
            // });
            // break;

        default:
            Message msg = new Message();
            msg.obj = status;
            handler.removeMessages(mOldMsg);
            mOldMsg = msg.what;
            handler.sendMessage(msg);
        }

        return 0;
    }

    public String getRecordPath()
    {
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
    		      Environment.DIRECTORY_DCIM), "RecordsMediaPlayer");
    	
	    if (! mediaStorageDir.exists()){
	        if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
	            Log.e(TAG, "<=getRecordPath() failed to create directory path="+mediaStorageDir.getPath());
	            return "";
	        }
	    }
	    return mediaStorageDir.getPath();
    }
	
	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	
	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 *
	 * @param activity
	 */
	private void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission

		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user

			ActivityCompat.requestPermissions(
					this,
					PERMISSIONS_STORAGE,this.
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
	
	//@Override
	public void onRequestPermissionsResult(int requestCode,
	        String permissions[], int[] grantResults) {
		
		Log.e(TAG, "=onRequestPermissionsResult requestCode="+requestCode+" permissions="+permissions+" grantResults="+grantResults);
	    switch (requestCode) 
	    {
	        case REQUEST_EXTERNAL_STORAGE: 
	        {
	            // If request is cancelled, the result arrays are empty.
	            if (grantResults.length > 0
	                && grantResults[0] == PackageManager.PERMISSION_GRANTED) 
	            {

	                // permission was granted, yay! Do the
	                // contacts-related task you need to do.
					edtIpAddressHistory.clear();
	                loadHistory(false);
	            }
	            else 
	            {

	                // permission denied, boo! Disable the
	                // functionality that depends on this permission.
	            }
	            return;
	        }

	        // other 'case' lines to check for other
	        // permissions this app might request
	    }
	}

    private void getListFiles2(File parentDir, ArrayList<String> fullPath) {
		if (parentDir == null || parentDir.listFiles() == null)
			return;

        Queue<File> files = new LinkedList<File>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
				if (file.getName().toLowerCase().endsWith(".ts") || file.getName().toLowerCase().endsWith(".mp4") || file.getName().toLowerCase().endsWith(".avi")
						|| file.getName().toLowerCase().endsWith(".mpeg2") || file.getName().toLowerCase().endsWith(".mov")
						|| file.getName().toLowerCase().endsWith(".flv") || file.getName().toLowerCase().endsWith(".mpeg4")
						|| file.getName().toLowerCase().endsWith(".mkv")) {
                    fullPath.add(file.getAbsolutePath());
                }
            }
        }
    }

    protected void loadHistory(boolean withSaved) {
        ArrayList<String> tempHistory = new ArrayList<String>();

        tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
        tempHistory.add("rtsp://rtsp.stream/movie");
		tempHistory.add("rtsp://rtsp.stream/pattern");

        edtIpAddressHistory.clear();
        edtIpAddressHistory.addAll(tempHistory);
        edtIpAddress.setText(tempHistory.get(0));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String strUrl;
        try {
            versionName = " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionName = versionName + "(" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ")";
        } catch (NameNotFoundException e) {
        }

        setTitle(getResources().getText(R.string.app_name) + versionName);

        Log.e(TAG, "Version: " + versionName);

        super.onCreate(savedInstanceState);
        
        verifyStoragePermissions(this);

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        setContentView(R.layout.main);
        mthis = this;

        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        SharedSettings.getInstance(this).loadPrefSettings();
        SharedSettings.getInstance().savePrefSettings();

        playerStatus = (RelativeLayout) findViewById(R.id.playerStatus);
        playerStatusText = (TextView) findViewById(R.id.playerStatusText);
        playerHwStatus = (TextView) findViewById(R.id.playerHwStatus);

        playerLayoutMulti = (RelativeLayout) findViewById(R.id.playerLayoutMulti);
        playerMultiSubtitles = (Spinner) findViewById(R.id.playerMultiSubtitles);
        playerMultiAudio = (Spinner) findViewById(R.id.playerMultiAudio);

        String multiSubtitles[] = { "Subtitle: Off" };
        spinnerArrayAdapterMultiSubtitles = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                new ArrayList<String>(Arrays.asList(multiSubtitles)));
        spinnerArrayAdapterMultiSubtitles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerMultiSubtitles.setAdapter(spinnerArrayAdapterMultiSubtitles);

        String multiAudio[] = { "AudioTrack: Off" };
        spinnerArrayAdapterMultiAudio = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                new ArrayList<String>(Arrays.asList(multiAudio)));
        spinnerArrayAdapterMultiAudio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerMultiAudio.setAdapter(spinnerArrayAdapterMultiAudio);

        // player = (MediaPlayer)findViewById(R.id.playerView);

        playerViewLayout = (LinearLayout) findViewById(R.id.playerViewLayout);

        layoutSeekBar = (RelativeLayout) findViewById(R.id.layoutSeekBar);
        layoutPlaybackBar = (RelativeLayout) findViewById(R.id.layoutPlaybackBar);
        layoutConnectionUrlBar = (LinearLayout) findViewById(R.id.layoutConnectionUrlBar);
        layoutSettingsBar = (LinearLayout) findViewById(R.id.layoutSettingsBar);
        layoutConnectionBar = (RelativeLayout) findViewById(R.id.layoutConnectionBar);

        textPlaybackPositionCurrent = (TextView) findViewById(R.id.textPlaybackPositionCurrent);
        textPlaybackPositionMax = (TextView) findViewById(R.id.textPlaybackPositionMax);
        seekPlaybackPosition = (SeekBar) findViewById(R.id.seekPlaybackPosition);
        
        seekPlaybackPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekPlaybackPositionThumbCaptured = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player == null){
                    isSeekPlaybackPositionThumbCaptured = false;
                }
                
                Position pos = player.getLiveStreamPosition();
                double seekPos = (double)seekPlaybackPosition.getProgress() / (double)seekPlaybackPosition.getMax();
                long newPos = (long)(seekPos * pos.getDuration());
                
                player.setLiveStreamPosition(newPos);
                isSeekPlaybackPositionThumbCaptured = false;
            }
        });

        //MediaPlayerConfig.setLogLevel(MediaPlayerConfig.LogLevel.ERROR);
        //MediaPlayerConfig.setLogLevelForJavaPart(MediaPlayerConfig.LogLevel.ERROR);
        //MediaPlayerConfig.setLogLevelForNativePart(MediaPlayerConfig.LogLevel.DEBUG);
        //MediaPlayerConfig.setLogLevelForMediaPart(MediaPlayerConfig.LogLevel.TRACE);

        player = new MediaPlayer(this, false);

        subtitleTextView = new TextView(this);
        subtitleTextView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        subtitleTextView.setShadowLayer(1.5f, -1, 1, Color.parseColor("#ff444444"));
        subtitleTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
        subtitleTextView.setTypeface(null, Typeface.BOLD);

        subtitleImageView = new ImageView(this);
        subtitleImageView
                .setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        barFrameLayout = new FrameLayout(this);
        barFrameLayout.addView(subtitleImageView);

        playerViewLayout.addView(subtitleTextView);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        playerViewLayout.addView(barFrameLayout, params);

        // externalView = new SurfaceView(mthis);
        // externalView.getHolder().addCallback(mthis);
        //
        // //externalView.setZOrderOnTop(true); // necessary
        //// SurfaceHolder sfhTrackHolder = externalView.getHolder();
        //// sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
        //
        // playerViewLayout.addView(externalView);
        // //externalView.setZOrderOnTop(true); // necessary

        uiSelectSubtitle(-1);
        uiSelectAudio(-1);

        player.setOnSubtitleListener(this);
        playerMultiSubtitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "playerMultiSubtitles:onItemSelected " + i + ", " + l);
                uiSelectSubtitle(i - 1);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        playerMultiAudio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "playerMultiAudio:onItemSelected " + i + ", " + l);
                uiSelectAudio(i - 1);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // player.getSurfaceView().setZOrderOnTop(true);
        strUrl = settings.getString("connectionUrl", "/sdcard/DCIM/sweep1920_60fps.mp4");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edtIpAddress = (AutoCompleteTextView) findViewById(R.id.edit_ipaddress);
        edtIpAddress.setText(strUrl);
        loadHistory(false);

        edtIpAddress.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(edtIpAddress.getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;

                }
                return false;
            }
        });

        btnHistory = (Button) findViewById(R.id.button_history);

        // Array of choices
        btnHistory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(MainActivity.edtIpAddress.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                if (edtIpAddressHistory.size() <= 0)
                    return;

                MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item,
                        edtIpAddressHistory);
                MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
                MainActivity.edtIpAddress.showDropDown();
            }
        });

        btnShot = (Button) findViewById(R.id.button_shot);

        // Array of choices
        btnShot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                     if (player.getState() == PlayerState.Started)
                         player.Pause();
                     else if (player.getState() == PlayerState.Paused)
                         player.Play();
                }
            }
        });

        buttonSynchro = (ToggleButton) findViewById(R.id.buttonSynchro);
        //buttonOmx = (ToggleButton) findViewById(R.id.buttonOmx);
        buttonDropFrames = (ToggleButton) findViewById(R.id.buttonDropFrames);

        buttonLogging = (ToggleButton) findViewById(R.id.buttonLogging);
        buttonLogging.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                    //player.enableLogging(buttonLogging.isChecked() ? LOG_LEVEL : 0);
                    MediaPlayerConfig.setLogLevelForMediaPart(buttonLogging.isChecked() ? MediaPlayerConfig.LogLevel.DEBUG : MediaPlayerConfig.LogLevel.NONE);
                }
            }
        });

        btnConnect = (Button) findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedSettings.getInstance().loadPrefSettings();
                if (player != null) {
                    if (!edtIpAddressHistory.contains(player.getConfig().getConnectionUrl()))
                        edtIpAddressHistory.add(player.getConfig().getConnectionUrl());

                    player.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
                    if (player.getConfig().getConnectionUrl().isEmpty())
                        return;

                    if (toastShot != null)
                        toastShot.cancel();

                    player.Close();

                    if (playing) {
                        externalView.getHolder().removeCallback(mthis);
                        playerViewLayout.removeView(externalView);
                        externalView = null;

                        setUIDisconnected();
                    } else {

                        externalView = new SurfaceView(mthis);
                        externalView.getHolder().addCallback(mthis);

                        playerViewLayout.addView(externalView);

                        playerViewLayout.setOnTouchListener(new OnSwipeTouchListener(mthis) {
                            public void pinchMove(boolean isGrow) {
                                Log.i(TAG, "=pinchMove grow:" + isGrow);
                                player.handleZoom(isGrow);
                            }

                            public void touchDown(int x, int y, int rawx, int rawy) {
                                Log.i(TAG, "=touchDown x:" + x + " y:" + y);
                                player.handleTouchBegin(rawx, rawy);
                            }

                            public void touchUp(int x, int y, int rawx, int rawy) {
                                Log.i(TAG, "=touchUp x:" + x + " y:" + y);
                                player.handleTouchEnd(rawx, rawy);
                            }

                            public void touchMove(int x, int y, int rawx, int rawy) {
                                Log.i(TAG, "=touchMove x:" + x + " y:" + y);
                                player.handleMoveContinue(rawx, rawy);
                            }

                        });

                        SharedSettings sett = SharedSettings.getInstance();
                        boolean bPort = (getResources()
                                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                        int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;

                        MediaPlayerConfig conf = new MediaPlayerConfig();

                        // player.setVisibility(View.INVISIBLE);

                        conf.setConnectionUrl(player.getConfig().getConnectionUrl());
                        conf.setContentProviderLibrary(0);
                        conf.setConnectionNetworkProtocol(-1/*-1*/);
                        conf.setConnectionBufferingType(2000);
                        conf.setSendKeepAlive(0);
                        // conf.setConnectionNetworkProtocol(-1);
                        // conf.setConnectionDetectionTime(sett.connectionDetectionTime);
                        // conf.setConnectionBufferingTime(sett.connectionBufferingTime);
                        // conf.setConnectionBufferingType(1);
                        // conf.setConnectionDetectionTime(1000);
                        // conf.setConnectionBufferingTime(5000);
                        // conf.setConnectionBufferingSize(5 * 1024 * 1024);
                        // conf.setDecodingType(sett.decoderType);

                        conf.setSynchroEnable(buttonSynchro.isChecked() ? 1 : 0);
                        conf.setDecodingType(/*buttonOmx.isChecked() ? 2 : */sett.decoderType);
                        conf.setRendererType(/*buttonOmx.isChecked() ? 1 : */sett.rendererType);

                        conf.setSynchroNeedDropVideoFrames(buttonDropFrames.isChecked() ? 1 : 0);
                        conf.setEnableColorVideo(sett.rendererEnableColorVideo);
                        conf.setEnableAspectRatio(aspect);
                        conf.setDataReceiveTimeout(30000);
                        conf.setNumberOfCPUCores(0);

                        conf.setFadeOnStart(0);

                        final boolean USE_SEGMENT_LIST = false;
                        //=>USE_SEGMENT_LIST
                        if(USE_SEGMENT_LIST){
                            conf.setContentProviderLibrary(1); //turn on rtsplib


                            //load segments
                            int num=1;
                            for(int i=0; i<4; i++){
                                PlaySegment seg = new PlaySegment();
                                seg.setId(i);
                                seg.setName("Seg="+num);
                                seg.setUrl(player.getConfig().getConnectionUrl());
                                //seg.setStartTime(num*60*1000);
                                seg.setStopTime(0);
                                //seg.setDurationTime(10*1000); //play 10 sec
                                seg.setStartOffset(0);
                                conf.connectionSegments.add(seg);
                                num++;
                            }
                        }//<=USE_SEGMENT_LIST

                        // Open Player
                        conf.setEnableABR(0);

                        // force zoom
                        conf.setAspectRatioMode(4); // Zoom and move mode

                        if (surface != null) {
                            Log.e(TAG, "setSurface " + surface);
                            player.setSurface(externalView, surface);
                        }

                        int callbackDataMask = MediaPlayer.PlayerCallbackDataMask.forType(MediaPlayer.PlayerCallbackDataMask.PP_CALLBACK_DATA_RENDERER_VIDEO_DATA);

                        player.setOnDataListener(mthis, callbackDataMask);

                        player.Open(conf, mthis);

                        buttonSynchro.setEnabled(false);
                        // buttonLogging.setEnabled(false);
                        //buttonOmx.setEnabled(false);
                        buttonDropFrames.setEnabled(false);

                        btnConnect.setText("Disconnect");

                        playing = true;

                        if (autoClose) {
                            autoCloseTimer = new Timer();
                            autoCloseTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    });
                                }

                            }, autoCloseAfterDelay);
                        }

                    }
                }
            }
        });

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (getWindow() != null && getWindow().getCurrentFocus() != null
                        && getWindow().getCurrentFocus().getWindowToken() != null)
                    inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });

        playerStatusText.setText("DEMO VERSION");
        setShowConnectionControls();

        // UdpxyService.startUdpxy(this);

        // String uriString = UdpxyService.proxify("239.0.0.1:1234");
        // Log.e(TAG, "uriString " + uriString);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("autoStartUrl")) {
                autoStartUrl = extras.getString("autoStartUrl");
            }

            if (extras.containsKey("autoStart")) {
                autoStart = Boolean.parseBoolean(extras.getString("autoStart"));
            }

            if (extras.containsKey("autoClose")) {
                autoClose = Boolean.parseBoolean(extras.getString("autoClose"));
            }

            if (extras.containsKey("autoEnableOMX")) {
                autoEnableOMX = Boolean.parseBoolean(extras.getString("autoEnableOMX"));
            }

            if (extras.containsKey("autoEnableSynchro")) {
                autoEnableSynchro = Boolean.parseBoolean(extras.getString("autoEnableSynchro"));
            }

            if (extras.containsKey("autoEnableSynchroDrop")) {
                autoEnableSynchroDrop = Boolean.parseBoolean(extras.getString("autoEnableSynchroDrop"));
            }

        }

        if (autoStart) {
            autoStartTimer = new Timer();
            autoStartTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (autoStartUrl != null && !autoStartUrl.isEmpty())
                                edtIpAddress.setText(autoStartUrl);

                            //buttonOmx.setChecked(autoEnableOMX);
                            buttonSynchro.setChecked(autoEnableSynchro);
                            buttonDropFrames.setChecked(autoEnableSynchroDrop);

                            btnConnect.performClick();
                        }
                    });
                }

            }, autoStartDelay);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("closeApp")) {
                finish();
            }
        }

    }

    private int[] mColorSwapBuf = null; // used by saveFrame()

    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }

    protected void onPause() {
        Log.e("SDL", "onPause()");
        super.onPause();

        editor = settings.edit();
        editor.putString("connectionUrl", edtIpAddress.getText().toString());

        editor.commit();

        if (player != null)
            player.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("SDL", "onResume()");
        super.onResume();
        if (player != null)
            player.onResume();
    }

    @Override
    protected void onStart() {
        Log.e("SDL", "onStart()");
        super.onStart();
        if (player != null)
            player.onStart();
    }

    @Override
    protected void onStop() {
        Log.e("SDL", "onStop()");
        super.onStop();
        if (player != null)
            player.onStop();

        if (toastShot != null)
            toastShot.cancel();

    }

    @Override
    public void onBackPressed() {
        if (progress != null) {
            Log.e(TAG, "dismiss progress");
            progress.dismiss();
        }

        if (toastShot != null)
            toastShot.cancel();

        if (!playing) {
            player.Close();
            super.onBackPressed();
            return;
        }

        btnConnect.callOnClick();
//        setUIDisconnected();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.e("SDL", "onWindowFocusChanged(): " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (player != null)
            player.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onLowMemory() {
        Log.e("SDL", "onLowMemory()");
        super.onLowMemory();
        if (player != null)
            player.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        Log.e("SDL", "onDestroy()");

        // UdpxyService.stopUdpxy(this);

        if (toastShot != null)
            toastShot.cancel();

        if (player != null)
            player.onDestroy();

        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.main_opt_settings:

            SharedSettings.getInstance().loadPrefSettings();

            Intent intentSettings = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivity(intentSettings);

            break;
        case R.id.main_opt_clearhistory:

            new AlertDialog.Builder(this).setTitle("Clear History")
                    .setMessage("Do you really want to delete the history?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            edtIpAddressHistory.clear();
                            loadHistory(false);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
            break;
        case R.id.main_opt_exit:
            finish();
            break;

        }
        return true;
    }


    private Map<Drawable, Integer> saveDrawables = new HashMap<Drawable, Integer>();

    public Drawable getDrawableRect() {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(Color.TRANSPARENT);
        gd.setStroke(5, Color.WHITE);
        return gd;
    }

    // tests for draw over video functionality
    public void testDrawRects(int width, int height) {

        if (width <= 0) width = 150;
        if (height <= 0) height = 150;

        int row = 5; int column = 5;
        int startx = 10; int spacex = 10; int objwidth = ((width / row) - spacex);
        int starty = 10; int spacey = 10; int objheight = ((height / column) - spacey);

        int curx = startx;
        int cury = starty;
        for (int i = 1; i < (row * column); i++) {
            Drawable draw = player.drawAddObjectOverVideo(getDrawableRect(), new Rect(curx, cury, curx + objwidth, cury + objheight));
            if (draw != null) {
                saveDrawables.put(draw, 0);
            }
            if ((i % row) == 0) {
                curx = startx;
                cury = cury + objheight + spacey;
                continue;
            }
            curx = curx + objwidth + spacex;
        }

        //player.drawObjectOverVideo(getDrawableRect(), new Rect(5, 5, 100, 100));

        Drawable draw = player.drawAddObjectOverVideo(getDrawableRect(), new Rect(900, 900, 1880, 1000));
        if (draw != null) {
            saveDrawables.put(draw, 0);
        }
    }

    public void testClearRectsByOne() {
        for (Map.Entry<Drawable, Integer> entry : saveDrawables.entrySet()) {
            player.drawRemoveObjectFromVideo(entry.getKey());
            saveDrawables.remove(entry.getKey());
            break;
        }
    }


    protected void setUIDisconnected() {
        setTitle(getResources().getText(R.string.app_name) + versionName);
        btnConnect.setText("Connect");
        playing = false;
        saveDrawables.clear();

        buttonSynchro.setEnabled(true);
        //buttonOmx.setEnabled(true);
        buttonDropFrames.setEnabled(true);
        // buttonLogging.setEnabled(true);

    }

    protected void setShowPlaybackControls() {
        // btnShot.setVisibility(View.VISIBLE);
        // edtIpAddress.setVisibility(View.GONE);
        // btnHistory.setVisibility(View.GONE);
        // btnConnect.setEnabled(false);

        layoutSeekBar.setVisibility(View.VISIBLE);
        layoutPlaybackBar.setVisibility(View.VISIBLE);
        layoutConnectionUrlBar.setVisibility(View.GONE);
        layoutSettingsBar.setVisibility(View.GONE);
        layoutConnectionBar.setVisibility(View.GONE);
    }

    protected void setShowConnectionControls() {
        
        updateLayoutSeekbar(false);
        
        setTitle(getResources().getText(R.string.app_name) + versionName);

        layoutSeekBar.setVisibility(View.GONE);
        layoutPlaybackBar.setVisibility(View.GONE);
        layoutConnectionUrlBar.setVisibility(View.VISIBLE);
        layoutSettingsBar.setVisibility(View.VISIBLE);
        layoutConnectionBar.setVisibility(View.VISIBLE);
        
        // btnShot.setVisibility(View.GONE);
        // edtIpAddress.setVisibility(View.VISIBLE);
        // btnHistory.setVisibility(View.VISIBLE);
        // btnConnect.setEnabled(true);
    }

    private void showStatusView() {
        // player.setVisibility(View.INVISIBLE);
        playerHwStatus.setVisibility(View.INVISIBLE);
        // player.setAlpha(0.0f);
        playerStatus.setVisibility(View.VISIBLE);
    }

    private void showVideoView() {
        playerStatus.setVisibility(View.INVISIBLE);
        // player.setVisibility(View.VISIBLE);
        playerHwStatus.setVisibility(View.VISIBLE);

        // SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
        // sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        setTitle("");
    }

    private void uiSelectSubtitle(int index) {
        Log.e(TAG, "uiSelectSubtitle: " + index);
        if (index == -1) {
            subtitleImageView.post(new Runnable() {
                @Override
                public void run() {
                    subtitleTextView.setVisibility(View.GONE);
                    subtitleImageView.setVisibility(View.GONE);
                    subtitleImageView.setImageBitmap(null);
                    subtitleImageView.destroyDrawingCache();
                }
            });
        }

        if (player == null)
            return;

        player.SubtitleSelect(index);
    }

    private void uiSelectAudio(int index) {
        Log.e(TAG, "uiSelectAudio: " + index);
        if (index == -1) {
            // subtitleImageView.post(new Runnable()
            // {
            // @Override
            // public void run()
            // {
            // subtitleTextView.setVisibility(View.GONE);
            // subtitleImageView.setVisibility(View.GONE);
            // subtitleImageView.setImageBitmap(null);
            // subtitleImageView.destroyDrawingCache();
            // }
            // });
        }

        if (player == null)
            return;

        player.AudioSelect(index);
    }

    protected void updateLayoutSeekbar(boolean start) {
        if (!start || player == null)
        {
            textPlaybackPositionCurrent.setText("00:00:00");
            textPlaybackPositionMax.setText("00:00:00");
            seekPlaybackPosition.setProgress(0);
            
            if (timerPlayerPosition != null){
                timerPlayerPosition.cancel();
                timerPlayerPosition = null;
            }

            return;
        }

        if (player == null)
            return;

        Position pos = player.getLiveStreamPosition();

        textPlaybackPositionCurrent.setText(valueToTimeString(pos.getCurrent() / 1000));
        textPlaybackPositionMax.setText(valueToTimeString(pos.getDuration() / 1000));

        if (timerPlayerPosition == null){
            timerPlayerPosition = new Timer();
            timerPlayerPosition.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (player == null)
                                return;
                            
                            Position pos = player.getLiveStreamPosition();
                            if (pos == null)
                                return;
                            
                            textPlaybackPositionCurrent.setText(valueToTimeString(pos.getCurrent() / 1000));
                            if (isSeekPlaybackPositionThumbCaptured)
                                return;
                            
                            double newPos = (double)pos.getCurrent() / pos.getDuration();
                            int seekPos = (int)(newPos * seekPlaybackPosition.getMax());
                            
                            seekPlaybackPosition.setProgress(seekPos);
                        }
                    });
                    
                }

            }, 1000, 1000);
        }
    }
    
    protected String valueToTimeString(long value) {
        long s = value;
        long m = s / 60;
        long h = m / 60;
        
        s = s % 60;
        m = m % 60;
        
        return "" + h +":" + m + ":" + s;
    }
    
    private void updateLayoutMulti() {
        if (player == null)
            return;

        int subtitleCount = player.SubtitleGetCount();
        int subtitleSelected = player.SubtitleGetSelected();
        Log.e(TAG, "updateLayoutMulti: subtitle count: " + subtitleCount + ", selected: " + subtitleSelected);

        spinnerArrayAdapterMultiSubtitles.clear();
        spinnerArrayAdapterMultiSubtitles.add("Subtitle: Off");
        for (int i = 0; i < subtitleCount; i++) {
            spinnerArrayAdapterMultiSubtitles.add("Subtitle: " + (i + 1));
        }

        playerMultiSubtitles
                .setSelection((subtitleSelected >= 0 && subtitleSelected < subtitleCount) ? (subtitleSelected + 1) : 0);

        int audioCount = player.AudioGetCount();
        int audioSelected = player.AudioGetSelected();
        Log.e(TAG, "updateLayoutMulti: audio count: " + audioCount + ", selected: " + audioSelected);

        spinnerArrayAdapterMultiAudio.clear();
        spinnerArrayAdapterMultiAudio.add("Audio: Off");
        for (int i = 0; i < audioCount; i++) {
            spinnerArrayAdapterMultiAudio.add("Audio: " + (i + 1));
        }

        playerMultiAudio.setSelection((audioSelected >= 0 && audioSelected < audioCount) ? (audioSelected + 1) : 0);
    }

    private Runnable runnableInformerHide = new Runnable() {
        @Override
        public void run() {
            subtitleTextView.setText("");
            subtitleTextView.setVisibility(View.INVISIBLE);
        }
    };

    private Runnable runnableInformerHide_image = new Runnable() {
        @Override
        public void run() {
            subtitleImageView.setVisibility(View.INVISIBLE);
            subtitleImageView.setImageBitmap(null);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        surface = holder.getSurface();
        if (surface != null && player != null) {
            Log.e(TAG, "setSurface " + surface);

            player.setSurface(externalView, surface);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        if (player != null) {
            player.setSurface(null);
        }
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
    }

    // @Override
    public int OnDecryptData(ByteBuffer buffer, int size, int payload) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int OnReceiveSubtitleString(String data, long duration) {
        Log.e(TAG, "OnReceiveSubtitleString: " + data + ", duration " + duration);

        final String text = data;
        subtitleTextView.removeCallbacks(runnableInformerHide);
        subtitleTextView.postDelayed(runnableInformerHide, duration);
        subtitleTextView.post(new Runnable() {
            @Override
            public void run() {
                subtitleTextView.setVisibility(View.VISIBLE);
                subtitleTextView.setSingleLine(true);
                subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                subtitleTextView.setTypeface(null, Typeface.NORMAL);
                subtitleTextView.setBackgroundColor(0x90000000);
                subtitleTextView.setText(text);
            }
        });

        return -1;
    }

    @Override
    public int OnReceiveSubtitleByteBuffer(ByteBuffer buffer, int size, long pts, long duration) {
        Log.e(TAG, "OnReceiveSubtitleByteBuffer: " + size);
        return -1;
    }

    @Override
    public int OnReceiveSubtitleBitmap(ByteBuffer buffer, int size, int left, int top, int width, int height,
            int video_width, int video_height, long pts, long duration) {
        Log.e(TAG, "OnReceiveSubtitleBitmap: " + size);

        if (width > video_width) {
            video_width = width;
        }

        if (height + top > video_height) {
            video_height = height + top;
        }

        final Bitmap bm = Bitmap.createBitmap(video_width, video_height, Bitmap.Config.ARGB_8888);
        int[] result = new int[width * height];
        buffer.rewind();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 0) {
            result[buffer.position() / 4] = buffer.getInt();
        }

        try {
            if (left >= 0 && top >= 0) {
                if (left + width > video_width)
                    left = video_width - width;

                bm.setPixels(result, 0, width, left, top, width, height);
            }

            subtitleImageView.removeCallbacks(runnableInformerHide_image);
            subtitleImageView.postDelayed(runnableInformerHide_image, duration);
            subtitleImageView.post(new Runnable() {
                @Override
                public void run() {
                    subtitleImageView.setImageBitmap(bm);
                    subtitleImageView.setVisibility(View.VISIBLE);
                }
            });
        } catch (IllegalStateException e) {
            Log.e(TAG, "OnReceiveSubtitle IllegalStateException1 " + e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "OnReceiveSubtitle IllegalArgumentException2 " + e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "OnReceiveSubtitle IllegalArgumentException3 " + e);
        }

        return -1;
    }

    public int OnReceiveSubtitleClear() {
        Log.e(TAG, "OnReceiveSubtitleClear DVB");

        subtitleImageView.post(new Runnable() {
            @Override
            public void run() {
                subtitleImageView.setVisibility(View.GONE);
                subtitleImageView.setImageBitmap(null);
                subtitleImageView.destroyDrawingCache();
            }
        });

        return -1;
    }

    @Override
    public int OnVideoSourceFrameAvailable(ByteBuffer buffer, int size,
                                           long pts, long dts, int stream_index, int format) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public int OnAudioSourceFrameAvailable(ByteBuffer buffer, int size,
                                           long pts, long dts, int stream_index, int format) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int OnVideoRendererFrameAvailable(ByteBuffer buffer, int size, String format_fourcc, int width, int height,
                                             int bytes_per_row, long pts, int will_show) {

        return 0;
    }

    @Override
    public int OnAudioRendererFormat(int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
        return 0;
    };

    @Override
    public int OnAudioRendererFrameAvailable(ByteBuffer buffer, int size, long pts) {
        return 0;
    };

}
