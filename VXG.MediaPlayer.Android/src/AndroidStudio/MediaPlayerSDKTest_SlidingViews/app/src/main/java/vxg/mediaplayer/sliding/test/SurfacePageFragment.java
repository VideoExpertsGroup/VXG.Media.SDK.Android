/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vxg.mediaplayer.sliding.test;

import java.nio.ByteBuffer; 
import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import veg.mediaplayer.sdk.DebugGuard;

public class SurfacePageFragment extends Fragment implements SurfaceHolder.Callback2 
{
	public abstract interface SurfacePageFragmentCallback
	{
		public void onSurfaceCreated(int position, Surface surface);
		public void onSurfaceChanged(int position, Surface surface, int newWidth, int newHeight);
		public void onSurfaceDestroyed(int position, Surface surface);
		public void onSurfaceTouched(int position);
	}

	public static final String ARG_PAGE = "page";

    private int mPageNumber;
    private int backColor;

    private SurfaceView curView = null;
	private FrameLayout layoutSurfacePage = null;
    private TextView statusText = null;
    private Surface curSurface = null;
    private SurfacePageFragmentCallback callback = null;
    public static SurfacePageFragment create(int pageNumber, final SurfacePageFragmentCallback callback) 
    {
        SurfacePageFragment fragment = new SurfacePageFragment();
        fragment.setPageFragment(pageNumber);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
    	fragment.callback = callback;

        return fragment;
    }

    public SurfacePageFragment() 
    {
    	super();
    }

    private void setPageFragment(int pageNumber) 
    {
		switch (pageNumber)
		{
			case 0: backColor = Color.argb(255, 66, 66, 66);break;
			case 1: backColor = Color.argb(255, 77, 77, 77);break;
			case 2: backColor = Color.argb(255, 88, 88, 88);break;
			case 3: backColor = Color.argb(255, 99, 99, 99);break;
			default : break;
		}
	
		Log.d(TAG, "SurfacePageFragment:SurfacePageFragment: cl: " + pageNumber + " backColor:" + backColor);
	
    }


    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        //Random rnd = new Random();
        //backColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.surface_page, container, false);

		Log.d(TAG, "SurfacePageFragment:onCreateView:  "  + backColor);

//        // Set the title view to show the page number.
        curView = (SurfaceView) rootView.findViewById(R.id.surfaceView);
		layoutSurfacePage = (FrameLayout) rootView.findViewById(R.id.layoutSurfacePage);


        curView.getHolder().addCallback(this);
        curView.setBackgroundColor(backColor);
        curView.setZOrderOnTop(true);
        curView.setOnTouchListener(new OnTouchListener() 
        {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if (callback != null)
				{
					callback.onSurfaceTouched(mPageNumber);
				}
				return false;
			}
        });        
        statusText = (TextView) rootView.findViewById(R.id.statusText); 
        statusText.setVisibility(View.GONE);
        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() 
    {
        return mPageNumber;
    }

    public Surface getSurface() 
    {
        return curSurface;
    }
    
    public TextView getStatus() 
    {
        return statusText;
    }

    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy: " + mPageNumber);
    }

    private static final String TAG = "MediaPlayerSDKTest.SurfacePageFragment";

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		if (holder == null)
			return;
		
		Log.d(TAG, "surfaceCreated: " + holder.getSurface());
		curSurface = holder.getSurface();
		if (callback != null)
		{
			callback.onSurfaceCreated(mPageNumber, holder.getSurface());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		if (holder == null)
			return;
		
		Log.d(TAG, "surfaceChanged: " + holder.getSurface());
		curSurface = holder.getSurface();
		if (callback != null)
		{
			callback.onSurfaceChanged(mPageNumber, holder.getSurface(), width, height);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		if (holder == null)
			return;
		
		Log.d(TAG, "surfaceDestroyed: " + holder.getSurface());
		if (callback != null)
		{
			callback.onSurfaceDestroyed(mPageNumber, holder.getSurface());
		}
		curSurface = null;
	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		
	}


	public void ChangeSize() 
	{

		final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)layoutSurfacePage.getLayoutParams(); 
		int x = 0 , y = 0, w = 1080, h = 1920;
		int newHeight = 1088;
		int newWidth = 1600;
	
		params.leftMargin	= x;
		params.topMargin	=  newHeight - y - h;
		params.rightMargin	=  (w > newWidth) ? (- w - params.leftMargin + newWidth) : (newWidth - w - params.leftMargin );
		params.bottomMargin =  (h > newHeight) ? (- h - params.topMargin + newHeight) : (newHeight - h - params.topMargin);

	
	  layoutSurfacePage.post(new Runnable()
	  {
		  @Override
		  public void run() 
		  {
			  layoutSurfacePage.requestLayout();
		  }
	  });

	}	

}
