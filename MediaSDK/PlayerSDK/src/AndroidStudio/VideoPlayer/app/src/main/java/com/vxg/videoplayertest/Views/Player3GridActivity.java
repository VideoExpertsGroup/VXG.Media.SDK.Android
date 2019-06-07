package com.vxg.videoplayertest.Views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.vxg.videoplayertest.Models.SharedSettings;
import com.vxg.videoplayertest.R;
import com.vxg.videoplayertest.Views.Controls.player.VideoPlayer;


import com.vxg.cloudsdk.CloudPlayerSDK;
import com.vxg.ui.CloudMultiPlayerView;



public class Player3GridActivity extends Activity {
	private final String TAG = Player3GridActivity.class.getSimpleName();
    

    private EditText urlInput;	
	private FrameLayout submitUrl;

    private SharedSettings sett;

	
    // Players GRIDs
    private CloudMultiPlayerView ctlPlayer;

    private int mSelectedCell = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_player3_grid);
		
        sett = SharedSettings.getInstance(this);
        initViews();
        setListeners();

		sett.loadPrefSettings();
		urlInput.setText(sett.URL);		
    }

    private void initViews() {
        
        ctlPlayer 	= findViewById(R.id.player_multi_view);
        ctlPlayer.setOnCloudMultiPlayerViewChange(initViewChangeListener());

		submitUrl 		= findViewById(R.id.submitUrl);
        urlInput 		= findViewById(R.id.urlInput);
		
    }

    private void setListeners() {

		submitUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Set URL in preferred settings	
				sett.URL = urlInput.getText().toString();
				sett.savePrefSettings();

				ctlPlayer.insertChannel(sett.URL, 0);
				ctlPlayer.insertChannel(sett.URL, 1);
				ctlPlayer.insertChannel(sett.URL, 2);
				ctlPlayer.insertChannel(sett.URL, 3);
			}
		});
	}

    private CloudMultiPlayerView.OnCloudMultiPlayerViewChange initViewChangeListener(){
        return new CloudMultiPlayerView.OnCloudMultiPlayerViewChange(){
            @Override
            public void onPlayed(int pos) {
                Log.i(TAG, "=onPlayed pos="+pos);
            }

            @Override
            public void onPaused(int pos) {
                Log.i(TAG, "=onPaused pos="+pos);
            }

            @Override
            public void onEOS(int pos) {
                Log.i(TAG, "=onEOS pos="+pos);
            }

            @Override
            public void onOutOfRange(int pos) {
                Log.i(TAG, "=onOutOfRange="+pos);
            }

            @Override
            public void onError(int pos, int error) {
                Log.e(TAG, "=onError="+pos+", error="+error);
            }

            @Override
            public void onClosed(int pos) {
                Log.e(TAG, "=onClosed="+pos);
            }

            @Override
            public void onItemClick(View view, int pos) {
                Log.i(TAG, "=onItemClick pos="+pos);
                mSelectedCell = pos;

                CloudPlayerSDK playerSDK = ctlPlayer.getPlayer(pos);
                if(playerSDK == null || playerSDK.getSource() == null || playerSDK.getSource().length() < 1)
                    {
						String access_token = "";
						switch (pos)
						{
							case 0:
								access_token = sett.URL;
							break;
							case 1:
								access_token = sett.URL;
								break;
							case 2:
								access_token = sett.URL;
								break;
							case 3:
								access_token = sett.URL;
								break;
							default:
								break;
						}
						ctlPlayer.insertChannel(access_token, pos);
		            }
		            else
		            {
		                    ctlPlayer.removeChannel(pos);
		            }


            }
        };
    }




}
