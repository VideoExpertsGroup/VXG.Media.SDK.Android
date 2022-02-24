# Overview

VXG Mobile SDK structure:

    |-- VXGMobileSDK.Android
        |-- MediaSDK
            |-- PlayerSDK
            |-- EncoderSDK
        |-- CloudSDK
            

**Cloud SDK** is a part of **VXG Mobile SDK** responsible for streaming and playback with *VXG Video Server* or *VXG Cloud Video*.

*VXG Video Server* is a software NVR, streaming server and a lightweight VMS that you can run on the edge, on 
premises or on a cloud instance.   

*VXG Cloud Video* is auto scaling verion of the VXG Server running on the cloud.


## Class CloudPlayerSDK
A class for live and recorded video playback from VXG Server or VXG Cloud. 

IMPORTANT: Access token from the Dashboard should be used here

## SDK structure
The SDK package consists of the following files and folders:

* aar   (Aar archive) 
* docs	(Files with links to documentation)
* src 	(Sample applications source code)

#### Key features

* Playback of live streams
* Playback of recorded videos
* Video controls: Play/Pause
* Control of position for recorded video
* Control of audio output: mute, unmute, volume control
* Control of timeline for recorded video (Show/Hide)

#### public CloudPlayerSDK(View view, CloudPlayerConfig config, ICloudPlayerCallback callback)

**Description**

Create new instance of player

**Parameters**

view - view control to present video.

config - player options. All options are described in CloudPlayerConfig

callback - callback function where all notifications will be received.

**Remarks**

Add callback to get player's notifications. All notifications are described in CloudPlayerEvents.
There can be one or more ICloudPlayerCallbacks in one application.

**Return values**

New player object is returned

**Example**

    player_view = (FrameLayout) findViewById(R.id.playerView);
    player = new CloudPlayerSDK(player_view, new CloudPlayerConfig(), new ICloudPlayerCallback(){
    	@Override
    	public void onStatus(final CloudPlayerEvent player_event, final ICloudObject p) {
    		runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				switch(player_event){
    					case SOURCE_CHANGED:
    						break;
    					case CONNECTING:
    						break;
    					case CONNECTED:
    					case STARTED:
    					case PAUSED:
    						break;
    					case CLOSED:
    						break;
    					case EOS:
    						break;
    					case SEEK_COMPLETED:
    						break;
    					case ERROR:
    						break;
    					case TRIAL_VERSION:
    						break;
    						}
    					}
    				});
    			}
    		});

#### int setSource(String access_token)

**Description**

Set access token to view channel.
Access_token is defined in the Dashboard after channel is created.

**Parameters**

access_token - access token for channel playback.

**Return values**

No return value

**Example**

    player_view = (FrameLayout) findViewById(R.id.playerView);
    player = new CloudPlayerSDK(player_view, new CloudPlayerConfig(), new ICloudPlayerCallback(){
    	@Override
    	public void onStatus(final CloudPlayerEvent player_event, final ICloudObject p) {
    		runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				switch(player_event){
    					case SOURCE_CHANGED:
    						break;
    					case CONNECTING:
    						break;
    					case CONNECTED:
    					case STARTED:
    					case PAUSED:
    						break;
    					case CLOSED:
    						break;
    					case EOS:
    						break;
    					case SEEK_COMPLETED:
    						break;
    					case ERROR:
    						break;
    					case TRIAL_VERSION:
    						break;
    						}
    					}
    				});
    			}
    		});
            
    String access_token = "eyJ0b2tlbiI6InNoYXJlLmV5SnphU0k2SURFNE0zMC41YTQwYzQ2NXQxMmNmZjc4MC5rNlIxWHdjX2ptUjRZSFU5QV9xSVFHc2liX2MiLCJjYW1pZCI6MTMxMDY0LCJhY2Nlc3MiOiJ3YXRjaCJ9";
    player.setSource(access_token);
    player.play();

#### void addCallback(ICloudPlayerCallback callback)

**Description**

Add callback to get player's notification. All notifications are described in CloudPlayerEvents.
There can be one or more ICloudPlayerCallbacks in one application.

**Parameters**

callback - callback function where all notifications will be received.

**Return values**

No return value

**Example**

    // "this" is an object that implements ICloudPlayerCallback interface 
    mPlayer.addCallback(this); 

#### void removeCallback(ICloudPlayerCallback callback)

**Description**

Remove callback to get player's notification.

**Parameters**

callback - callback function where all notifications will be received by application.

**Return values**

No return value

**Example**

    // "this" is an object that implements ICloudPlayerCallback interface 
    mPlayer.removeCallback(this); 

#### int setConfig(CloudPlayerConfig config)

**Description**

Set various settings for player in player config. All settings are described in CloudPlayerConfig

**Parameters**

No input parameters

**Return values**

Upon successful completion, setConfig() returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

      CloudPlayerConfig config;
      config.m_visibleControls(false);
      config.m_aspectRatio(0);
      
       // Set config for m_aspectRatio
       player.setConfig(config);

#### CloudPlayerConfig getCloneConfig()

**Description**

Get current player config.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getCloneConfig() returns CloudPlayerConfig, otherwise null value is returned.

**Example**

    CloudPlayerConfig mPlayerConfig;
    mPlayerConfig = player.getCloneConfig(); 
     if (mPlayerConfig.m_minLatency  < 10000)
       // return message here

#### void play()

**Description**

Resume play if player is in Pause state.

**Parameters**

No input parameters

**Return values**

No return values

**Example**

    mPlayer.play();

#### void pause()

**Description**

Change playback state from Play to Pause.

**Parameters**

No input parameters

**Return values**

No return values

**Example**
    
    mPlayer.pause();

#### void close()

**Description**

Close player object and free all resources.

**Parameters**

No input parameters

**Return values**

