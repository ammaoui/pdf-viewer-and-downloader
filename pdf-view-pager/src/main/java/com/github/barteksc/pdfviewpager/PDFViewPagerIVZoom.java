/*
 * Copyright (C) 2016 Olmo Gallegos Hernández.
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
import android.util.AttributeSet;

import com.github.barteksc.pdfviewpager.adapter.PDFPagerAdapterIVZoom;


public class PDFViewPagerIVZoom extends PDFViewPager {
    public PDFViewPagerIVZoom(Context context, String pdfPath) {
        super(context, pdfPath);
    }

    public PDFViewPagerIVZoom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initAdapter(Context context, String pdfPath) {
        setAdapter(new PDFPagerAdapterIVZoom(context, pdfPath));
    }
}
