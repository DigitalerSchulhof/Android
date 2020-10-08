package de.dsh.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import de.dsh.R;
import de.dsh.SchulhofFragment;

public class SchulhofActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new SchulhofFragment()).commit();
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