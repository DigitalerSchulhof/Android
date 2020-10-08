package de.dsh;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SchulhofFragment extends Fragment {
    String SPEICHER_NAME        = "speicher";
    String SPEICHER_SCHULE      = "schule";
    String SPEICHER_BENUTZER    = "benutzer";
    String SPEICHER_PASSWORT    = "passwort";

    WebView wv;
    SwipeRefreshLayout sr;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schulhof, container, false);
        wv = view.findViewById(R.id.wvSchulhof);
        sr = view.findViewById(R.id.srSchulhof);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                sr.setRefreshing(false);
                wv.evaluateJavascript("document.getElementById('cms_appAngemeldet').value", new ValueCallback<String>() {
                    // Wenn nein, anmelden! 2, da String als "" ausgegeben wird
                    @Override
                    public void onReceiveValue(String value) {
                        if(value.length() == 2) {
                            String benutzer = laden(SPEICHER_BENUTZER);
                            String passwort = laden(SPEICHER_PASSWORT);
                            wv.evaluateJavascript("cms_appanmeldung('"+benutzer+"','"+passwort+"');", null);
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
                laden();
            }
        });

        laden();
        return view;
    }

    public void laden() {
        wv.loadUrl(laden(SPEICHER_SCHULE, "https://digitaler-schulhof.de")+"/App");
        sr.setRefreshing(true);
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