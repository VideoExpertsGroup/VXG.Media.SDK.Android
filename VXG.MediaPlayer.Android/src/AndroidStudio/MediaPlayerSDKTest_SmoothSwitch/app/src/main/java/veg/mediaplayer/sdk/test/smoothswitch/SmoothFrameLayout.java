/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package veg.mediaplayer.sdk.test.smoothswitch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
 
public class SmoothFrameLayout extends FrameLayout {

	public SurfaceView needForRemove = null;
	public SurfaceView needForAdd = null;
	
    public SmoothFrameLayout(Context context) {
		super(context);
	}

    public SmoothFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    public SmoothFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
  		Log.e(TAG, "onLayout " + changed + ", " + left + ", " + top + ", " + right + ", " + bottom);
  		if (needForRemove != null)
  		{
  			removeViewInLayout(needForRemove);
  			needForRemove = null;
  		}

  		if (needForAdd != null)
  		{
  			addViewInLayout(needForAdd, -1, needForAdd.getLayoutParams(), true);
  			needForAdd = null;
  		}
  		
        super.onLayout(changed, left, top, right, bottom);
    }	    
    
	private static final String TAG  = "MediaPlayerTest.SmoothSwitch";
    
}