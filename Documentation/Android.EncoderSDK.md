# Overview

VXG Mobile SDK structure:

    |-- VXGMobileSDK.Android
        |-- MediaSDK
            |-- PlayerSDK
            |-- EncoderSDK
        |-- CloudSDK

**Encoder SDK** is a part of **VXG Mobile SDK** responsible for capture video from built-in camera, encoding and streaming video. 

## Key features
* Streaming formats: WebRTC, RTSP and RTMP publishing.  
* Hardware acceleration of encoding up to UHD resolution.
* Multi-core software encoding.
* Dual channel encoding.
* Video integration with any Activity using SurfaceView.
* Hardware video pre- and post-processing using OpenGL shaders.
* Event notifications.
* Low latency encoding and streaming.
* Video recording into an mp4 file in parallel with streaming.
* Video transcoding.
* Encrypted and transfers via HTTPS tunnelling.
* Streaming from files.
* Network bandwidth test feature.

# How to use
## SDK requirements
| | |
| ------ | ------ |
| Android version | 4.4  or  newer |
| Development environments | Android Studio |

## SDK structure
The SDK package consists of the following files and folders:

* aar   (Aar archive) 
* bin 	(Link on the samples applications binaries)
* docs	(Files with links to documentation)
* src 	(Sample applications source code)

## Block diagram

