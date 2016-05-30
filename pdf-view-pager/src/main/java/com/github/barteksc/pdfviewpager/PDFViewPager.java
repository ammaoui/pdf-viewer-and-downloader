/*
 * Original work Copyright (C) 2016 Olmo Gallegos Hernández.
 * Modified work Copyright (C) 2016 Bartosz Schiller.
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
package com.github.barteksc.pdfviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.github.barteksc.pdfviewpager.adapter.PDFPagerAdapter;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class PDFViewPager extends VerticalViewPager {
    protected Context context;

    protected float pageScale = 1.0f;

    public PDFViewPager(Context context, String pdfPath) {
        super(context);
        this.context = context;
        init(pdfPath);
    }

    public PDFViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    protected void init(String pdfPath) {
        initAdapter(context, pdfPath, 1.0f);
    }

    protected void init(AttributeSet attrs) {
        if (isInEditMode()) {
            setBackgroundResource(R.drawable.flaticon_pdf_dummy);
            return;
        }

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PDFViewPager);
            String assetFileName = a.getString(R.styleable.PDFViewPager_assetFileName);

            float scale = a.getFloat(R.styleable.PDFViewPager_pageBitmapScale, 1.0f);

            if (assetFileName != null && assetFileName.length() > 0) {
                initAdapter(context, assetFileName, scale);
            }
            a.recycle();
        }
    }

    protected void initAdapter(Context context, String pdfPath, float pageScale) {
        setAdapter(new PDFPagerAdapter(context, pdfPath, pageScale));
    }

    public float getPageBitmapScale() {
        float sc = pageScale;
        if(getAdapter() != null) {
            sc = ((PDFPagerAdapter) getAdapter()).getPageBitmapScale();
        }
        return sc;
    }

    public void setPageBitmapScale(float scale) {
        pageScale = scale;
        if(getAdapter() != null) {
            ((PDFPagerAdapter) getAdapter()).setPageBitmapScale(scale);
        }
    }
}
