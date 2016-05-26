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
package es.voghdev.pdfviewpager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.barteksc.pdfviewpager.PDFViewPager;
import com.github.barteksc.pdfviewpager.adapter.PDFPagerAdapter;
import com.github.barteksc.pdfviewpager.view.ScrollBar;


public class ZoomablePDFActivityPhotoView extends AppCompatActivity{
    PDFViewPager pdfViewPager;
    ScrollBar scrollBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.zoomable_asset_on_xml);
        setContentView(R.layout.activity_zoomable_pdf_xml_photoview);
        pdfViewPager = (PDFViewPager) findViewById(R.id.pdfViewPagerZoom);
        scrollBar = (ScrollBar) findViewById(R.id.scrollBar);
        scrollBar.setVerticalViewPager(pdfViewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PDFPagerAdapter adapter = (PDFPagerAdapter) pdfViewPager.getAdapter();
        if(adapter != null) {
            adapter.close();
            adapter = null;
        }
    }

    public static void open(Context context){
        Intent i = new Intent(context, ZoomablePDFActivityPhotoView.class);
        context.startActivity(i);
    }
}
