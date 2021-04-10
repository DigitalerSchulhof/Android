package de.dsh;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class WebsiteFragment extends Fragment {
    final String SPEICHER_NAME        = "speicher";
    String SPEICHER_SCHULE      = "schule";

    String schule;

    MasterKey MASTER_KEY;
    SharedPreferences PREFERENCES;

    WebView wv;
    SwipeRefreshLayout sr;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_website, container, false);

        try {
            MASTER_KEY = new MasterKey.Builder(getActivity().getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            PREFERENCES = EncryptedSharedPreferences.create(getActivity().getApplicationContext(), SPEICHER_NAME, MASTER_KEY, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        schule = laden(SPEICHER_SCHULE, "https://digitaler-schulhof.de");
        wv = view.findViewById(R.id.wvWebsite);
        sr = view.findViewById(R.id.srWebsite);

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
            public void onPageFinished(WebView view, String url) {
                sr.setRefreshing(false);
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

        wv.getSettings().setUserAgentString(wv.getSettings().getUserAgentString() + "-AppAndroid");
        wv.loadUrl(schule+(pfad.equals("") ? "/" : "/"+pfad));
        sr.setRefreshing(true);
        return view;
    }

    public String laden(String key) {
        return laden(key, "");
    }

    public String laden(String key, String fallback) {
        return PREFERENCES.getString(key, fallback);
    }
}
