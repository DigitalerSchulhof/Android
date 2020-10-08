package com.dsh.digitalerschulhof.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dsh.digitalerschulhof.R;
import com.dsh.digitalerschulhof.SchulhofFragment;

public class SchulhofActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("pfad", getIntent().getDataString());
            Fragment frag = new SchulhofFragment();
            frag.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, frag).commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            WebView wv = findViewById(R.id.wvSchulhof);
            if(wv != null) {
                if (wv.canGoBack()) {
                    wv.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