No return value

**Example**

    mPlayer.close();

#### CloudPlayerState getState()

**Description**

Return player state. Player's states are described in CloudPlayerState.

**Parameters**

No input parameters

**Return values**

getState() returns CloudPlayerState.

**Example**

    CloudPlayerState mState = mPlayer.getState();
    if (mState == CloudPlayerState.PLAYED)
     // Player started and played video successfully. 

#### void setPosition(long nPosition)

**Description**

Set position for recorded playback. This function can be used go to live streaming if nPosition is set as CloudPlayer.POSITION_LIVE.

Following notifications can be sent in status callback after setPosition:

CloudPlayerEvent.SEEK_COMPLETED - position is changed successfully

CloudPlayerEvent.EOS - end of stream is reached.

**Parameters**

nPosition - position in video storage. Position is set in Unix time, UTC, milliseconds.

**Return values**

No return value

**Example**

    // Go to live streaming
    mPlayer.setPosition(CloudPlayer.POSITION_LIVE);
    
    // Playback video Tue, 17 Oct 2017 00:00:00 GMT
    mPlayer.setPosition(1508198400000);


#### long getPosition()

**Description**

Get current position. This function returns value for both modes: live video streaming and recorded video playback.

**Parameters**

No input parameters

**Return values**


Upon successful completion, getPosition() returns position, otherwise negative value is returned. All errors are described in section Errors.
Position is returned in Unix time format, UTC time, milliseconds.

**Example**

    // Get current position 
    mPosition = mPlayer.getPosition();

#### boolean isLive()

**Description**

Tells if live or recorded video is played at the moment.

**Parameters**

No input parameters

**Return values**

isLive() returns true if live video is played, otherwise false value is returned.

**Example**

    b_cam_live = mPlayer.isLive();
    if (true == b_cam_live)
    	// Live streaming is played at the moment

#### void mute(boolean bMute)

**Description**

Set mute / unmute on audio render.

**Parameters**

bMute - Mute sound if value is true, and unmute sound if value is false.

**Return values**

No return value

**Example**

    // Mute sound on audio outptut
    boolean b_mute = true;
    mPlayer.mute(b_mute);
    
    // Unmute sound on audio outptut
    boolean b_mute = flase;
    mPlayer.mute(b_mute);

#### boolean isMute()

**Description**

Return current status of audio output.

**Parameters**

No input parameters

**Return values**

isMute() returns true if audio output is muted, otherwise false if audio output is unmuted.

**Example**

    boolean b_mute_audio =  mPlayer.isMute();
    if (true == b_mute_audio)
       // Audio is muted in player

#### void setVolume(int val)

**Description**

Set volume for audio output of defined player.

**Parameters**

val - volume level. 0 is min level; 100 - max level.

**Return values**

No return value

**Example**

    // Set middle audio level for audio output
    mPlayer.setVolume(50);

#### int getVolume()

**Description**

Return audio level.

**Parameters**

No input parameters

**Return values**

getVolume() returns audio level. Level is changed from min level 0 to max level 100.

**Example**

    // Get current audio level for defined player
    int m_audio_volume = mPlayer.getVolume();

#### void showTimeline(View vwTimeline)

**Description**

Show timeline in defined view.
Timeline shows all recorded segments or dates when there is recorded data.

**Parameters**

vwTimeline - view that is used to show timeline control. This control can be customized or reused in application.

**Return values**

No return value

**Example**

    // Show timeline  
    mPlayer.showTimeline(View);

#### void hideTimeline()

**Description**

Hide timeline. Timeline shows all recorded segments or dates when there is recorded data.

**Parameters**

No input parameters

**Return values**

No return values

**Example**

      // Hide timeline
        mPlayer.hideTimeline();

#### long getID();



**Description**

Return source ID. The ID is assigned to every camera that is created on VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getID() returns camera ID, otherwise negative value (-1) is returned.

**Example**

    long cam_id = mPlayer.getID();
    if(cam_id == -1){
      //camera connection error
      Log.e("Error=" + mPlayer.getResultInt() + " "+mPlayer.getResultStr());
    }

#### String getPreviewURLSync();


**Description**

Get URL to download preview image. This function is called synchronously.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getPreviewURLSync() returns URL, otherwise null is returned.

**Example**

    String URL = mPlayer.getPreviewURLSync();
    if (null != URL)
     // Download image 

#### int getPreviewURL(ICompletionCallback callback)


**Description**

Get URL. URL can be used to download camera preview in jpg format. This function is called asynchronously.

**Parameters**

ICompletionCallback - it is called if correct URL is received from the server or in the case of error.

**Return values**

Upon successful completion, getPreviewURL returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

    int err = mPlayer.getPreviewURLSync(new ICompletionCallback(){
                @Override
                public int onComplete(Object o_result, final int result) {
    
                    if(result == 0) {
                                   String URL = (String)o_result;
                                   // download image here
                    }else {
                                            //FAIL
                    }
                    return 0;
                }
            }););

#### long getTimeLive();


**Description**

Get the time when camera will be deleted on server. Until this time camera will be available.
It is useful when the video from camera will be viewed only once and you don't want to keep it on server after the player is closed.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getTimeLive() returns time, otherwise 0 value is returned.

Important note: time is set in UTC. Time format is Unix time in milliseconds.

**Example**

    long t = mPlayer.getTimeLive();


#### String getTimeZone()

**Description**

Get a time zone. Time zone is used further to set timeline, calendar, live time and other parameters.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getTimeZone() returns time zone, otherwise null value is returned.
time_zone - time zone of the camera. Complete list of supported time zones: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

**Example**

    String time_zone = mPlayer.getTimeZone();

#### String getName();

**Description**

Get a camera name.
Use refresh() or refreshSync() to update the name from VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getName() returns camera name, otherwise null value is returned if camera name is not set.

