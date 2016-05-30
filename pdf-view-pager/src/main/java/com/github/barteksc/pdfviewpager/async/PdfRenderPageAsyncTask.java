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

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.lang.ref.WeakReference;

public class PdfRenderPageAsyncTask extends AsyncTask<Void, Void, Void> {

    private final static int MAX_DIMENSION = 1700;

    private WeakReference<ImageView> imageViewRef;
    private Bitmap bitmap;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private int position;
    private float pageScale = 1.0f;
    private OnPdfPageRenderListener listener;

    public PdfRenderPageAsyncTask(ImageView imageView, int position, PdfiumCore pdfiumCore, PdfDocument pdfDocument, float pageScale) {
        imageViewRef = new WeakReference<>(imageView);
        this.position = position;
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageScale = pageScale;
    }

    @Override
    protected Void doInBackground(Void... params) {
        pdfiumCore.openPage(pdfDocument, position);

        int width = pdfiumCore.getPageWidth(pdfDocument, position);
        int height = pdfiumCore.getPageHeight(pdfDocument, position);


        if (width > height && width > MAX_DIMENSION) {
            float aspectRatio = width /
                    (float) height;
            width = MAX_DIMENSION;
            height = Math.round(width / aspectRatio);
        } else if (height > MAX_DIMENSION) {
            float aspectRatio = height /
                    (float) width;
            height = MAX_DIMENSION;
            width = Math.round(height / aspectRatio);
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, position, 0, 0,
                width, height);

        return null;
    }

    @Override
    protected void onPostExecute(Void o) {
        if (imageViewRef.get() != null) {
            imageViewRef.get().setImageBitmap(bitmap);
            if (listener != null) {
                listener.onPageRendered(position, imageViewRef.get(), bitmap);
            }
        }
    }

    @Override
    public void onCancelled(Void result) {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public PdfRenderPageAsyncTask setOnPdfPageRenderListener(OnPdfPageRenderListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnPdfPageRenderListener {
        void onPageRendered(int position, ImageView imageView, Bitmap bitmap);
    }
}
