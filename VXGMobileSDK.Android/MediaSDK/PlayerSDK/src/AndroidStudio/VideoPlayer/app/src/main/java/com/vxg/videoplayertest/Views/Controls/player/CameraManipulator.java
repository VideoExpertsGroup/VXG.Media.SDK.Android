package com.vxg.videoplayertest.Views.Controls.player;


import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vxg.videoplayertest.Listeners.CameraManipulatorListener;
import com.vxg.videoplayertest.R;

public class CameraManipulator {
    private ViewGroup mParent;
    private View btnLeft;
    private View btnRight;
    private View btnUp;
    private View btnDown;
    private View btnZoomIn;
    private View btnZoomOut;
    private FrameLayout container;

    public void setListener(CameraManipulatorListener cameraManipulatorListener) {
        this.listener = cameraManipulatorListener;
    }

    public boolean isVisible(){
        return container.getVisibility() ==View.VISIBLE;
    }

    public void hide(){
        container.setVisibility(View.INVISIBLE);
    }

    private CameraManipulatorListener listener;

    public CameraManipulator(ViewGroup activity) {
        mParent = activity;
        initViews();
        setOnClickListeners();
    }

    public void setPosition(int x,int y){
      FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)container.getLayoutParams();
      params.leftMargin = x;
      params.topMargin = y;
      container.setLayoutParams(params);
    }

    private void initViews() {
        container = mParent.findViewById(R.id.container);
        btnLeft = mParent.findViewById(R.id.btnLeft);
        btnRight = mParent.findViewById(R.id.btnRight);
        btnUp = mParent.findViewById(R.id.btnUp);
        btnDown = mParent.findViewById(R.id.btnDown);
        btnZoomIn = mParent.findViewById(R.id.btnZoomIn);
        btnZoomOut = mParent.findViewById(R.id.btnZoomOut);

    }

    private void disable(){
        container.setVisibility(View.GONE);
    }

    public void show(){
        container.setVisibility(View.VISIBLE);
    }

	
    private void setOnClickListeners() {
	/*	
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLeft();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRight();
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUp();
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDown();
            }
        });

        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onZoomIn();
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onZoomOut();
            }
        });
	*/

	btnLeft.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onLeft();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});

	btnRight.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onRight();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});

	
	btnUp.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onUp();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});

	
	btnDown.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onDown();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});

	
	btnZoomIn.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onZoomIn();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});

	
	btnZoomOut.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
	
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onZoomOut();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onStop();
					break;
			}
			return true;
		}
	});
	


    }
	
    private void onLeft() {
        if (listener != null) {
            listener.onLeft();
        }
    }

    private void onRight() {
        if (listener != null) {
            listener.onRight();
        }
    }

    private void onUp() {
        if (listener != null) {
            listener.onUp();
        }
    }

    private void onDown() {
        if (listener != null) {
            listener.onDown();
        }
    }

    private void onZoomIn(){
        if (listener != null) {
            listener.onZoomIn();
        }
    }

    private void onZoomOut(){
        if (listener != null) {
            listener.onZoomOut();
        }
    }

    private void onStop(){
        if (listener != null) {
			listener.onStop();
        }
    }


    private CameraManipulator() {
    }
}
