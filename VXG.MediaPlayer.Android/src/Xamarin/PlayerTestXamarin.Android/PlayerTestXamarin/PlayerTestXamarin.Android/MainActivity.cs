using System;
using Android.App;
using Android.Graphics;
using Android.OS;
using Android.Util;
using Android.Widget;
using Java.Nio;
using Veg.Mediaplayer.Sdk;

namespace PlayerTestXamarin.Droid
{
    [Activity(Label = "PlayerTestXamarin.Android", MainLauncher = true, Icon = "@drawable/icon")]
    public class MainActivity : Activity, MediaPlayer.IMediaPlayerCallback
    {
        private const string Url = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";

        private MediaPlayer _mediaPlayer;

        protected override void OnCreate(Bundle bundle)
        {
            base.OnCreate(bundle);

            SetContentView(Resource.Layout.Main);

            _mediaPlayer = FindViewById<MediaPlayer>(Resource.Id.playerView);
            _mediaPlayer.SurfaceView.SetZOrderOnTop(true);

            // Not sure if this is not redundant
            var trackHolder = _mediaPlayer.SurfaceView.Holder;
            trackHolder.SetFormat(Format.Transparent);

            var connectButton = FindViewById<Button>(Resource.Id.connectButton);
            connectButton.Click += ConnectButton_Click;
        }

        private void ConnectButton_Click(object sender, EventArgs e)
        {
            _mediaPlayer.Open(new MediaPlayerConfig
            {
                ConnectionUrl = Url,
                ConnectionNetworkProtocol = -1,
                ConnectionBufferingTime = 300,
                ConnectionDetectionTime = 1000,
                DecodingType = 1,
                RendererType = 1,
                SynchroEnable = 1,
                SynchroNeedDropVideoFrames = 1,
                EnableColorVideo = 1,
                EnableAspectRatio = 1,
                DataReceiveTimeout = 30000,
                NumberOfCPUCores = 0
            }, this);
        }

        protected override void OnPause()
        {
            base.OnPause();

            _mediaPlayer?.OnPause();
        }

        protected override void OnStart()
        {
            base.OnStart();

            _mediaPlayer?.OnStart();
        }

        protected override void OnStop()
        {
            base.OnStop();

            _mediaPlayer?.OnStop();
        }

        public override void OnBackPressed()
        {
            base.OnBackPressed();

            _mediaPlayer?.Close();
        }

        public override void OnWindowFocusChanged(bool hasFocus)
        {
            base.OnWindowFocusChanged(hasFocus);

            _mediaPlayer?.OnWindowFocusChanged(hasFocus);
        }

        public override void OnLowMemory()
        {
            base.OnLowMemory();

            _mediaPlayer?.OnLowMemory();
        }

        protected override void OnDestroy()
        {
            base.OnDestroy();

            _mediaPlayer?.OnDestroy();
        }

        public int OnReceiveData(ByteBuffer p0, int p1, long p2)
        {
            Log.Error("PlayerTestXamarin", "Form Native Player OnReceiveData: size: " + p1 + ", pts: " + p2);

            return 0;
        }

        public int Status(int p0)
        {
            Log.Error("PlayerTestXamarin", "From Native Player status: " + p0);

            return 0;
        }
    }
}