**Example**

    String camera_name = mPlayer.getName();

#### CloudCameraStatus getStatus();

**Description**

Return camera status.
Use refresh() or refreshSync() to update the value from VXG server.

There are following camera statuses:

(0) ACTIVE - Camera is live and works properly

(1) UNAUTHORIZED - Camera is not authorized

(2) INACTIVE - Camera is switched off

(3) INACTIVE_BY_SCHEDULER - Camera is switched off by scheduler

(4) OFFLINE - Camera is offline

**Parameters**

No input parameters

**Return values**

Upon successful completion, getStatus() returns CloudCameraStatus object, otherwise null value is returned.

**Example**

    mPlayer.refreshSync(); //sync with cloud
    CloudCameraStatus camera_status = mPlayer.getStatus();
    if (camera_status == CloudCameraStatus.ACTIVE)
      //Camera is online

#### CloudCameraRecordingMode getRecordingMode();


**Description**

Get recording state of camera. 

There is limitation for media storage on server if trial key is used - recorded data is stored for 72 hours after the moment of recording.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

CloudCameraRecordingMode - recording state

There are following recording states:

(0) CONTINUES - Continuous recording. Media data is recorded uninterruptedly in 24/7 mode.

(1) BY_EVENT - Record media data by event. Current option is supported by push camera only.

(2) NO_RECORDING - No recording or it can be used to stop recording.

**Example**

    mPlayer.refreshSync();
    CloudCameraRecordingMode mRecMode = mPlayer.getRecordingMode();
    if (mRecMode == CloudCameraRecordingMode.CONTINUES)
       // Recording is ON
    else if (mRecMode == CloudCameraRecordingMode.NO_RECORDING)
       // Recording is OFF

#### boolean isRecording()


**Description**

Get the current recording state of the camera.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

isRecording() returns true if recording is running, returns false if recording does not work for some reason.

**Example**

    boolen recording = mPlayer.isRecording();
       if (true == recording )
          // Camera recording works right now

#### CloudTimeLine getTimelineSync(long start, long end);


**Description**

Get information about recorded data on server in defined range. Range is set in start and end values. This function is called synchronously.

Records are provided in following structure:

    public class CloudTimeline {
    public long start; //requested time start
    public long end; //requested time end
    public List<Pair<Long, Long>> periods;
    }

where:

start - start time of range where there are actual records

end - end time of range where there are actual records

periods - start and end time of every record.

All times are defined in Unix time (milliseconds).

**Parameters**

start - start time of range. It is set in milliseconds Unix time.
end - end time of range. It is set in milliseconds Unix time.

**Return values**

Upon successful completion, getTimelineSync() returns object CloudTimeLine, otherwise null value is returned. Null means there are no records for defined period.

**Example**

    //get record for latest day
      long time_start = get_time_cur_ms()-24*3600*1000;
      long time_end = get_time_cur_ms();
      CloudTimeline timeline = mPlayer.getTimelineSync(time_start, time_end);

#### int getTimeline(long start, long end, ICompletionCallback callback);


**Description**

Get information about recorded data on server in defined range. Range is set in start and end values. This function is called asynchronously.

Records are provided in following structure in callback function.

	public class CloudTimeline {
	public long start; //requested time start
	public long end; //requested time end
	public List<Pair<Long, Long>> periods;
	}

where:

start - start time of range where there are actual records

end - end time of range where there are actual records

periods - start and end time of every record.

All times are defined in Unix time (milliseconds).

**Parameters**

start - start time of range. It is set in milliseconds Unix time.

end - end time of range. It is set in milliseconds Unix time.

callback - callback is called if correct response is received from server or in case error.

**Return values**

Upon successful completion, getTimeline() returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

    //get record for latest day in async callback
      long time_start = get_time_cur_ms()-24*3600*1000;
      long time_end = get_time_cur_ms();
      int err =  mPlayer.getTimeline(time_start, time_end,new ICompletionCallback(){
                @Override
                public int onComplete(Object o_result, final int result) {
    
                    if(result == 0) {
                                   CloudTimeline TimeLine = (CloudTimeline)o_result;
                                   // Handle CloudTimeLine 
                    }else {
                                            //FAIL
                    }
                    return 0;
                }
            }););
            
#### ArrayList<Long> getTimelineDaysSync(boolean use_timezone)


**Description**

Get information about recorded days considering or not considering the timezone. This function is called synchronously.
Records are provided in array of Long time values since 1.1.1970.

**Parameters**

boolean use_timezone. True - return Long time values considering camera timezone. False - Long time values are in UTC.

**Return values**

Upon successful completion, getTimelineDaysSync() returns object ArrayList, otherwise null value is returned. Empty array means that there are no records for defined period.

**Example**

    ArrayList<Long> days = mPlayer.getTimelineDaysSync(false);
    if(days != null && days.size()>0){
      //fist day when record started
      String str = CloudHelpers.formatTime(days.get(0));
    }

#### int getTimelineDays(boolean use_timezone, ICompletionCallback callback)


**Description**

Get information about recorded days considering or not considering the timezone. This function is called asynchronously.
Records are provided in array of Long time values since 1.1.1970.

**Parameters**

boolean use_timezone. True - return Long time values taking into account camera timezone. False - Long time values are in UTC.
callback - callback is called if correct response is received from server or in case of error.

**Return values**

Upon successful completion, getTimelineDays() returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**
    
    int err =  mPlayer.getTimelineDays(false, new ICompletionCallback(){
                @Override
                public int onComplete(Object o_result, final int result) {
    
                    if(result == 0) {
                       ArrayList<Long> days = (ArrayList<Long>)o_result;
                       if(days != null && days.size()>0){
                           //fist day when record started
                           String str = CloudHelpers.formatTime(days.get(0));
                       }
                    }else {
                       //FAIL
                    }
                    return 0;
                }
            }););
            

