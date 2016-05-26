# PdfViewPager

Android widget to display PDF documents in your Activities or Fragments.

Important note: **PDFViewPager** uses [PdfiumAndroid][6] lib, which works **on API 9 or higher**.

[Legacy sample][7] is left, because it may be useful for someone...

Changes in this fork
--------------------
* uses [PdfiumAndroid][6] instead of PdfRenderer
* uses [VerticalViewPager][11] for vertical scrolling
* optimizes memory usage by recycling views and bitmaps during scroll
* loads PDF pages in separated AsyncTasks
* added scrollbar for quick document browsing
* added convinient AsyncTask for loading adapter with document asynchronously (it doesn't lag UI)

... so nearly whole library has changed and I decided to change Java package name to

`com.github.barteksc`

Classes and other packages' names are untouched.

Installation
------------

Add this line in your *app/build.gradle*

    compile 'com.github.barteksc:pdf-view-pager:1.0.1'

Usage - Remote PDF's
--------------------

![Screenshot][remotePDFScreenshot]

Use **RemotePDFViewPager** to load from remote URLs

1.- Add INTERNET, READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions on your AndroidManifest.xml

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

2.- Make your Activity or Fragment implement DownloadFile.Listener

    public class RemotePDFActivity extends AppCompatActivity implements DownloadFile.Listener {

3.- Create a **RemotePDFViewPager** object

    RemotePDFViewPager remotePDFViewPager =
          new RemotePDFViewPager(context, "http://partners.adobe.com/public/developer/en/xml/AdobeXMLFormsSamples.pdf", this);

4.- Configure the corresponding callbacks and they will be called on each situation.

    @Override
    public void onSuccess(String url, String destinationPath) {
        // That's the positive case. PDF Download went fine

        adapter = new PDFPagerAdapter(this, "AdobeXMLFormsSamples.pdf");
        remotePDFViewPager.setAdapter(adapter);
        setContentView(remotePDFViewPager);
    }

    @Override
    public void onFailure(Exception e) {
        // This will be called if download fails
    }

    @Override
    public void onProgressUpdate(int progress, int total) {
        // You will get download progress here
        // Always on UI Thread so feel free to update your views here
    }

5.- Don't forget to close adapter in *onDestroy* to release all resources

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adapter.close();
    }

Usage - Local PDF's
-------------------

Use **PDFViewPager** class to load PDF from assets

![Screenshot][localPDFScreenshot] ![Screenshot][zoomingScreenshot]

1.- Copy your assets to cache directory (lib will do that for you in future versions)

    CopyAsset copyAsset = new CopyAssetThreadImpl(context, new Handler());
    copyAsset.copy(asset, new File(getCacheDir(), "sample.pdf").getAbsolutePath());

2.- Create your **PDFViewPager** passing your PDF file, located in *assets* (see [sample][8])

    pdfViewPager = new PDFViewPager(this, "sample.pdf");

*Now supports Zooming and panning thanks to [sephiroth74/ImageViewZoom][5] library*

    pdfViewPager = new PDFViewPagerZoom(this, "sample.pdf");

2b.- Or directly, declare it on your XML layout

    <es.voghdev.pdfviewpager.library.PDFViewPager
        android:id="@+id/pdfViewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:assetFileName="sample.pdf"/>

*again, if you want zoom and pan support*

    <es.voghdev.pdfviewpager.library.PDFViewPagerZoom
    ... />

3.- Release adapter in *onDestroy*

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((PDFPagerAdapter)pdfViewPager.getAdapter()).close();
    }

Usage - PDF's on SD card
------------------------

![Screenshot][sdcardPDFScreenshot]

Use **PDFViewPager** class to load PDF from your SD card

1.- Create a **PDFViewPager** object, passing the file location in your SD card

    PDFViewPager pdfViewPager = new PDFViewPager(context, getPdfPathOnSDCard());

    protected String getPdfPathOnSDCard() {
        File f = new File(getExternalFilesDir("pdf"), "adobe.pdf");
        return f.getAbsolutePath();
    }

Usage - scrollbar
-----------------

Use **ScrollBar** class to place scrollbar view near **PdfViewPager**

1.- in layout XML (it's important that the parent view is **RelativeLayout**)

``` xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:app="http://schemas.android.com/apk/res-auto"
              android:orientation="vertical"
              android:layout_width="fill_parent"
              android:layout_height="fill_parent">

    <com.github.barteksc.pdfviewpager.PDFViewPagerZoom
        android:id="@+id/pdfViewPagerZoom"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:assetFileName="adobe.pdf"
        android:layout_below="@+id/textView1"
        android:layout_toLeftOf="@+id/scrollBar"/>

    <com.github.barteksc.pdfviewpager.view.ScrollBar
        android:id="@+id/scrollBar"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_alignParentRight="true"
        android:layout_alignParentEnd="true" />
</RelativeLayout>
```
2.- in activity or fragment
``` java

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ...

        PdfViewPager pdfViewPager = (PDFViewPager) findViewById(R.id.pdfViewPagerZoom);
        ScrollBar scrollBar = (ScrollBar) findViewById(R.id.scrollBar);
        scrollBar.setVerticalViewPager(pdfViewPager);
    }

```

TODOs
-----

- [ ] adapt scrollbar for handling big number of pages
- [ ] update documentation in this readme

Developed By
------------

* Olmo Gallegos Hernández - [@voghDev][9] - [mobiledevstories.com][10]

<a href="http://twitter.com/voghDev">
  <img alt="Follow me on Twitter" src="http://imageshack.us/a/img812/3923/smallth.png" />
</a>
<a href="https://www.linkedin.com/profile/view?id=91543271">
  <img alt="Find me on Linkedin" src="http://imageshack.us/a/img41/7877/smallld.png" />
</a>

* Heavily modified by Bartosz Schiller

# License

    Original work Copyright 2015 Olmo Gallegos Hernández
    Modified work Copyright 2016 Bartosz Schiller

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Contributing
------------

    fork the project into your GitHub account
    checkout your GitHub repo for the project
    implement your changes
    commit your changes, push them
    review your code and send me a pull request if you consider it

[remotePDFScreenshot]: ./screenshots/remote.gif
[localPDFScreenshot]: ./screenshots/local.gif
[sdcardPDFScreenshot]: ./screenshots/sdcard.gif
[zoomingScreenshot]: ./screenshots/zooming.gif
[5]: https://github.com/sephiroth74/ImageViewZoom
[6]: https://github.com/barteksc/PdfiumAndroid
[7]: https://github.com/barteksc/PdfViewPager/blob/master/sample/src/main/java/es/voghdev/pdfviewpager/LegacyPDFActivity.java
[8]: https://github.com/barteksc/PdfViewPager/tree/master/sample/src/main/java/es/voghdev/pdfviewpager
[9]: http://twitter.com/voghDev
[10]: http://www.mobiledevstories.com
[11]: https://github.com/castorflex/VerticalViewPager
