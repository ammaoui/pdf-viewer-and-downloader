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

import com.github.barteksc.pdfviewpager.PDFViewPagerIVZoom;
import com.github.barteksc.pdfviewpager.adapter.PDFPagerAdapter;


public class ZoomablePDFOnXMLActivity extends AppCompatActivity{
    PDFViewPagerIVZoom pdfViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.zoomable_asset_on_xml);
        setContentView(R.layout.activity_zoomable_pdf_xml);

        pdfViewPager = (PDFViewPagerIVZoom) findViewById(R.id.pdfViewPagerZoom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((PDFPagerAdapter) pdfViewPager.getAdapter()).close();
    }

    public static void open(Context context){
        Intent i = new Intent(context, ZoomablePDFOnXMLActivity.class);
        context.startActivity(i);
    }
}