#### double getLat();


**Description**

Get latitude of location.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No parameters

**Return values**

double latitude value.

**Example**

    double latitude = mPlayer.getLat();

#### double getLng();


**Description**

Get longitude of location.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No parameters

**Return values**

double longitude value.

**Example**

    double mPlayer.getLng();

#### boolean hasError();


**Description**

Check if error occurs.

**Parameters**

No parameters

**Return values**

true - error occurs;
false - no error.

**Example**

    boolean is_error = mPlayer.hasError();
    if(is_error){
      Log.e(TAG, "last error="+mPlayer.getResultInt()+" "+mPlayer.getResultStr());
    }

#### int getResultInt();


**Description**

Get Integer code of the latest result.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.

**Example**

    boolean is_error = mPlayer.hasError();
    if(is_error){
      Log.e(TAG, "last error="+mPlayer.getResultInt()+" "+mPlayer.getResultStr());
    }

#### String getResultStr()


**Description**

Get String value of the latest result.

**Parameters**

No parameters

**Return values**

Returns String value of CloudReturnCodes.

**Example**
    
    boolean is_error = mPlayer.hasError();
    if(is_error){
      Log.e(TAG, "last error="+mPlayer.getResultInt()+" "+mPlayer.getResultStr());
    }

#### int refreshSync()


**Description**

Update/refresh camera properties. This function is called synchronously.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.


#### int refresh(ICompletionCallback callback);


**Description**

Update/refresh camera properties. This function is called asynchronously.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.

**Example**

    int err = mPlayer.refresh(new ICompletionCallback(){
                @Override
                public int onComplete(Object o_result, final int result) {
    
                    if(result == 0) {
                       String name = mPlayer.getName();
                       boolean isRec = mPlayer.isRecording();
                    }else {
                                            //FAIL
                    }
                    return 0;
                }
            }););

#### void setRange(long startPos, long endPos)

**Description**

Set playback range.
In case player gets position beyond the bounds of [startPos, endPos] the player notifies CloudPlayerEvent.OUT_OF_RANGE and goes to PAUSED state.

**Parameters**

long startPos - start position
long endPos - end position


#### void resetRange()

**Description**

Reset playback range was set by setRange(long startPos, long endPos) before.

**Parameters**

None


## Class CloudStreamerSDK

Cloud-Server Streamer is a tool for live video streaming. 

IMPORTANT: Access token from DashBoard should be used here to stream video 

Please create channel and set access token in SetSource. 

#### Key features

* Live video streaming from mobile camera
* Preview image setting for defined channel   
* Various options: video resolution, video bitrate and others

#### public CloudStreamerSDK(ICloudStreamerCallback callback)

**Description**

Construct streamer object.

**Parameters**

callback - callback is used to notify application on various events: errors, started , stopped, connected and so on.

	public interface ICloudStreamerCallback {
	public void onStarted(String url_push); //Cloud gets ready for data, url_push == rtmp://...
	public void onStopped(); //Cloud closed getting the data
	public void onError(int error);
	public void onCameraConnected(); // getCamera() to get the camera
	}

**Return values**

Return new CloudStreamerSDK object

**Example**

		mCloudStreamer = new CloudStreamerSDK(new ICloudStreamerCallback() {
			@Override
			public void onStarted(String surl) {
				Log.v(TAG, "=>onStarted surl="+surl);
				rtmp_url = surl;
				// URL for streamer 
			}

			@Override
			public void onStopped() {
				// It is called on stopped 
			}

			@Override
			public void onError(final int result) {
              	// It is callsed on error
				Log.v(TAG, "=onError");
			}

			@Override
			public void onCameraConnected() {
                // it is called if channel is connected to VXG server   
			}
		});

#### int setSource(String access_token)

**Description**

Set access token of channel for streaming.
New channel is created in DashBoard or using AdminAPI.
Access token for streamer is defined after channel creating.

**Parameters**

access_token - access token to broadcast channel.

**Return values**

No return value

**Example**

	CloudStreamerSDK mCloudStreamer;
		mCloudStreamer = new CloudStreamerSDK(new ICloudStreamerCallback() {
			@Override
			public void onStarted(String surl) {
				Log.v(TAG, "=>onStarted surl="+surl);
				rtmp_url = surl;
          		// Streaming should be started on defined URL
			}

			@Override
			public void onStopped() {
				// All clients are disconnected , recording is OFF
                // streaming is stopped 
			}

			@Override
			public void onError(final int result) {
				// ERROR is happened
			}

			@Override
			public void onCameraConnected() {
				// Camera is connected to VXG backend
			}
		});

	String msAccessToken = "eyJ0b2tlbiI6InNoYXJlLmV5SnphU0k2SURFXQxMmNmZjc4MC5rNlIxWHdjX2ptUjRZSFU5QV9xSVFHc2liX2MiLCJjYW1pZCI6MTMxMDY0LCJhY2Nlc3MiOiJ3YXRjaCJ9"; 
	// Set access token
	mCloudStreamer.setSource(msAccessToken); //see onStatus()
	mCloudStreamer.Start();


#### int setConfig(String config)

**Description**

Restore saved CloudCamera session

**Example**

	CloudStreamerSDK streamer = new CloudStreamerSDK(null);
	String config = settings.getString("streamer_config", "");
	streamer.setConfig(config);

	
#### public void Start()

**Description**

Start channel object on backend.
Backend sends onStarted as soon as client is connected or record is enabled.
Backend sends onStopped as soon as latest client is disconnected and recording is disabled or in case if application call stop().

**Parameters**

There is not input parameters.

**Return values**

No return value

