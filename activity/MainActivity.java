package com.actiknow.chatbot.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.actiknow.chatbot.R;
import com.actiknow.chatbot.utils.AppConfigTags;
import com.actiknow.chatbot.utils.AppConfigURL;
import com.actiknow.chatbot.utils.AppDataPref;
import com.actiknow.chatbot.utils.Constants;
import com.actiknow.chatbot.utils.NetworkConnection;
import com.actiknow.chatbot.utils.SetTypeFace;
import com.actiknow.chatbot.utils.Utils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ImageView ivImageView;
    WebView webView;
    LinearLayout ll1;
    RelativeLayout rlNoInternetAvailable;
    ProgressBar progressBar;
    FrameLayout fl1;
    View v1;
    AppDataPref appDataPref;
    String setting_url;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initApplication();
        initListener();
    }

    private void initApplication() {
        if (NetworkConnection.isNetworkAvailable(MainActivity.this)) {
            Utils.showLog(Log.INFO, AppConfigTags.URL, AppConfigURL.INIT, true);
            StringRequest strRequest = new StringRequest(Request.Method.POST, AppConfigURL.INIT,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Utils.showLog(Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                            if (response != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject(response);
                                    boolean is_error = jsonObj.getBoolean(AppConfigTags.ERROR);
                                    String message = jsonObj.getString(AppConfigTags.MESSAGE);
                                    int status = jsonObj.getInt(AppConfigTags.STATUS);
                                    if(!is_error){
                                        if(status != 1){
                                            MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                                                    .limitIconToDefaultSize()
                                                    .content(jsonObj.getString(AppConfigTags.SETTING_URL))
                                                    .positiveText("OK")
                                                    .typeface(SetTypeFace.getTypeface(MainActivity.this), SetTypeFace.getTypeface(MainActivity.this))
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            finish();
                                                        }
                                                    }).build();
                                            dialog.show();
                                        }else {
                                            if (appDataPref.getStringPref(MainActivity.this, AppDataPref.RESPONSE).equalsIgnoreCase("")) {
                                                Glide.with(MainActivity.this)
                                                        .load(jsonObj.getString(AppConfigTags.SETTING_IMAGE))
                                                        .listener(new RequestListener<String, GlideDrawable>() {
                                                            @Override
                                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                                progressBar.setVisibility(View.GONE);
                                                                return false;
                                                            }

                                                            @Override
                                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                                progressBar.setVisibility(View.GONE);
                                                                return false;
                                                            }
                                                        })
                                                        .into(ivImageView);
                                            }
                                        }
                                        appDataPref.putStringPref(MainActivity.this, AppDataPref.RESPONSE, response);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            } else {
                                Utils.showLog(Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Utils.showLog(Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString(), true);
                            NetworkResponse response = error.networkResponse;
                            if (response != null && response.data != null) {
                                Utils.showLog(Log.ERROR, AppConfigTags.ERROR, new String(response.data), true);
                            }

                        }
                    }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new Hashtable<String, String>();
                    Utils.showLog(Log.INFO, AppConfigTags.PARAMETERS_SENT_TO_THE_SERVER, "" + params, true);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(AppConfigTags.HEADER_API_KEY, Constants.api_key);
                    params.put(AppConfigTags.PACKAGE_NAME, getApplicationContext().getPackageName());
                    Utils.showLog(Log.INFO, AppConfigTags.HEADERS_SENT_TO_THE_SERVER, "" + params, false);
                    return params;
                }
            };
            Utils.sendRequest(strRequest, 30);
        } else {
                    Intent dialogIntent = new Intent(Settings.ACTION_SETTINGS);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                }
    }

    private void initView(){
        ivImageView = (ImageView)findViewById(R.id.ivImageView);
        webView = (WebView)findViewById(R.id.webView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        fl1 = (FrameLayout) findViewById (R.id.fl1);
        v1 = findViewById (R.id.v1);
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        rlNoInternetAvailable = (RelativeLayout) findViewById(R.id.rlNoInternetAvailable);
    }

    private void initData(){
        progressDialog = new ProgressDialog(this);
        Configuration config = getResources().getConfiguration();
        appDataPref = AppDataPref.getInstance();
        String response = appDataPref.getStringPref(MainActivity.this, AppDataPref.RESPONSE);
        if (response != null) {
            try {
                JSONObject jsonObj = new JSONObject(response);
                boolean is_error = jsonObj.getBoolean(AppConfigTags.ERROR);
                String message = jsonObj.getString(AppConfigTags.MESSAGE);
                int status = jsonObj.getInt(AppConfigTags.STATUS);
                setting_url = jsonObj.getString(AppConfigTags.SETTING_URL);
                if (!is_error) {
                    if (status != 1) {
                        MaterialDialog dialog = new MaterialDialog.Builder(this)
                                .limitIconToDefaultSize()
                                .content(jsonObj.getString(AppConfigTags.SETTING_URL))
                                .positiveText("OK")
                                .typeface(SetTypeFace.getTypeface(MainActivity.this), SetTypeFace.getTypeface(MainActivity.this))
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        finish();
                                    }
                                }).build();
                        dialog.show();
                    } else {
                            Glide.with(MainActivity.this)
                                    .load(jsonObj.getString(AppConfigTags.SETTING_IMAGE))
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(ivImageView);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private void initListener(){
        ivImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isNetworkAvailable(MainActivity.this)) {
                    ivImageView.setVisibility(View.GONE);
                    v1.setVisibility(View.VISIBLE);
                    ll1.setVisibility(View.VISIBLE);
                    getWebView();
                }else{
                    ivImageView.setVisibility(View.GONE);
                    v1.setVisibility(View.GONE);
                    ll1.setVisibility(View.GONE);
                    rlNoInternetAvailable.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getWebView () {
        webView.setWebViewClient (new CustomWebViewClient ());
        WebSettings webSetting = webView.getSettings ();
        webSetting.setJavaScriptEnabled (true);
        webSetting.setDisplayZoomControls (true);
        Utils.showProgressDialog(progressDialog, getResources().getString(R.string.progress_dialog_text_please_wait), true);
        Log.e("setting_url",setting_url);
        webView.loadUrl (setting_url);
        //SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder ("<style>@font-face{font-family: myFont; src: url(file:///android_asset/" + Constants.font_name + ");}</style>" + htmlTermsOfUse);
        //webView.loadDataWithBaseURL ("", spannableStringBuilder.toString (), "text/html", "UTF-8", "");


        if (Build.VERSION.SDK_INT >= 21) {
            progressBar.setProgressTintList (ColorStateList.valueOf (getResources ().getColor (R.color.colorPrimary)));
            progressBar.setIndeterminateTintList (ColorStateList.valueOf (getResources ().getColor (R.color.colorPrimary)));
        } else {
            progressBar.getProgressDrawable ().setColorFilter (
                    getResources ().getColor (R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.getIndeterminateDrawable ().setColorFilter (
                    getResources ().getColor (R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }


       /* webView.setWebViewClient (new WebViewClient() {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
                super.onPageStarted (view, url, favicon);
                if (url.length () > 0) {
                    fl1.setVisibility (View.VISIBLE);
                    v1.setVisibility (View.GONE);
                }
            }

            public void onPageFinished (WebView view, String url) {
                progressDialog.dismiss ();
                fl1.setVisibility (View.GONE);
                v1.setVisibility (View.VISIBLE);
            }
        });

        webView.setWebChromeClient (new WebChromeClient() {
            public void onProgressChanged (WebView view, int progress) {
                if (progress > 70) {
                    progressBar.setIndeterminate (true);
                } else {
                    progressBar.setIndeterminate (false);
                    progressBar.setProgress (progress + 10);
                }
            }
        });*/
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            Utils.showProgressDialog(progressDialog, getResources().getString(R.string.progress_dialog_text_please_wait), true);
            view.loadUrl (url);
            return true;
        }

        public void onPageFinished (WebView view, String url) {
            progressDialog.dismiss ();
            fl1.setVisibility (View.GONE);
            v1.setVisibility (View.GONE);
        }



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    { //if back key is pressed
        if((keyCode == KeyEvent.KEYCODE_BACK)&& webView.canGoBack())
        {
            webView.goBack();
            return true;

        }

        return super.onKeyDown(keyCode, event);

    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set title
        alertDialogBuilder.setTitle("Exit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
/*
    private class MyWebViewClient extends WebViewClient {
        @Override
//Implement shouldOverrideUrlLoading//
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

//Check whether the URL contains a whitelisted domain. In this example, we’re checking
//whether the URL contains the “example.com” string//
            if(Uri.parse(url).getHost().endsWith("example.com")) {

//If the URL does contain the “example.com” string, then the shouldOverrideUrlLoading method
//will return ‘false” and the URL will be loaded inside your WebView//
                return false;
            }

//If the URL doesn’t contain this string, then it’ll return “true.” At this point, we’ll
//launch the user’s preferred browser, by firing off an Intent//
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
    }*/
}
