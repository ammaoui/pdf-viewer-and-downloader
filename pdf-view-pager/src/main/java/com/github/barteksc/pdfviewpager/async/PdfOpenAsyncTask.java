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
package com.github.barteksc.pdfviewpager.async;

import android.content.Context;
import android.os.AsyncTask;

import com.github.barteksc.pdfviewpager.PDFViewPager;
import com.github.barteksc.pdfviewpager.adapter.PDFPagerAdapterZoom;
import com.github.barteksc.pdfviewpager.util.FileUtil;

import java.io.File;
import java.io.IOException;



public class PdfOpenAsyncTask extends AsyncTask<Void, Void, Void> {

    private PDFPagerAdapterZoom pagerAdapterZoom;

    private PDFViewPager pdfViewPager;
    private Context context;
    private String path;
    private OnPdfLoadListener listener;

    public PdfOpenAsyncTask(PDFViewPager pdfViewPager, String path) {
        this.pdfViewPager = pdfViewPager;
        this.context = pdfViewPager.getContext();
        this.path = path;
    }

    public PdfOpenAsyncTask setOnPdfLoadListener(OnPdfLoadListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            FileUtil.copyAsset(context, path, new File(context.getCacheDir(), path).getAbsolutePath());
            pagerAdapterZoom = new PDFPagerAdapterZoom(context, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        pdfViewPager.setAdapter(pagerAdapterZoom);
        if (listener != null) {
            listener.onPdfLoaded();
        }
    }

    @Override
    protected void onCancelled() {

    }

    public interface OnPdfLoadListener {
        void onPdfLoaded();
    }
}
