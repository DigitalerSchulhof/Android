package de.dsh;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.webkit.WebView;

import de.dsh.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.navigation);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_schulhof, R.id.navigation_website, R.id.navigation_profil)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.navigation_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
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
            wv = findViewById(R.id.wvWebsite);
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