**Example**

	CloudStreamerSDK mCloudStreamer;
	mCloudStreamer = new CloudStreamerSDK(new ICloudStreamerCallback() {...};
	String msAccessToken = "access token is to set here";
	mCloudStreamer.setSource(msAccessToken);
	mCloudStreamer.Start();


#### public void Stop()

**Description**

Stop channel object on backend.
Backend sends onStopped as soon as channel is stopped.

**Parameters**

There is not input parameters.

**Return values**

No return value

**Example**

	CloudStreamerSDK mCloudStreamer;
	mCloudStreamer.Stop();


#### public int getPreviewHeight()

**Description**

Application is able to add custom image on backend. This image is used for channel preview.
Function returns a height of image that is required for defined channel.

**Parameters**

There are not input parameters

**Return values**

Return height of picture.

**Example**

	CloudStreamerSDK mCloudStreamer;
	...
		int width = mCloudStreamer.getPreviewWidth();
		int height = mCloudStreamer.getPreviewHeight();
	...


#### public int setPreviewImage(String file)

**Description**

Application is able to add custom image on backend. This image is used for channel preview.
Function uploads picture on server. Format of picture is JPG.

**Parameters**

File - absolute path to result picture.

**Return values**

No return value

**Example**

	CloudStreamerSDK mCloudStreamer;
	...
	// Convert picture to acceptable size
	mCloudStreamer.cropPreview("/sdcard/DCIM/Camera/picture1.jpg","/sdcard/DCIM/Camera/picture2.jpg");
	// Upload preview image on the server
	mCloudStreamer.setPreviewImage("/sdcard/DCIM/Camera/picture2.jpg");


#### public int getPreviewWidth()

**Description**

Application is able to add custom image on backend. This image is used for channel preview.
Function returns a width of image that is required for defined channel.

**Parameters**

There are not input parameters

**Return values**

Return width of picture.

**Example**

	CloudStreamerSDK mCloudStreamer;
	...
	int width = mCloudStreamer.getPreviewWidth();
	int height = mCloudStreamer.getPreviewHeight();
	...


#### public void cropPreview(String origPreviewFullpath, String cropPreviewFullpath)

**Description**

Application is able to add custom image on backend. This image is used for channel preview.
Format of the picture is JPG.
Function prepares picture before upload picture on server using function setPreviewImage().
Function converts and scales the defined picture for acceptable picture resolution.

**Parameters**

origPreviewFullpath - absolute path to original picture that is to be converted to.

cropPreviewFullpath - absolute path to result picture.

**Return values**

No return value

**Example**

	CloudStreamerSDK mCloudStreamer;
	...
	// Convert picture to acceptable size
	mCloudStreamer.cropPreview("/sdcard/DCIM/Camera/picture1.jpg","/sdcard/DCIM/Camera/picture2.jpg");
	// Upload preview image on the server
	mCloudStreamer.setPreviewImage("/sdcard/DCIM/Camera/picture2.jpg");


#### long getID();

**Description**

Return source ID. The ID is assigned to every camera that is created on VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getID() returns camera ID, otherwise negative value (-1) is returned.

**Example**

	long cam_id = mStreamer.getID();
	if(cam_id == -1){
		//camera connection error
		Log.e("Error=" + mStreamer.getResultInt() + " "+mStreamer.getResultStr());
	}


#### String getPreviewURLSync();


**Description**

Get URL to download preview image. This function is called synchronously.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getPreviewURLSync() returns URL, otherwise null is returned.

**Example**

	String URL = mStreamer.getPreviewURLSync();
	if (null != URL)
	// Download image 


#### int getPreviewURL(ICompletionCallback callback)


**Description**

Get URL. URL can be used to download camera preview in jpg format. This function is called asynchronously.

**Parameters**

ICompletionCallback - it is called if correct URL is received from the server or in the case of error.

**Return values**

Upon successful completion, getPreviewURL returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

	int err = mStreamer.getPreviewURLSync(new ICompletionCallback(){
            @Override
            public int onComplete(Object o_result, final int result) {

                if(result == 0) {
                               String URL = (String)o_result;
                               // download image here
                }else {
                                        //FAIL
                }
                return 0;
            }
        }););

		
#### long getTimeLive();


**Description**

Get the time when camera will be deleted on server. Until this time camera will be available.
It is useful when the video from camera will be viewed only once and you don't want to keep it on server after the player is closed.

Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getTimeLive() returns time, otherwise 0 value is returned.
Important note: time is set in UTC. Time format is Unix time in milliseconds.

**Example**

	long t = mPlayer.getTimeLive();


#### void setTimeLive(long time)


**Description**

Set time when camera will be deleted on server. Until this time camera will be available.
It is useful when the video from camera will be viewed only once and you don't want to keep it on server after the player is closed.

Use save() or saveSync() to save the value to VXG server.

**Parameters**

time - time when camera object is deleted on server.
Important note: time is set in UTC. Time format is Unix time in milliseconds.

**Return values**

No return value

**Example**

	// Delete camera in 10 minutes after this camera
	long t = getTime() + 60 000;  
	mStreamer.setTimeLive(t);
	mStreamer.saveSync();


#### void setTimezone(String time_zone)


**Description**

Set a time zone. Time zone is used further to set timeline, calendar, live time and other parameters.

Use save() or saveSync() to save the value to VXG server.

**Parameters**

time_zone - time zone of the camera. Complete list of supported time zones: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

**Return values**

No return value

**Example**

	mStreamer.setTimeZone("America/New_York");
	mStreamer.saveSync();


#### String getTimeZone()


**Description**

Get a time zone. Time zone is used further to set timeline, calendar, live time and other parameters.

Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getTimeZone() returns time zone, otherwise null value is returned.
time_zone - time zone of the camera. 
Complete list of supported time zones: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

**Example**

	String time_zone = mStreamer.getTimeZone();


#### String getName();


**Description**

