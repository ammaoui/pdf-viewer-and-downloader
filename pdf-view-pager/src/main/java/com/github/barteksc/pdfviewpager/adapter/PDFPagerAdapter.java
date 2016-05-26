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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewpager.R;
import com.github.barteksc.pdfviewpager.async.PdfRenderPageAsyncTask;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class PDFPagerAdapter extends PagerAdapter {
    String pdfPath;
    Context context;
    PdfiumCore pdfiumCore;
    PdfDocument pdfDocument;

    SparseArray<WeakReference<Bitmap>> bitmaps;
    SparseArray<WeakReference<PdfRenderPageAsyncTask>> asyncTasks;
    LayoutInflater inflater;

    int maxTextureSize;

    public PDFPagerAdapter(Context context, String pdfPath) {
        this.pdfPath = pdfPath;
        this.context = context;
        bitmaps = new SparseArray<>();
        asyncTasks = new SparseArray<>();
        init();
    }

    protected void init() {
        try {
            maxTextureSize = getMaxTextureSize();
            pdfiumCore = new PdfiumCore(context);
            pdfDocument = pdfiumCore.newDocument(getSeekableFileDescriptor(pdfPath));
            inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PDFPagerAdapter", e.getMessage());
        }
    }

    protected FileDescriptor getSeekableFileDescriptor(String path) throws IOException {
        ParcelFileDescriptor pfd;

        File pdfCopy = new File(path);
        if (pdfCopy.exists()) {
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd.getFileDescriptor();
        }

        if (isAnAsset(path)) {
            pdfCopy = new File(context.getCacheDir(), path);
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
        } else {
            URI uri = URI.create(String.format("file://%s", path));
            pfd = context.getContentResolver().openFileDescriptor(Uri.parse(uri.toString()), "rw");
        }

        return pfd.getFileDescriptor();
    }

    private boolean isAnAsset(String path) {
        return !path.startsWith("/");
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.view_pdf_page, container, false);
        ImageView iv = (ImageView) v.findViewById(R.id.imageView);

        if (!pdfiumReady() || getCount() < position)
            return v;

        executeRenderTask(iv, position, new PdfRenderPageAsyncTask.OnPdfPageRenderListener() {
            @Override
            public void onPageRendered(int position, ImageView imageView, Bitmap bitmap) {
                bitmaps.put(position, new WeakReference<>(bitmap));
                imageView.setImageBitmap(bitmap);
            }
        });

        container.addView(v, 0);

        return v;
    }

    protected void executeRenderTask(ImageView imageView, int position, PdfRenderPageAsyncTask.OnPdfPageRenderListener listener) {
        PdfRenderPageAsyncTask task = new PdfRenderPageAsyncTask(imageView, position, pdfiumCore, pdfDocument).setOnPdfPageRenderListener(listener);
        asyncTasks.put(position, new WeakReference<>(task));
        task.execute();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        WeakReference<Bitmap> bitmapRef = bitmaps.get(position);
        if (bitmapRef != null) {
            recycleBitmap(position);
        }
        cancelRenderTask(position);
    }

    public void close() {
        releaseAllBitmaps();
        cancelAllRenderTasks();
        if (pdfiumReady()) {
            pdfiumCore.closeDocument(pdfDocument);
        }
    }

    protected void releaseAllBitmaps() {
        for (int i = 0; bitmaps != null && i < bitmaps.size(); ++i) {
            recycleBitmap(bitmaps.keyAt(i));
        }
        bitmaps.clear();
    }

    protected void recycleBitmap(int position) {
        Bitmap b = bitmaps.get(position).get();
        if (b != null && !b.isRecycled()) {
            b.recycle();
            bitmaps.remove(position);
        }
    }

    protected void cancelAllRenderTasks() {
        for (int i = 0; asyncTasks != null && i < asyncTasks.size(); ++i) {
            cancelRenderTask(asyncTasks.keyAt(i));
        }
        asyncTasks.clear();
    }

    protected void cancelRenderTask(int position) {
        WeakReference<PdfRenderPageAsyncTask> asyncTaskRef = asyncTasks.get(position);
        if (asyncTaskRef != null) {
            if (asyncTaskRef.get() != null) {
                asyncTaskRef.get().cancel(true);
            }
            asyncTasks.remove(position);
        }
    }

    @Override
    public int getCount() {
        return pdfiumReady() ? pdfiumCore.getPageCount(pdfDocument) : 0;
    }

    protected boolean pdfiumReady() {
        return pdfiumCore != null && pdfDocument != null;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }
}
