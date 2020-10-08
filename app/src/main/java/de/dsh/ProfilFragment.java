package de.dsh;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class ProfilFragment extends Fragment {
    String SPEICHER_NAME        = "speicher";
    String SPEICHER_SCHULE      = "schule";
    String SPEICHER_BENUTZER    = "benutzer";
    String SPEICHER_PASSWORT    = "passwort";

    String schule;
    String benutzer;
    String passwort;

    HashMap<String, String> schulen = new HashMap<String, String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profil, container, false);
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
                String schule   = schulen.values().toArray(new String[] {})[((Spinner) view.findViewById(R.id.spnSchule)).getSelectedItemPosition()];
                speichern(SPEICHER_BENUTZER, benutzer);
                speichern(SPEICHER_PASSWORT, passwort);
                speichern(SPEICHER_SCHULE,   schule);
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
                Toast.makeText(getActivity().getApplicationContext(), "Änderungen gelöscht!", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    class SchulenLadenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... _) {
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
                throw new RuntimeException("Kein JSON!!!");
            }

            Spinner spnSchule = getView().findViewById(R.id.spnSchule);
            spnSchule.setAdapter(new ArrayAdapter(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, schulen.keySet().toArray(new String[] {})));

            String s = laden(SPEICHER_SCHULE, "https://digitaler-schulhof.de");
            for(int i = 0; i < schulen.size(); i++) {
                if(schulen.values().toArray()[i].equals(s)) {
                    spnSchule.setSelection(i);
                }
            }
        }
    }

    public void speichern(String k, String v) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences.Editor edit = EncryptedSharedPreferences.create(SPEICHER_NAME, masterKey, getActivity().getApplicationContext(), EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).edit();
            edit.putString(k, v);
            edit.apply();
        } catch (GeneralSecurityException | IOException ex) {
            ex.printStackTrace();
        }
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