Get a camera name.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

Upon successful completion, getName() returns camera name, otherwise null value is returned if camera name is not set.

**Example**

	String camera_name = mStreamer.getName();


#### void setName(String name)


**Description**

Set a camera name.
Use save() or saveSync() to save the value to VXG server.

**Parameters**

name - camera name.

**Return values**

No return value

**Example**

	mStreamer.setName("My camera");
	mStreamer.saveSync();


#### CloudCameraStatus getStatus();


**Description**

Return camera status.
Use refresh() or refreshSync() to update the value from VXG server.

There are following camera statuses:

(0) ACTIVE - Camera is live and works properly

(1) UNAUTHORIZED - Camera is not authorized

(2) INACTIVE - Camera is switched off

(3) INACTIVE_BY_SCHEDULER - Camera is switched off by scheduler

(4) OFFLINE - Camera is offline

**Parameters**

No input parameters

**Return values**

Upon successful completion, getStatus() returns CloudCameraStatus object, otherwise null value is returned.

**Example**

	CloudCameraStatus camera_status = mStreamer.getStatus();
	if (camera_status == CloudCameraStatus.ACTIVE)
	//Camera is online


#### void setRecordingMode(CloudCameraRecordingMode rec_mode);


**Description**

Control recording on the camera. There is limitation for media storage on server if trial key is used - recorded data is stored for 72 hours after the moment of recording.
Use save() or saveSync() to save the value to VXG server.

**Parameters**

rec_mode - recording option.
There are following recording options:

(0) CONTINUES - Continuous recording. Media data is recorded uninterruptedly in 24/7 mode.

(1) BY_EVENT - Record media data by event. Current option is supported by push camera only.

(2) NO_RECORDING - No recording or it can be used to stop recording.

**Return values**

No return value

**Example**

	CloudCameraRecordingMode mRecMode = CloudCameraRecordingMode.CONTINUES;
	mStreamer.setRecordingMode(mRecMode);
	mStreamer.saveSync();


#### CloudCameraRecordingMode getRecordingMode()


**Description**

Get recording state of camera. There is limitation for media storage on server if trial key is used - recorded data is stored for 72 hours after the moment of recording.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No input parameters

**Return values**

CloudCameraRecordingMode - recording state
There are following recording states:

(0) CONTINUES - Continuous recording. Media data is recorded uninterruptedly in 24/7 mode.

(1) BY_EVENT - Record media data by event. Current option is supported by push camera only.

(2) NO_RECORDING - No recording or it can be used to stop recording.

**Example**

	CloudCameraRecordingMode mRecMode = mStreamer.getRecordingMode();
	if (mRecMode == CloudCameraRecordingMode.CONTINUES)
		// Recording is ON
	else if (mRecMode == CloudCameraRecordingMode.NO_RECORDING)
		// Recording is OFF


#### boolean isRecording();


**Description**

Get recording state of the camera in current time.

**Parameters**

No input parameters

**Return values**

isRecording() returns true if recording is running, returns false if recording does not work for some reason.

**Example**

	boolen recording = mStreamer.isRecording();
		if (true == recording )
		// Camera recording works right now


#### CloudTimeLine getTimelineSync(long start, long end);


**Description**

Get information about recorded data on server in defined range. Range is set in start and end values. This function is called synchronously.

Records are provided in following structure:

	public class CloudTimeline {
	public long start; //requested time start
	public long end; //requested time end
	public List<Pair<Long, Long>> periods;
	}
where

start - start time of range where there are actual records

end - end time of range where there are actual records

periods - start and end time of every record.

All times are defined in Unix time (milliseconds).

**Parameters**

start - start time of range. It is set in milliseconds Unix time.

end - end time of range. It is set in milliseconds Unix time.

**Return values**

Upon successful completion, getTimelineSync() returns object CloudTimeLine, otherwise null value is returned. Null means there are no records for defined period.

**Example**

	//get record for latest day
	long time_start = get_time_cur_ms()-24*3600*1000;
	long time_end = get_time_cur_ms();
	CloudTimeline timeline = mStreamer.getTimelineSync(time_start, time_end);


#### int getTimeline(long start, long end, ICompletionCallback callback);


**Description**

Get information about recorded data on server in defined range. Range is set in start and end values. This function is called asynchronously.

Records are provided in following structure in callback function.

	public class CloudTimeline {
	public long start; //requested time start
	public long end; //requested time end
	public List<Pair<Long, Long>> periods;
	}
where

start - start time of range where there are actual records

end - end time of range where there are actual records

periods - start and end time of every record.

All times are defined in Unix time (milliseconds).

**Parameters**

start - start time of range. It is set in milliseconds Unix time.

end - end time of range. It is set in milliseconds Unix time.

callback - callback is called if correct response is received from server or in case error.

**Return values**

Upon successful completion, getTimeline() returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

	//get record for latest day in async callback
	long time_start = get_time_cur_ms()-24*3600*1000;
	long time_end = get_time_cur_ms();
	int err =  mStreamer.getTimeline(time_start, time_end,new ICompletionCallback(){
            @Override
            public int onComplete(Object o_result, final int result) {

                if(result == 0) {
                               CloudTimeline TimeLine = (CloudTimeline)o_result;
                               // Handle CloudTimeLine 
                }else {
                                        //FAIL
                }
                return 0;
            }
        }););
        


#### ArrayList<Long> getTimelineDaysSync(boolean use_timezone);


**Description**

Get information about recorded days taking into account use or not timezone. This function is called synchronously.
Records are provided in array of Long time values since 1.1.1970.

**Parameters**

boolean use_timezone. true - return Long time values taking into account camera timezone. false - Long time values are in UTC.

**Return values**

Upon successful completion, getTimelineDaysSync() returns object ArrayList, otherwise null value is returned. Empty array means there are no records for defined period.

