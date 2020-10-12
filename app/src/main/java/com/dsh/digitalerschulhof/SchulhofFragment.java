package com.dsh.digitalerschulhof;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SchulhofFragment extends Fragment {
    String SPEICHER_NAME        = "speicher";
    String SPEICHER_SCHULE      = "schule";
    String SPEICHER_BENUTZER    = "benutzer";
    String SPEICHER_PASSWORT    = "passwort";

    String pfad;

    String schule;

    WebView wv;
    SwipeRefreshLayout sr;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schulhof, container, false);
        schule = laden(SPEICHER_SCHULE, "https://digitaler-schulhof.de");
        wv = view.findViewById(R.id.wvSchulhof);
        sr = view.findViewById(R.id.srSchulhof);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setAppCacheEnabled(true);
        if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(wv.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            wv.setBackgroundColor(Color.parseColor("#212121"));
        }
        if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(wv.getSettings(), WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY);
        }
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(schule)) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                sr.setRefreshing(false);
                wv.evaluateJavascript("typeof CMS_BENUTZERNAME", new ValueCallback<String>() {
                    // Wenn nein, anmelden!
                    @Override
                    public void onReceiveValue(String value) {
                        if(value.equals("\"undefined\"")) {
                            String benutzer = laden(SPEICHER_BENUTZER);
                            String passwort = laden(SPEICHER_PASSWORT);
                            if(url.startsWith(schule + "/App")) {
                                wv.evaluateJavascript("cms_appanmeldung('"+benutzer+"','"+passwort+"');", null);
                            } else {
                                wv.evaluateJavascript("cms_anmeldung('"+benutzer+"','"+passwort+"');", null);
                            }
                        }
                    }
                });
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        wv.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Für Downloads einen Browser nutzen!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wv.reload();
                sr.setRefreshing(true);
            }
        });

        String pfad = "";
        if(getArguments() != null) {
            pfad = getArguments().getString("pfad");
        }

        wv.loadUrl(schule+(pfad.equals("") ? "/App" : "/"+pfad));
        sr.setRefreshing(true);
        return view;
    }

    public String laden(String key) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(SPEICHER_NAME, masterKey, getActivity().getApplicationContext(), EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).getString(key, "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String laden(String key, String fallback) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(SPEICHER_NAME, masterKey, getActivity().getApplicationContext(), EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).getString(key, fallback);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return fallback;
    }
}
