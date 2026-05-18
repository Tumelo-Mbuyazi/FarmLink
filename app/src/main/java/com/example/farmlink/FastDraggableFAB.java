package com.example.farmlink;



import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FastDraggableFAB extends FloatingActionButton {

    private static final String PREFS_NAME = "fab_position";
    private float dX, dY;
    private float startX, startY;
    private long startTime;
    private boolean isDragging;
    private OnClickListener clickListener;
    private boolean positionLoaded = false;

    public FastDraggableFAB(Context context) {
        super(context);
        init();
    }

    public FastDraggableFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FastDraggableFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!positionLoaded) {
            loadOrSetDefaultPosition();
            positionLoaded = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                dX = getX() - event.getRawX();
                dY = getY() - event.getRawY();
                startTime = System.currentTimeMillis();
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(event.getRawX() - startX);
                float deltaY = Math.abs(event.getRawY() - startY);

                if (deltaX > 10 || deltaY > 10) {
                    isDragging = true;
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    ViewParent parent = getParent();
                    if (parent instanceof View) {
                        View parentView = (View) parent;
                        newX = Math.max(0, Math.min(newX, parentView.getWidth() - getWidth()));
                        newY = Math.max(0, Math.min(newY, parentView.getHeight() - getHeight()));
                    }

                    setX(newX);
                    setY(newY);
                }
                return true;

            case MotionEvent.ACTION_UP:
                long duration = System.currentTimeMillis() - startTime;

                if (isDragging) {
                    // Save position after drag
                    savePosition(getX(), getY());
                }

                if (!isDragging && duration < 300) {
                    if (clickListener != null) {
                        clickListener.onClick(this);
                    }
                    performClick();
                }

                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.clickListener = l;
    }

    private void savePosition(float x, float y) {
        getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat("fab_x", x)
                .putFloat("fab_y", y)
                .apply();
    }

    private void loadOrSetDefaultPosition() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        float savedX = prefs.getFloat("fab_x", -1);
        float savedY = prefs.getFloat("fab_y", -1);

        if (savedX != -1 && savedY != -1) {
            // Load saved position
            setX(savedX);
            setY(savedY);
        } else {
            // Set default position: middle-right
            post(() -> {
                ViewParent parent = getParent();
                if (parent instanceof View) {
                    View parentView = (View) parent;
                    int parentWidth = parentView.getWidth();
                    int parentHeight = parentView.getHeight();
                    int fabWidth = getWidth();
                    int fabHeight = getHeight();

                    // Find bottom navigation height
                    int bottomNavHeight = 0;
                    View bottomNav = parentView.findViewById(R.id.bottomNavigation);
                    if (bottomNav != null) {
                        bottomNavHeight = bottomNav.getHeight();
                    }

                    // Middle-right position (20dp from right edge)
                    float defaultX = parentWidth - fabWidth - 40;
                    float defaultY = (parentHeight - fabHeight - bottomNavHeight) / 2f;

                    setX(defaultX);
                    setY(defaultY);
                }
            });
        }
    }
}