**Example**

	ArrayList<Long> days = mStreamer.getTimelineDaysSync(false);
	if(days != null && days.size()>0){
		//fist day when record started
		String str = CloudHelpers.formatTime(days.get(0));
	}


#### int getTimelineDays(boolean use_timezone, ICompletionCallback callback)


**Description**

Get information about recorded days taking into account use or not timezone. This function is called asynchronously.
Records are provided in array of Long time values since 1.1.1970.

**Parameters**

boolean use_timezone. true - return Long time values taking into account camera timezone. 
false - Long time values are in UTC.

callback - callback is called if correct response is received from server or in case error.

**Return values**

Upon successful completion, getTimelineDays() returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

	int err =  mStreamer.getTimelineDays(false, new ICompletionCallback(){
            @Override
            public int onComplete(Object o_result, final int result) {

                if(result == 0) {
                   ArrayList<Long> days = (ArrayList<Long>)o_result;
                   if(days != null && days.size()>0){
                       //fist day when record started
                       String str = CloudHelpers.formatTime(days.get(0));
                   }
                }else {
                   //FAIL
                }
                return 0;
            }
        }););
        


#### void setLatLngBounds(double latitude, double longitude)


**Description**

Set location coordinates of mobile camera.
Use save() or saveSync() to save the value to VXG server.

**Parameters**

latitude, longitude

**Return values**

No return value.

**Example**

	mStreamer.setLatLngBounds(56.5, 84.5);
	mStreamer.saveSync();


#### double getLat();


**Description**

Get location property: latitude.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No parameters

**Return values**

double latitude value.

**Example**

	double latitude = mStreamer.getLat();


#### double getLng();


**Description**

Get location property: longitude.
Use refresh() or refreshSync() to update the value from VXG server.

**Parameters**

No parameters

**Return values**

double longitude value.

**Example**

	double latitude = mStreamer.getLat();


#### boolean hasError();


**Description**

Check is error occurs.

**Parameters**

No parameters

**Return values**

true - error occurs;
false - no error.

**Example**

	boolean is_error = mStreamer.hasError();
	if(is_error){
		Log.e(TAG, "last error=" + mStreamer.getResultInt() + " " + mStreamer.getResultStr());
	}


#### int getResultInt();


**Description**

Get last result Integer code.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.

**Example**

	boolean is_error = mStreamer.hasError();
	if(is_error){
		Log.e(TAG, "last error=" + mStreamer.getResultInt() + " " + mStreamer.getResultStr());
	}


#### String getResultStr()


**Description**

Get last result String value.

**Parameters**

No parameters

**Return values**

Returns String value of CloudReturnCodes.

**Example**

	boolean is_error = mStreamer.hasError();
	if(is_error){
		Log.e(TAG, "last error=" + mStreamer.getResultInt() + " " + mStreamer.getResultStr());
	}


#### int refreshSync()


**Description**

Update/refresh camera properties. This function is called synchronously.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.


#### int refresh(ICompletionCallback callback);


**Description**

Update/refresh camera properties. This function is called asynchronously.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.

**Example**

	int err = mStreamer.refresh(new ICompletionCallback(){
            @Override
            public int onComplete(Object o_result, final int result) {

                if(result == 0) {
                   String name = mStreamer.getName();
                   boolean isRec = mStreamer.isRecording();
                }else {
                                        //FAIL
                }
                return 0;
            }
        }););


#### int saveSync()


**Description**

Save camera properties. This function is called synchronously.

**Parameters**

No parameters

**Return values**

Returns CloudReturnCodes value.


#### int save(ICompletionCallback callback);


**Description**

Save camera properties. This function is called asynchronously.

**Parameters**

callback - result callback

**Return values**

Returns CloudReturnCodes value.

**Example**

	int err = mStreamer.refresh(new ICompletionCallback(){
            @Override
            public int onComplete(Object o_result, final int result) {

                if(result == 0) {
                  Log.i("Camera properties saved OK");
                }else {
                                        //FAIL
                  Log.i("Camera properties save FAILED err=" + result);
                }
                return 0;
            }
        }););


#### int putTimelineSegmentSync(long timeStart, long timeStop, String fileSegment);

**Description**

Upload file segment to the cloud timeline. This function is called synchronously.

**Parameters**

long timeStart - start time of the segment.

long timeStop - end/stop time of the segment.

String fileSegment - absolute path to file has to be uploaded to the cloud timeline.

**Return values**

Upon successful completion, putTimelineSegmentSync returns 0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

	final long minute = 60 * 1000;
	final long time_cur = CloudHelpers.currentTimestampUTC();
	final String filename1 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180406_175730_0.mp4";
	final String filename2 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180406_175801_1.mp4";
	Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			int ret1 = mCloudStreamer.putTimelineSegmentSync(time_cur - 5 * minute, time_cur - 4 * minute - minute / 2, filename1);
			int ret2 = mCloudStreamer.putTimelineSegmentSync(time_cur - 4 * minute - minute / 2, time_cur - 4 * minute, filename2);
		}
	});
	t.start();


#### int putTimelineSegment(long timeStart, long timeStop, String fileSegment, ICompletionCallback callback);

**Description**

Upload file segment to the cloud timeline. This function is called asynchronously.

**Parameters**

long timeStart - start time of the segment.

long timeStop - end/stop time of the segment.

String fileSegment - absolute path to file has to be uploaded to the cloud timeline.

ICompletionCallback - completion callback.

**Return values**

Upon successful completion, putTimelineSegment returns >=0, otherwise negative value is returned. All errors are described in section Errors.