![Screenshot](https://user-images.githubusercontent.com/11888021/156012158-549670a6-b66b-4436-8407-e722bb33b568.png)


## How to apply the license key

This is **not required for evaluation and testing**. It is only required after purchasing a license in order to remove the evaluation version limitations.

**Step 1:** Copy the Mobile license key from the License & Plans page

![Screenshot](https://user-images.githubusercontent.com/11888021/156011640-5da87a5d-c208-4b90-87ed-0adca54b9213.png)

**Step 2:** Create an empty file called **license** in your application folder **assets** and paste your license key into this file. See the picture below how it should look in corresponded test application **\streamland_streamer**.

![Screenshot](https://user-images.githubusercontent.com/11888021/156011770-cdba7598-5547-4f94-b697-2fa2db34d359.png)

## Integration with an application
#### Integration using a resource file in 2 steps:

**Step 1:** Add to layout xml for your activity as below:

    <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android" 
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        >
        <veg.mediacapture.sdk.MediaCapture 
            android:id="@+id/captureView"                     
            android:layout_width="fill_parent"
            android:layout_height="fill_parent" 
            android:layout_gravity="center"
        />
    </FrameLayout>


**Step 2:** Change the main activity 
(MainActivity.java)

    public	class	MainActivity extends	Activity	implements
    MediaCapture.MediaCaptureCallback
    {
    ...
        // callback handler
        @Override
            public int OnCaptureStatus(int arg) { return 0; };
        @Override
            public int OnCaptureReceiveData(ByteBuffer buffer, int type, int size, long pts){ return 0; };
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
        ...
            // Create Capturer instance
            capturer = (MediaCapture)findViewById(R.id.captureView);
            //adjust Capturer’ config
            MediaCaptureConfig config = capturer.getConfig(); 
            config.setUrl(“rtmp://srv”); 
            config.setStreaming(true);
            //etc
            //open the Capturer 
            capturer.Open(null, this);
            protected void onPause()
        {
            Log.e(TAG, "onPause()"); super.onPause();
            if (capturer != null)
            capturer.onPause();
        }
        @Override
        protected void onResume()
        {
            Log.e(TAG, "onResume()"); super.onResume();
            if (capturer != null)
            capturer.onResume();
        }
        @Override
        protected void onStart()
        {
            Log.e(TAG, "onStart()");
            super.onStart(); 
            sMainActivity = this;
            // Lock screen mWakeLock.acquire();
            if (capturer != null)
            capturer.onStart();
        }
        @Override
        protected void onStop()
            {
            Log.e(TAG, "onStop()");
            super.onStop();
            if (capturer != null)
            capturer.onStop();
            // A WakeLock should only be released when isHeld() is true ! if (mWakeLock.isHeld()) mWakeLock.release();
            if (toastShot != null)
            toastShot.cancel();
            if(misSurfaceCreated){ finish();
            }
        }  
        @Override
        public void onBackPressed()
        {
            if (toastShot != null)
            toastShot.cancel();
            if(capturer != null)
            capturer.Close();
            super.onBackPressed();
        }
        @Override
        public void onWindowFocusChanged(boolean hasFocus)
        {
            Log.e(TAG, "onWindowFocusChanged(): " + hasFocus);
            super.onWindowFocusChanged(hasFocus);
            if (capturer != null)
            capturer.onWindowFocusChanged(hasFocus);
        }
        @Override
        public void onLowMemory()
        {
            Log.e(TAG, "onLowMemory()"); super.onLowMemory();
            //if (capturer != null)
            //	capturer.onLowMemory();
        }
        @Override
        protected void onDestroy()
        {
            Log.e(TAG, "onDestroy()"); if (toastShot != null)
            toastShot.cancel();
            if (capturer != null)
            capturer.onDestroy(); System.gc();
            if (multicastLock != null) { multicastLock.release(); multicastLock = null;
        }
        super.onDestroy();
        }
    }



#### Dynamic Integration (without modifying resources)

**Step 1:** This approach is similar to the previous one except the capturer is created dynamically within the onCreate() method

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    ...
        // Create Capturer instance
        capturer = new MediaCapture(this, null);
        FrameLayout.LayoutParams	params	=	new FrameLayout.LayoutParams(250,250, Gravity.CENTER); 
        capturer.setLayoutParams(params);
    // Add Capture Instance to layout
    FrameLayout lp = (FrameLayout)findViewById(R.id.captureView); 
    lp.addView(capturer);
        //adjust Capturer’ config
        MediaCaptureConfig config = capturer.getConfig(); 
        config.setUrl(“rtmp://srv”); 
        config.setStreaming(true);
        //etc
        //open the Capturer capturer.Open(null, this);
    protected void onPause()
    {
        Log.e(TAG, "onPause()"); 
        super.onPause();
        if (capturer != null)
        capturer.onPause();
    }
    @Override
    protected void onResume()
    {
        Log.e(TAG, "onResume()");
        super.onResume(); 
        if (capturer != null)
        capturer.onResume();
    }
    @Override
    protected void onStart()
    {
        Log.e(TAG, "onStart()");
        super.onStart(); 
        sMainActivity = this;
        // Lock screen mWakeLock.acquire();
        if (capturer != null)
        capturer.onStart();
    }
    @Override
    protected void onStop()
    {
        Log.e(TAG, "onStop()"); 
        super.onStop();
        if (capturer != null)
        capturer.onStop();
        // A WakeLock should only be released when isHeld() is true ! if (mWakeLock.isHeld()) mWakeLock.release();
        if (toastShot != null)
        toastShot.cancel(); 
        if(misSurfaceCreated){ finish();
    }
    }
    @Override
    public void onBackPressed()
    {
        if (toastShot != null)
        toastShot.cancel();
        if(capturer != null)
        capturer.Close();
        super.onBackPressed();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
    Log.e(TAG, "onWindowFocusChanged(): " + hasFocus); super.onWindowFocusChanged(hasFocus);
    if (capturer != null)
    capturer.onWindowFocusChanged(hasFocus);
    }
    @Override
    public void onLowMemory()
    {
        Log.e(TAG, "onLowMemory()"); 
        super.onLowMemory();
        //if (capturer != null)
        //	capturer.onLowMemory();
    }
    @Override
    protected void onDestroy()
    {
        Log.e(TAG, "onDestroy()");
        if (toastShot != null)
        toastShot.cancel();
        if (capturer != null)
        capturer.onDestroy(); System.gc();
        if (multicastLock != null) { multicastLock.release(); multicastLock = null;
    }
    super.onDestroy();
    }

#### Integration with Activity

The VXG SDK is based on SurfaceView and can be integrated with any Activity using the code below:

    <FrameLayout
        android:id="@+id/captureViewLayout" 
        android:layout_width="fill_parent" 
        android:layout_height=" fill_parent " >
    < veg.mediacapture.sdk.MediaCapture 
        android:id="@+id/captureView" 
        android:layout_width="fill_parent" 
        android:layout_height="fill_parent" 
        android:layout_gravity="center" />
    </FrameLayout>

#### Manifest requirements
The following settings should be set in manifest to avoid any issues with camera using and the SDK.

    android:launchMode="singleInstance" 
    android:noHistory="true" 
    android:configChanges="orientation|screenSize"


# Media capture
#### Notifications
The SDK notifies about results, errors and notifications using the “MediaCapture” callback. All messages are synchronous and the SDK core waits until the application handles a message.

|Value|	Name|	Type|	Description|
| ------ | ------ | ------ | ------ |
|700	|CAP_OPENED	|NOTIFICATION	|Capturer has been opened successfully
|701	|CAP_STARTED	|NOTIFICATION	|Capturer has been started successfully
|702	|CAP_STOPPED	|NOTIFICATION	|Capturer has been stopped successfully
|703	|CAP_CLOSED	|NOTIFICATION	|Capturer has been closed successfully
|704	|CAP_ERROR	|NOTIFICATION	|Error has happened, details can be gotten by the call function: ErrorGetRTMPStatus or getRECStatus
|705	|CAP_TIME	|NOTIFICATION	|Modules statistics were refreshed
|706	|CAP_SURFACE_CREATED	|NOTIFICATION	|Surface is created, Important notification start function is to be called after this notification
|707	|CAP_SURFACE_DESTROYED	|NOTIFICATION	|Surface is destroyed
|708	|CAP_RECORD_STARTED	|NOTIFICATION	|File   recording   started,   see   the   record property PP_RECORD_STAT_FILE_NAME
|709	|CAP_RECORD_STOPPED	|NOTIFICATION	|File   recording   stopped   see   the   record property PP_RECORD_STAT_FILE_NAME_STO PPED

## Functions description
Following functions are members of the MediaCapture class.

#### Open
Open the camera, create a preview and initialize all modules.

**Definition**

    int Open(final MediaCaptureConfig config, final MediaCaptureCallback callback)

**Parameters**
 
MediaCaptureConfig - Initializes parameters
MediaPlayerCallback	- Notification callback, event is provided over this callback

**Return Value**
 
Upon successful completion Open() returns 0. Otherwise -1 is returned. All errors are provided in the callback status.

**Remarks**
 
Connect to the network resource or open a local media file, create a pipeline, allocate resources and start video playback.

**Example**

    MediaCapture  capturer = new MediaCapture();
    // Get config
    MediaCaptureConfig config = capturer.getConfig(); 
        config.setStreaming(true);    
        config.setCaptureMode(ncm);
        config.setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_AAC); 
        config.setVideoBitrate(abitrate); 
        config.setAudioSamplingRate(44100); //hardcoded 
        config.setAudioChannels(2);
        config.setUrl(rtmp_url); 
        config.setvideoOrientation(0); //landscape 
        config.setVideoFramerate(30); 
        config.setVideoBitrate(vbitrate);
    player.Open(null, This);

All configuration parameters are described in the table below:

|Name 	|Description 	|Values 	|Default 	|Type|
| ------ | ------ | ------ | ------ | ------ |
|Streaming	|Set/Get Enable streaming module |		|True	|Boolean
|UseAVSync	|Set/Get Enable AV sync|		|True	|Boolean
|AudioFormat	|Set/Get Control audio format	|TYPE_AUDIO_AAC TYPE_AUDIO_AC3 TYPE_AUDIO_AMR_N TYPE_AUDIO_AMR_WB TYPE_AUDIO_EAC3 TYPE_AUDIO_FLAC TYPE_AUDIO_G711_ALA W TYPE_AUDIO_G711_MLA W TYPE_AUDIO_RAW TYPE_AUDIO_VORBIS TYPE_AUDIO_MPEG TYPE_AUDIO_MSGSM TYPE_AUDIO_OPUS TYPE_AUDIO_QCELP	|TYPE_AUDIO_G711_ALAW	|String
|AudioSamplingRate	|Set/Get Control audio sample rate	|8000-96000 (depends on device capabilities)	|44100	|Int
|AudioChannels	|Set/Get Control number of audio channels	    |1-5 (depends on device capabilities)	    |2	    |Int
|AudioBitrate	|Set/Get Control Audio bitrate	|Kpbs	|128	|Int
|VideoBitrate	|Set/Get Control Video bitrate	|Kpbs	|1000	|Int
|VideoFramerate	|Set/Get Control video frame rate|		|30	|Int
|VideoOrientation	|Set/Get Control orientation	|0: landscape; 90: portrait	|0	|Int
|VideoKeyFrameInterval	|Set/Get Control Video Key interval	|0 - 100 , 0 - every frame is key-frame, 1 - one key frame per second, 2 - one key frame per 2 seconds and so on|1	|Int
|VideoResolution	|Set/Get Control Video resolution	|VR_1920x1080(0) VR_1280x720(1) VR_640x480(2) VR_320x240(3) VR_3840x2160(4), VR_720x576(5), VR_640x480(6), VR_352x288(7), VR_176x144(8), VR_640x360(9), VR_720x405(10), VR_864x486(11), VR_960x540(12)	|VR_1280x720	|CaptureVideoResolution
|SecVideoBitrate	|RTSP only secondary video Set/Get Control Video bitrate	|Kpbs	|1000	|Int
|SecVideoFramerate	|RTSP only secondary video Set/Get Control video frame rate	|Kbps	|30	|Int
|SetVideoKeyFrameInterval	|RTSP only secondary video Set/Get Control video Key interval	|0 - 100 , 0 - every frame is key-frame, 1 - one key frame per second, 2 - one key frame per 2 seconds and so on|1	|Int
|SecVideoResolution	|RTSP only secondary video Set/Get Control Video resolution	|VR_1920x1080(0) VR_1280x720(1) VR_640x480(2) VR_320x240(3) VR_3840x2160(4), VR_720x576(5), VR_640x480(6), VR_352x288(7), VR_176x144(8), VR_640x360(9), VR_720x405(10), VR_864x486(11), VR_960x540(12)	|VR_320x240	|CaptureVideoResolution 
|CameraFacing	|Set/Get front or back camera |Back camera - 0, FRONT Camera -  1 	| 0	| Int 
|CaptureSource	|Set/Get capture surface |	PP_MODE_CAMERA(0xffffffff)	PP_MODE_VIRTUAL_DISPLAY(0x00000001) 	PP_MODE_SURFACE(0x00000002) PP_MODE_OFFSCREEN_SURFACE(0x00000004)  	| PP_MODE_CAMERA | Int 

Recording options:

|Name 	|Description 	|Values 	|Default 	|Type|
| ------ | ------ | ------ | ------ | ------ |
|Recording	|Set/Get Enable video recording|		|false	|Boolean
|RecordPath	|Set/Get Set full path for recorded files|		|“”	|String
|RecordFlags	|Set/Get Set setting for recording	|PP_RECORD_NO_START( 0x00000000) PP_RECORD_AUTO_STA RT(0x00000001) PP_RECORD_SPLIT_BY_TI ME(0x00000002) PP_RECORD_SPLIT_BY_SI ZE(0x00000004) PP_RECORD_DISABLE_VI DEO(0x00000008) PP_RECORD_DISABLE_A UDIO(0x00000010)	|0	|PlayerRecordFlags
|RecordSplitTime	|Set/Get Split stream on chunks by time if flags are PP_RECORD_SP LIT_BY_TIME, in seconds|	|0	|Int
|RecordSplitSize	|Set/Get Split stream on chunks by size if flags are PP_RECORD_SP LIT_BY_ SIZE, in seconds|		|0	|Int
|RecordPrefix	|Set/Get Prefix is added to name of recorded files|		|“”	|String

Transcoding options:

|Name 	|Description 	|Values 	|Default 	|Type|
| ------ | ------ | ------ | ------ | ------ |
|Transcoding	|Set/Get Enable transcoding|		|False	|Boolean
|TransWidth	|Set/Get Control width of the transcoded picture|		|256	|Int
|TransHeight	|Set/Get Control height of the transcoded picture|		|144	|Int
|TransFps	|Set/Get Control height of the transcoded picture|		|2	|Int
|TransFormat	|Set/Get	|TYPE_VIDEO_RAW	|TYPE_VIDEO_RAW	|String
|StreamType	|Set/Get Set mode: 1) RTMP publish 2)RTSP server 3) Network Bandwidth test	|STREAM_TYPE_RTMP_PU BLISH =0x1, STREAM_TYPE_RTSP_SER VER=0x2; STREAM_TYPE_RTSP_Z IO_SERVER	|STREAM_TYP E_RTMP_PUB LISH	|Streamer Types
|VideoUseSrcFile	|Set/Get Set mode stream from file|		|false	|Boolean
|VideoSrcFilePath	|Set/Get Set complete path to file that will be streamed|		|“”	|String

#### Config.setSecureStreaming
Enable encrypted channel RTP	by	HTTPS.	It	works	if	set	StreamType= STREAM_TYPE_RTSP_SERVER.

**Definition**

    public void setSecureStreaming (boolean	enabled, String sslCertPEM, String RsaPrivateKey)

**Parameters**
 
Enabled – enable encrypted channel SSL in HTTP tunnel. 
sslCertPEM – ssl certificate in pem format.
RsaPrivateKey - RSA private key.

**Return Value**
 
No value is returned by function SetSecureStreaming.

**Remarks**
 
Enable encrypted channel RTP by HTTPS.
The URL for the tunneled stream is rtsp://TX_IP:8080/ch0 .

**Examples**

    config. SetSecureStreaming (true, certificate, private_key);

#### Close
Close the capturer and release all resources.

**Definition**

    public void Close()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
No value is returned by function Remarks
Closes capturer, destroys the pipeline, releases all resources that were allocated on the Open() call.

**Examples**

    capturer.Close ();


#### Start
Start all modules (streaming, recording and transcoding) according to the configuration.

**Definition**

    public void Start()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
No value is returned by this function

**Remarks**
 
Start all modules (streaming, recording and transcoding) according to the configuration. 
**Important note:** Start function should be called after CAP_SURFACE_CREATED notification.

**Examples**

    capturer.Start();

#### Stop
Stop all started modules. State is changed from Started to Stopped.

**Definition**

    public void Stop()

**Parameters**
 
There are no parameters for this call
 
**Return Value**
 
No value is returned by this function

**Remarks**
 
Stop all started modules and change state from Started to Stopped.

**Examples**

    capturer.Stop ();


#### StartStreaming
Start only streaming module.

**Definition**

    public void StartStreaming()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
No value is returned by this function

**Remarks**
 
Start streaming module. Format of streaming is set configuration.
**Important  note:**  Start  function  should  be  called  after  CAP_SURFACE_CREATED notification.

**Examples**

    capturer.StartStreaming();
 
#### StopStreaming
Stop streaming module.

**Definition**

    public void StopStreaming()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
No value is returned by this function

**Remarks**
 
Stop streaming module.

**Examples**

    capturer.StopStreaming ();

#### StartRecodring
Start only recording module.

**Definition**

    public void StartRecording()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
No value is returned by this function

**Remarks**
 
Start the recording module.
**Important  note:**  Start  function  should  be  called  after  CAP_SURFACE_CREATED notification. 

**Examples**

    capturer.StartRecording();

#### StopRecording
Stop recording module.

**Definition**

    public void StopRecording()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
No value is returned by this function

**Remarks**
 
Stop only the recording module.

**Examples**

    capturer.StopRecording ();

#### StartTranscoding
Start only the transcoding module.

**Definition**

    public void StartTranscoding()

**Parameters**
 
There are no parameters for this call 

**Return Value**
 
No value is returned by this function

**Remarks**
 
Start transcoding module.
**Important  note:**  Start  function  should  be  called  after  CAP_SURFACE_CREATED notification.

**Examples** 

    capturer.StartTranscoding();

#### StopTranscoding
Stop transcoding module.

**Definition**

    public void StopTranscoding()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
No value is returned by this function

**Remarks**
 
Stop trancoding module.

**Examples** 

    capturer.StopTranscoding ();

#### getState
Return capturer state. 

**Definition**

    public CaptureState getState()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
Following states are provided: 
0	- Opening
1	- Opened
2	- Started
3	- Paused
4	- Stopped
5	- Closing
6	- Closed

**Remarks**
 
Provide the current state of capturer.

**Examples**

    if (capturer.getState() == CapturerState.Closing) ;

#### getRTMPStatus
Return status of RTMP.

**Definition**

    public CaptureState getRTMPState()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
Following states are provided: 
0  – NO ERROR
-1 – Try to connect
-5 – Connecting error
-12 – Out of memory
-999 – Demo version

**Remarks**
 
Provides the current state of capturer.

**Examples**

    if (capturer.getRTMPState() == CapturerState.Closing) ;

#### getRecStatus
Returns status of the Recording module.

**Definition**

    public CaptureState getRecState()

**Parameters**
 
There are no parameters for this call

**Return Value**
 
Following states are provided: 
0 – NO ERROR
-1 – Try to open file
-5 – File open error
-12 – Out of memory
-999 – Demo version

**Remarks**
 
Provide the current state of capturer.

**Examples**

    if (capturer.getRecState() == CapturerState.Closing) ;

#### getDuration
Returns the time from that is expired from starting of capturer.

**Definition**

    public long getDuration()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getDurarion()  returns time in milliseconds  from capturer start. Otherwise, -1 is returned. All errors are provided in callback status.

**Remarks**
 
Return time from that is expired from starting of capturer.

**Examples**

    int duration = capturer.getDuration() ;

#### getVideoPackets
Provide the number of video frames in buffer before streaming.

**Definition**

    public long getVideoPackets()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getVideoPackets() returns number of frames. Otherwise, -1 is returned. All errors are provided in the callback status.

**Remarks**
 
Provide the number of video frames in buffer before streaming. It is used for streaming only, mode: Publish RTMP.

**Examples**

    int duration = capturer. getVideoPackets () ;
 
#### getAudioPackets
Provide the number of audio frames in buffer before streaming.

**Definition**

    public long getAudioPackets()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getAudioPackets()  returns number of frames. Otherwise,
-1 is returned. All errors are provided in callback status.

**Remarks**
 
Provide the number of audio frames in buffer before streaming. It is used for streaming only, mode: Publish RTMP.

**Examples**

    int duration = capturer. getAudioPackets () ;

#### getLastVideoPTS
Provide the timestamp for the last video frame. It is sent by streaming module by network.

**Definition**

    public long getLastVideoPTS()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getLastVideoPTS () returns a timestamp. Otherwise, -1 is returned. All errors are provided in the callback status.
 
**Remarks**
 
Provides the timestamp for the last video frame. It is sent by streaming module by network. It is used for only the streaming module in case the mode is Publish RTMP.

**Examples**

    int v_pts = capturer. getLastVideoPTS ();

#### getLastAudioPTS
Provides the timestamp for last audio sample. It is sent by streaming module by network.

**Definition**

    public long getLastAudioPTS()

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getLastVideoPTS () returns a timestamp. Otherwise, -1 is returned. All errors are provided in callback status.

**Remarks**
 
Provide the timestamp for last audio sample is sent by streaming module by network. It is used for only streaming module in case if mode is Publish RTMP.

**Examples**

    Int a_pts = capturer. getLastAudioPTS () ;

#### getStatReconnectCount
Provide  the  number  or  reconnections  to  RTMP  server  that  happened  from  the streaming start.

**Definition**

    public long getStatReconnectCount() 

**Parameters**
 
There are no parameters for this call.

**Return Value**
 
Upon successful completion, getStatReconnectCount returns number of reconnection. Otherwise, -1 is returned. All errors are provided in the callback status.

**Remarks**
 
Provide the number or reconnections to RTMP server that happened from streaming start. It is used for only streaming module in case if mode is Publish RTMP.

**Examples**

    Int a_pts = capturer. getStatReconnectCount () ;

#### getRecordProperties

|Name|	Description|	Function|	Type|
| ------ | ------ | ------ | ------ |
|PP_RECORD_STAT_D URATION	|Get current recording file duration in milliseconds	|getPropLong(PP_RECORD_STAT_ DURATION)	|long
|PP_RECORD_STAT_D URATION_TOTAL	|Get total recording duration in milliseconds	|getPropLong(PP_RECORD_STAT_ DURATION_TOTAL)	|long
|PP_RECORD_STAT_SI ZE	|Get current recording file size in bytes	|getPropLong(PP_RECORD_STAT_ SIZE)	|long
|PP_RECORD_STAT_SI ZE_TOTAL	|Get total recorded bytes	|getPropLong(PP_RECORD_STAT_ SIZE_TOTAL)	|long
|PP_RECORD_STAT_FI LE_NAME	|Get current recording file name	|getPropString(PP_RECORD_STAT_FILE_NAME) |String
|PP_RECORD_STAT_FI LE_NAME_STOPPED	|Get file name of file just recorded	|getPropString(PP_RECORD_STAT_FILE_NAME_STOPPED)	|String

