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
package com.github.barteksc.pdfviewpager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewpager.R;
import com.github.barteksc.pdfviewpager.async.PdfRenderPageAsyncTask;

import java.lang.ref.WeakReference;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PDFPagerAdapterZoom extends PDFPagerAdapter {

    SparseArray<WeakReference<PhotoViewAttacher>> attachers;

    public PDFPagerAdapterZoom(Context context, String pdfPath) {
        super(context, pdfPath);
        attachers = new SparseArray<>();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.view_pdf_page, container, false);
        final ImageView iv = (ImageView) v.findViewById(R.id.imageView);

        if (!pdfiumReady() || getCount() < position)
            return v;

        executeRenderTask(iv, position, new PdfRenderPageAsyncTask.OnPdfPageRenderListener() {
            @Override
            public void onPageRendered(int position, ImageView imageView, Bitmap bitmap) {
                PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
                attachers.put(position, new WeakReference<>(attacher));
                bitmaps.put(position, new WeakReference<>(bitmap));
                attacher.update();
            }
        });

        container.addView(v, 0);

        return v;
    }

    @Override
    public void close() {
        super.close();
        if (attachers != null) {
            attachers.clear();
            attachers = null;
        }
    }

}
