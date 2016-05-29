/*
 * Copyright (C) 2016 Bartosz Schiller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewpager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.barteksc.pdfviewpager.R;
import com.github.barteksc.pdfviewpager.util.Util;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class ScrollBar extends View implements ViewPager.OnPageChangeListener {

    private static final String TAG = ScrollBar.class.getSimpleName();

    private int handleColor = 0;
    private int indicatorColor = 0;
    private int indicatorTextColor = 0;

    private Paint handlePaint;
    private float handleHeight = 0;
    private int viewWidth;
    private VerticalViewPager viewPager;
    private PointF handlePos;
    private ViewPager.OnPageChangeListener viewPagerListener;
    private int viewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private int currentPage = 0;
    private ScrollBarPageIndicator indicator;

    private boolean programmaticPageChangeLocked = false;

    public ScrollBar(Context context) {
        super(context);
        init();
    }

    public ScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
        init();
    }

    public ScrollBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
        init();
    }

    private void init() {

        indicator = new ScrollBarPageIndicator(getContext());
        setIndicatorPage(currentPage);
        indicator.setBackgroundColor(indicatorColor);
        indicator.setTextColor(indicatorTextColor);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                indicator.addToScrollBar(ScrollBar.this);
                ScrollBar.this.removeOnLayoutChangeListener(this);
            }
        });

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(handleColor);

        if (getBackground() == null) {
            setBackgroundColor(Color.LTGRAY);
        }

        handlePos = new PointF(0, 0);

        viewWidth = Util.getDP(getContext(), 30);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ScrollBar, defStyleAttr, 0);

        try {
            handleColor = a.getColor(R.styleable.ScrollBar_sb_handleColor, Color.parseColor("#FF4081"));
            indicatorColor = a.getColor(R.styleable.ScrollBar_sb_indicatorColor, Color.parseColor("#FF4081"));
            indicatorTextColor = a.getColor(R.styleable.ScrollBar_sb_indicatorTextColor, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    public void setVerticalViewPager(VerticalViewPager viewPager) {
        if (this.viewPager == viewPager || viewPager == null) {
            return;
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);
        invalidate();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        viewPagerListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isViewPagerReady()) {
            calculateHandleHeight();
            calculateHandlePosByPage(currentPage);
        }
    }

    float getHandleHeight() {
        return handleHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + viewWidth;
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        int minh = MeasureSpec.getSize(heightMeasureSpec) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            canvas.drawRect(0, 0, getWidth(), Util.getDP(getContext(), 40), handlePaint);
            return;
        } else if (!isViewPagerReady()) {
            return;
        }

        calculateHandleHeight();

        canvas.drawRect(handlePos.x, handlePos.y, getWidth(), handlePos.y + handleHeight, handlePaint);

    }

    private void calculateHandlePosByPage(int position) {
        handlePos.y = position * handleHeight;
    }

    private void calculateHandleHeight() {
        handleHeight = getHeight() / (float) viewPager.getAdapter().getCount();
    }

    private boolean isViewPagerReady() {
        return viewPager != null && viewPager.getAdapter() != null;
    }

    private int getPagesCount() {
        return isViewPagerReady() ? viewPager.getAdapter().getCount() : 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_POINTER_DOWN:
                float y = event.getY();
                if (y < 0 || y > getHeight())
                    return true;

                int pageNum = (int) Math.floor(y / handleHeight);

                float handleY = pageNum * handleHeight;
                if (handleY < 0) {
                    handleY = 0;
                } else if (y + handleHeight / 2 > getHeight()) {
                    handleY = getHeight() - handleHeight;
                }
                handlePos.y = handleY;

                if (pageNum != currentPage) {
                    indicator.setPageNum(pageNum + 1);
                }
                currentPage = pageNum;
                indicator.setVisibility(VISIBLE);
                indicator.setScroll(handleY);
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                int pgNum = (int) Math.floor(event.getY() / handleHeight);
                programmaticPageChangeLocked = true;
                viewPager.setCurrentItem(pgNum);
                currentPage = pgNum;
                indicator.setVisibility(INVISIBLE);
                invalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!programmaticPageChangeLocked) {
            currentPage = position;
            handlePos.y = position * handleHeight + positionOffset * handleHeight;

            invalidate();
        }

        if (viewPagerListener != null) {
            viewPagerListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (!programmaticPageChangeLocked) {
            //if(viewPagerScrollState == ViewPager.SCROLL_STATE_IDLE) {
            currentPage = position;
            calculateHandlePosByPage(position);
            invalidate();
            //}
        }

        if (viewPagerListener != null) {
            viewPagerListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

        if (programmaticPageChangeLocked && state == ViewPager.SCROLL_STATE_IDLE) {
            programmaticPageChangeLocked = false;
        }

        if (!programmaticPageChangeLocked) {
            viewPagerScrollState = state;
        }

        if (viewPagerListener != null) {
            viewPagerListener.onPageScrollStateChanged(state);
        }
    }

    public void setCurrentItem(int position) {
        if(!isViewPagerReady()) {
            throw new IllegalStateException("ViewPager not set");
        }
        programmaticPageChangeLocked = true;
        currentPage = position;
        viewPager.setCurrentItem(position);
        invalidate();
    }

    private void setIndicatorPage(int position) {
        indicator.setPageNum(position + 1);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPage = savedState.currentPage;
        setIndicatorPage(currentPage);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = currentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
