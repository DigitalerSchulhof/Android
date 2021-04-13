package de.dsh;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProfilFragment extends Fragment {
    final String SPEICHER_NAME        = "speicher";

    String SPEICHER_SCHULE      = "schule";
    String SPEICHER_BENUTZER    = "benutzer";
    String SPEICHER_PASSWORT    = "passwort";

    MasterKey MASTER_KEY;
    SharedPreferences PREFERENCES;

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";

    String schule;
    String benutzer;
    String passwort;

    HashMap<String, String> schulen = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profil, container, false);

        try {
            MASTER_KEY = new MasterKey.Builder(getActivity().getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            PREFERENCES = EncryptedSharedPreferences.create(getActivity().getApplicationContext(), SPEICHER_NAME, MASTER_KEY, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            getActivity().getSharedPreferences(SPEICHER_NAME, Context.MODE_PRIVATE).edit().clear().apply();
            e.printStackTrace();
        }

        new SchulenLadenTask().execute();

        benutzer = laden("benutzer");
        passwort = laden("passwort");
        final EditText edtBenutzer = view.findViewById(R.id.edtBenutzer);
        final EditText edtPasswort = view.findViewById(R.id.edtPasswort);
        edtBenutzer.setText(benutzer);
        edtPasswort.setText(passwort);

        ((Spinner) view.findViewById(R.id.spnSchule)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                schule = schulen.values().toArray(new String[] {})[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        view.findViewById(R.id.btnSpeichern).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String benutzer = edtBenutzer.getText().toString();
                String passwort = edtPasswort.getText().toString();
                String schule = schulen.values().toArray(new String[]{})[((Spinner) view.findViewById(R.id.spnSchule)).getSelectedItemPosition()];
                speichern(SPEICHER_BENUTZER, benutzer);
                speichern(SPEICHER_PASSWORT, passwort);
                speichern(SPEICHER_SCHULE, schule);
                edtPasswort.setText("");

                // https://stackoverflow.com/questions/1109022/how-do-you-close-hide-the-android-soft-keyboard-using-java#answer-17789187
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                if (view == null) {
                    view = new View(getActivity());
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Toast.makeText(getActivity().getApplicationContext(), "Änderungen gespeichert!", Toast.LENGTH_LONG).show();
            }
        });

        view.findViewById(R.id.btnAbbrechen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtBenutzer.setText(benutzer);
                edtPasswort.setText(passwort);

                // https://stackoverflow.com/questions/1109022/how-do-you-close-hide-the-android-soft-keyboard-using-java#answer-17789187
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                if (view == null) {
                    view = new View(getActivity());
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Toast.makeText(getActivity().getApplicationContext(), "Änderungen gelöscht!", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    class SchulenLadenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://digitaler-schulhof.de/dshs.php");
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String json) {
            try {
                JSONArray arr = new JSONArray(json);
                for(int i = 0; i < arr.length(); i++) {
                    JSONArray schule = arr.getJSONArray(i);
                    schulen.put(schule.getString(0) + " (" + schule.getString(1) + ")", schule.getString(2));
                }
            } catch (JSONException e) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Fehler beim Laden unterstützter Schulen!", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            Spinner spnSchule = getView().findViewById(R.id.spnSchule);
            spnSchule.setAdapter(new ArrayAdapter(getActivity().getApplicationContext(), R.layout.profil_spinner, schulen.keySet().toArray(new String[] {})));

            String s = laden(SPEICHER_SCHULE, "");
            for(int i = 0; i < schulen.size(); i++) {
                if(schulen.values().toArray()[i].equals(s)) {
                    spnSchule.setSelection(i);
                }
            }
        }
    }

    public void speichern(String k, String v) {
        SharedPreferences.Editor editor = PREFERENCES.edit();
        editor.putString(k, v);
        editor.apply();
    }

    public String laden(String key) {
        return laden(key, "");
    }

    public String laden(String key, String fallback) {
        if(PREFERENCES == null) {
            return fallback;
        }
        return PREFERENCES.getString(key, fallback);
    }
}
