package org.rtspplayer.sample.util;


import org.rtspplayer.sample.activity.MainActivity;

/**
 * Created by alexey on 02.02.17.
 */
public class OnSwipeTouchImplementation extends OnSwipeTouchListener {
    private MainActivity act;
    GestureActions gestureActions;
    public OnSwipeTouchImplementation(MainActivity activity, int num)
    {
        super(true);
        act=activity;
        gestureActions=new GestureActions(activity,num);

    }
    @Override
    public void swipeRight(int x, int y)
    {
        gestureActions.swipeRight(x,y);

    }
    @Override
    public void swipeLeft(int x, int y)
    {
        gestureActions.swipeLeft(x,y);
    }








    @Override
    public void scrollUp()
    {
        gestureActions.scrollUp();
    }
    @Override
    public void scrollDown()
    {
        gestureActions.scrollDown();
    }
    @Override
    public void singleTap(int x, int y)
    {
        gestureActions.singleTap(x, y);
    }


    @Override
    public void doubleTap(int x, int y)
    {
        gestureActions.doubleTap(x, y);
    }

    // Zooming

    @Override
    public void pinchMove(boolean isGrow)
    {
        gestureActions.pinchMove(isGrow);
    }
    @Override
    public void touchDown(int x, int y)
    {
        // Reset parameters
        gestureActions.touchDown(x, y);
    }

    @Override
    public void touchMove(int x, int y)
    {
        gestureActions.touchMove(x, y);
    }

}
