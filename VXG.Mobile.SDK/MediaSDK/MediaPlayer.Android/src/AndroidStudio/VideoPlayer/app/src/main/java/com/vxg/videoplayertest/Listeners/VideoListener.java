package com.vxg.videoplayertest.Listeners;



public interface VideoListener {
    void onVideoReady();
    void onVideoPlayed();
    void onVideoPaused();
    void onVideoClosed();
}
