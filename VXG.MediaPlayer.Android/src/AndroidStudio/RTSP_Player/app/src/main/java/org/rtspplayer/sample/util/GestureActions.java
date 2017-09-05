package org.rtspplayer.sample.util;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;

import veg.mediaplayer.sdk.MediaPlayer.PlayerState;



public class GestureActions {
    public final String TAG="GestureActions";
    private MainActivity act;
    int playerNum =0;
    boolean blocked=false;
    public GestureActions(MainActivity activity, int num){
        act=activity;
        playerNum =num;
    }



    public void swipeRight(int x, int y)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            Log.i(TAG, "=OnSwipeTouchListener swipeRight");
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();

            if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                return;

            if (act.mPanelIsVisible || act.isStartedByIntent)
                return;

            act.playPreviousChannelOrBack();
        }
    }

    public void swipeLeft(int x, int y)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            Log.i(TAG, "=OnSwipeTouchListener swipeLeft");
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();
            if (act.mPanelIsVisible && !act.isStartedByIntent && act.showPreview) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);
                alertDialog.setMessage(act.getResources().getString(R.string.dialog_hide_preview_video));

                alertDialog.setPositiveButton(act.getResources().getString(R.string.dialog_logout_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        act.showPreview = false;
                        act.hidePlayerView();
                        if (act.getPlayer() != null)
                            act.getPlayer().Close();
                        Log.i(TAG, "finish activity");
                        //finish();
                    }
                });

                alertDialog.setNegativeButton(act.getResources().getString(R.string.dialog_logout_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.show();
            }

            if (act.mPanelIsVisible || act.isStartedByIntent)
                return;

            if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                return;

            act.playNextChannelOrBack();
        }
    }



    public void scrollUp() {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            Log.i(TAG, "=OnSwipeTouchListener scrollUp");
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();

            if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                return;

            if (act.mPanelIsVisible)
                return;

            act.getAudio_manager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
    }

    public void scrollDown()
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            Log.i(TAG, "=OnSwipeTouchListener scrollDown");
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();

            if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                return;

            if (act.mPanelIsVisible)
                return;

            act.getAudio_manager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
    }

    public void singleTap(int x, int y)
    {
        if(playerNum==6||playerNum!=6&&act.isFullScreen) {
            Log.i(TAG, "=OnSwipeTouchListener singleTap " + act.getPlayer().getState());
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();
            if (act.isStartedByIntent) {
                act.updatePlayerPanelControl(!act.isPlayerPanelVisible, act.isLocked);
                return;
            }

            if (act.mPanelIsVisible && act.getPlayer() != null && act.getPlayer().getState() == PlayerState.Closed) {
                return;
            }

            if (act.mPanelIsVisible) {
                act.hideControlPanelAndGrid();
                act.mCloseIconsIsVisible = false;
                act.currentList.notifyDataSetChanged();
            } else {
                act.updatePlayerPanelControl(!act.isPlayerPanelVisible, act.isLocked);
               if (SharedSettings.getInstance().AllowFullscreenMode && !act.fourCamerasGridVisible)
                    act.getHider().hide();
            }
        }
            else if(act._2x2camerasData.get(playerNum)!=null) {

                setToFullScreenPlayerLayout();
            }
    }

    long cur_pos = 0;
    private void setToFullScreenPlayerLayout()
    {
        setCurPlayer(playerNum);
        act.updatePlayerPanelControlButtons(act.isLocked, act.getPlayer().getState()==PlayerState.Started?true:false, SharedSettings.getInstance().rendererAspectRatioMode);
        SharedSettings _set = SharedSettings.getInstance();
        _set.setLongValueForKey(act.S_CUR_ID, act._2x2camerasData.get(playerNum).id);
        act.m_cur_item=act._2x2camerasData.get(playerNum);
        _set.savedTabNumForSavedId = _set.selectedTabNum;
        _set.savePrefSettings();
        act.playersResetIfonBackPressedNumber = playerNum;
        act.currentCameraIn2x2Mode = playerNum;
        act.getPlayer().toggleMute(false);

        act.getHider().hide();
        act.getPlayersnames().setVisibility(View.INVISIBLE);
        act.hideProgressView(act.getPlayer());
        act.fourCamerasGridVisible = false;

        act.hideGridSeparators();
        setRows();


        LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams)act.getPlayer().getLayoutParams();

        lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
        act.getPlayer().setLayoutParams(lpc);

        setsetZOrder();

        setFullScreenPlayer();

        act.isFullScreen = true;

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        act.getHider().setup();
        act.getHider().getAnchorView().setSystemUiVisibility(uiOptions);
        act.getLayoutPlayerPanel().setVisibility(View.VISIBLE);
    }
    private void setRow1(LinearLayout playerRow0,LinearLayout playerRow1)
    {
        LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
        parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
        parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
        playerRow0.setLayoutParams(parent);

        LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
        parent1.height = 0;
        parent1.width = 0;
        parent1.weight = (float) 0.0;
        playerRow1.setLayoutParams(parent1);
    }
    private void setRow2(LinearLayout playerRow0,LinearLayout playerRow1)
    {
        LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
        parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
        parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
        parent.weight = (float) 1.0;
        playerRow1.setLayoutParams(parent);

        LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
        parent1.height = 0;
        parent1.width = 0;
        parent1.weight = (float) 0.0;
        playerRow0.setLayoutParams(parent1);
    }
    private void setRows()
    {
        final LinearLayout playerRow0 = (LinearLayout) act.findViewById(R.id.playerRow0);

        final LinearLayout playerRow1 = (LinearLayout) act.findViewById(R.id.playerRow1);

        switch(playerNum)
        {
            case 0:
            case 1:
                setRow1(playerRow0,playerRow1);
                break;
            case 2:
            case 3:
                setRow2(playerRow0,playerRow1);
                break;

        }
    }
    private void setFullScreenPlayer()
    {
        switch(playerNum)
        {
            case 0:
                act.playerFullScreen = act.playerFullScreen.player1;
                break;
            case 1:
                act.playerFullScreen = act.playerFullScreen.player2;
                break;
            case 2:
                act.playerFullScreen = act.playerFullScreen.player3;
                break;
            case 3:
                act.playerFullScreen = act.playerFullScreen.player4;
                break;

        }
    }
    private void setsetZOrder()
    {
        switch(playerNum)
        {
            case 0:
                act.getPlayer1().getSurfaceView().setZOrderOnTop(false);
                act.getPlayer2().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer3().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer4().getSurfaceView().setZOrderOnTop(true);
                break;
            case 1:
                act.getPlayer1().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer2().getSurfaceView().setZOrderOnTop(false);
                act.getPlayer3().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer4().getSurfaceView().setZOrderOnTop(true);
                break;
            case 2:
                act.getPlayer1().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer2().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer3().getSurfaceView().setZOrderOnTop(false);
                act.getPlayer4().getSurfaceView().setZOrderOnTop(true);
                break;
            case 3:
                act.getPlayer1().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer2().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer3().getSurfaceView().setZOrderOnTop(true);
                act.getPlayer4().getSurfaceView().setZOrderOnTop(false);
                break;

        }
    }
   private void setCurPlayer(int num)
   {
       switch (num)
       {
           case 0:
               act.setPlayer(act.getPlayer1());
               break;
           case 1:
               act.setPlayer(act.getPlayer2());
               break;
           case 2:
               act.setPlayer(act.getPlayer3());
               break;
           case 3:
               act.setPlayer(act.getPlayer4());
               break;
       }
   }
    public void doubleTap(int x, int y)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {

        }
        else if(act._2x2camerasData.get(playerNum)!=null) {

            setToFullScreenPlayerLayout();
        }
    }

    // Zooming
    long t = 0;
    int Growing = 0;

    // Move picture
    long t1 = 0;
    int 	last_x 	= 	-1;
    int 	last_y 	= 	-1;
    int 	diff_x 	= 	-1;
    int 	diff_y 	= 	-1;
    int 	max_x	=	-1;
    int 	max_y	=	-1;
    int 	_x 		= 	50;	// Center
    int 	_y 		= 	50; // Center
    int 	_x_last	=	-1;
    int 	_y_last =	-1;

    public void pinchMove(boolean isGrow)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();

            if (SharedSettings.getInstance().rendererAspectRatioMode != 4)
                return;

            Log.i(TAG, "=OnSwipeTouchListener pinchMove  isGrow:" + isGrow);


            if (isGrow) {
                Growing++;
                if (Growing >= 5) Growing = 5;
            } else {
                Growing--;
                if (Growing <= -5) Growing = -5;
            }

            Log.i(TAG, "=OnSwipeTouchListener pinchMove Growing:" + Growing + "  t:" + t + " t_diff:" + (System.nanoTime() - t));

            if (t != 0 && (System.nanoTime() - t) > /*1000000000*/10000000 /* 10 milliseconds */) {
                if (Growing >= 0) {
                    if ((SharedSettings.getInstance().rendererAspectRatioZoomModePercent + 2) <= 300)
                        SharedSettings.getInstance().rendererAspectRatioZoomModePercent += 2;
                    else
                        SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 300;

                } else {
                    if ((SharedSettings.getInstance().rendererAspectRatioZoomModePercent - 2) >= 25)
                        SharedSettings.getInstance().rendererAspectRatioZoomModePercent -= 2;
                    else
                        SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 25;
                }
//						SharedSettings.getInstance().rendererAspectRatioMoveModeX = -1;
//						SharedSettings.getInstance().rendererAspectRatioMoveModeY = -1;
                act.getPlayer().getConfig().setAspectRatioMoveModeX(SharedSettings.getInstance().rendererAspectRatioMoveModeX);
                act.getPlayer().getConfig().setAspectRatioMoveModeY(SharedSettings.getInstance().rendererAspectRatioMoveModeY);
                act.getPlayer().getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                act.getPlayer().getConfig().setAspectRatioMode(SharedSettings.getInstance().rendererAspectRatioMode);
                act.getPlayer().UpdateView();
                Log.i(TAG, "=OnSwipeTouchListener pinchMove ZoomModePercent:" + SharedSettings.getInstance().rendererAspectRatioZoomModePercent + "  t:" + t + " t_diff:" + (System.nanoTime() - t));
                t = System.nanoTime();
            } else if (t == 0) {
                t = System.nanoTime();
            }
        }
    }

    public void touchDown(int x, int y)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {
            last_x = -1;
            last_y = -1;
            diff_x = -1;
            diff_y = -1;
            _x_last = -1;
            _y_last = -1;
        }
    }



    public void touchMove(int x, int y)
    {
        blocked=!(playerNum==6||playerNum!=6&&act.isFullScreen);
        if(!blocked) {

            if (act.isLocked)
                return;

            act.refreshPlayerPanelControlVisibleTimer();

            if (SharedSettings.getInstance().rendererAspectRatioMode != 4)
                return;

            if (-1 == max_x || -1 == max_y) {
                Display d = ((WindowManager) act.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                max_x = d.getWidth();
                max_y = d.getHeight();
            }

            Log.i(TAG, "=OnSwipeTouchListener touchMove x:" + x + " y:" + y + " t:" + t1 + " t_diff:" + (System.nanoTime() - t1));

            if (true /*t1 != 0 && (System.nanoTime() - t) > /*1000000000*10000000 /* 10 milliseconds */) {
                if (last_x != -1 && Math.abs(last_x - x) < (max_x / 10)) {
                    diff_x = last_x - x;
                    _x += diff_x * 100 / (max_x / 1.5);

                    if (_x > 100) _x = 100;
                    if (_x < 0) _x = 0;
                }
                last_x = x;

                if (last_y != -1 && Math.abs(last_y - y) < (max_y / 10)) {
                    diff_y = last_y - y;
                    _y -= diff_y * 100 / (max_y / 2.5);
                    if (_y > 100) _y = 100;
                    if (_y < 0) _y = 0;
                }
                last_y = y;

                Log.i(TAG, "=OnSwipeTouchListener touchMove1 _x:" + _x + " diff_x:" + diff_x + " last_x:" + last_x + " max_x:" + max_x);
                Log.i(TAG, "=OnSwipeTouchListener touchMove1 _y:" + _y + " diff_y:" + diff_y + " last_y:" + last_y + " max_y:" + max_y);


                if (_x_last != _x || _y_last != _y) {
                    SharedSettings.getInstance().rendererAspectRatioMoveModeX = _x;
                    SharedSettings.getInstance().rendererAspectRatioMoveModeY = _y;
                    act.getPlayer().getConfig().setAspectRatioMoveModeX(SharedSettings.getInstance().rendererAspectRatioMoveModeX);
                    act.getPlayer().getConfig().setAspectRatioMoveModeY(SharedSettings.getInstance().rendererAspectRatioMoveModeY);
                    act.getPlayer().getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    act.getPlayer().getConfig().setAspectRatioMode(5);
                    act.getPlayer().UpdateView();
                    t1 = System.nanoTime();
                }

                _x_last = _x;
                _y_last = _y;
            } else if (t1 == 0) {
                t1 = System.nanoTime();
            }
        }
    }
}
