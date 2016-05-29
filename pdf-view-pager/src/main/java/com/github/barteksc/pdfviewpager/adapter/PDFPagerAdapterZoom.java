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
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewpager.R;

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

        WeakReference<PhotoViewAttacher> attacherRef = attachers.get(position);
        if (attacherRef != null) {
            PhotoViewAttacher attacher = attacherRef.get();
            if (attacher != null) {
                attacher.cleanup();
            }
            attachers.remove(position);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View v = inflater.inflate(R.layout.view_pdf_page, container, false);
        final ImageView iv = (ImageView) v.findViewById(R.id.imageView);

        if (!pdfiumReady() || getCount() < position)
            return v;

        PhotoViewAttacher attacher = new PhotoViewAttacher(iv, pdfiumCore, pdfDocument, position);
        attachers.put(position, new WeakReference<>(attacher));

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