**Example**

	final long minute = 60 * 1000;
	final long time_cur = CloudHelpers.currentTimestampUTC();
	final String filename1 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180406_175730_0.mp4";
	final String filename2 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180406_175801_1.mp4";

	mCloudStreamer.putTimelineSegment(time_cur - 5 * minute, time_cur - 4 * minute - minute / 2, filename1,
		new ICompletionCallback() {
			@Override
			public int onComplete(Object o_result, int result) {
				return 0;
			}
	});

	mCloudStreamer.putTimelineSegment(time_cur - 4 * minute - minute / 2, time_cur - 4 * minute, filename2,
		new ICompletionCallback() {
			@Override
			public int onComplete(Object o_result, int result) {
				return 0;
			}
		});
	}


## Class CloudPlayerConfig

Config to control player settings. 

Following setting are supported: 
* Show/Hide Controls
* Control video output  
* Control Latency 
* Control buffer on start and during playback


#### public CloudPlayerConfig()

**Description**

Allocate config object with default value in every setting.

**Parameters**

No input parameters

**Return values**

No return value

**Example**

	// Allocate new config object
	CloudPlayerConfig mPlayerConfig = new CloudPlayerConfig();


#### public CloudPlayerConfig(CloudPlayerConfig src)

**Description**

Allocate config object and initialize with defined config.

**Parameters**

CloudPlayerConfig src

**Return values**

No return value

**Example**

	// Allocate new config object with existing settings
	CloudPlayerConfig mPlayerConfig = new CloudPlayerConfig(src);


#### void visibleControls(boolean bControls)

**Description**

Present or hide visible controls in player. This setting is not supported by API 2.0 and lower versions.

**Parameters**

bControls - shows control if value is true, otherwise hides control.

**Return values**

No return value

**Example**

	// Set visible control
	mPlayerConfig.visibleControls(true);


#### void aspectRatio(int mode)

**Description**

Set video output mode. It is supported starting from API 2.1.

Following modes are supported:

0 - stretch

1 - fit to screen with aspect ratio

2 - crop

3 - 100% size

4 - zoom mode

5 - move mode

**Parameters**

mode - video output mode. Default value is 1. Correct values are 0-5 .

**Return values**

No return value

**Example**

	// Scale video according video view without aspect ratio.
	mPlayerConfig.visibleControls(1);


#### void setMinLatency(long Latency)

**Description**

Set latency control. Function is supported starting from version API 2.1.
Latency in player is controlled by reducing the number of inner buffers in pipeline.

**Parameters**

Latency - set max latency in milliseconds.

**Return values**

No return value

**Example**

	// Set latency = 200 ms in player 
	mPlayerConfig.visibleControls(200);


#### void setBufferOnStart(int bufferring_time)

**Description**

Set buffer size on start of playback. Buffer size is allocated based on time stamp information in stream. This setting is supported starting from API 2.1.

**Parameters**

buffering_time - size of buffer in milliseconds.

**Return values**

No return value

**Example**

	// buffering 1 second video and audio on start of playback
	mPlayerConfig.setWaitTimeStartStream(1000);

## Enum CloudReturnCodes

Returning codes and error codes. Use **getResultInt()** or **getResultStr()** to get the last error.

| Name | Code | Description |
| ------ | ------ | ------ |
|OK	|0	|SUCCESS
|OK_COMPLETIONPENDING	|1	|Operation is pending
|ERROR_NOT_IMPLEMENTED	|-1	|Function or method is not implemented
|ERROR_NOT_CONFIGURED	|-2	|Object is not configured
|ERROR_NO_MEMORY	|-12	|Out of memory
|ERROR_ACCESS_DENIED	|-13	|Access denied
|ERROR_BADARGUMENT	|-22	|Invalid argument
|ERROR_STREAM_UNREACHABLE	|-5049	|The stream is not reachable. Please double check the URL address and restart the stream
|ERROR_EXPECTED_FILTER	|-5050	|Expected filter
|ERROR_NO_CLOUD_CONNECTION	|-5051	|No cloud connection (there is no connection object or token is invalid)
|ERROR_WRONG_RESPONSE	|-5052	|Response from cloud is expected in json, but we got something elseSource is not configured
|ERROR_INVALID_SOURCE	|-5054	|Invalid sourceRecords not found
|ERROR_NOT_AUTHORIZED	|-5401	|Failed authorization on cloud (wrong credentials)
|ERROR_NOT_FOUND	|-5404	|Object not found

## Enum CloudPlayerEvent

Notifications that player object generates in various states 

| Name | Code | Description |
| ------ | ------ | ------ |
|CONNECTING	|0	|Connection is establishing
|CONNECTED	|1	|Connection is established
|STARTED	|2	|play() is successfully done. Player goes to state STARTED.
|PAUSED	|3	|pause() is successfully done. Player state is PAUSE.
|CLOSED	|6	|close() is successfully finished. Player state is CLOSED.
|EOS	|12	|End-Of-Stream event is notifying that playback is stopped because of the end of stream.
|OUT_OF_RANGE	|13	|OUT_OF_RANGE(13), / Out-Of-Range event /The event occurs if setRange(long startPos, long endPos) was set and player position went out of this range.
|SEEK_COMPLETED	|17	|setPosition() is successfully finished.
|ERROR	|105	|Player is disconnected from media stream due to an error
|SOURCE_CHANGED	|3000	|setSource() is successfully finished.
|TRIAL_VERSION	|-999	|Trial version limitation - playback is limited to 2 minutes.

## Enum CloudPlayerState

Player states 

| Name | Code | Description |
| ------ | ------ | ------ |
|CONNECTING	|0	|Connection is establishing
|CONNECTED	|1	|Connection is established
|STARTED	|2	|play() is successfully done. Player state is STARTED.
|PAUSED	|3	|pause() is successfully done. Player state is PAUSED.
|CLOSED	|6	|close() is successfully finished. Player state is CLOSED.
|EOS	|12	|Player stopped because the End-Of-Stream is reached.

