package com.ixitask.ixitask.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.utils.Constants;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
//        startPreferenceFragment(new GeneralPreferenceFragment(), false);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {

        private Context context;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            Preference logOutPref = getPreferenceScreen().findPreference(getString(R.string.preference_key_log_out));
            if (logOutPref!=null)
            logOutPref.setOnPreferenceClickListener(preference -> {
                if (context!=null) {
                    new AlertDialog.Builder(context)
                            .setTitle("Log out")
                            .setMessage(context.getString(R.string.prompt_log_out))
                            .setPositiveButton("Yes", (d, w) -> {
                                SharedPreferences.Editor prefEditor = PreferenceManager
                                        .getDefaultSharedPreferences(context).edit();
                                prefEditor.remove(Constants.ARG_USER_ID);
                                prefEditor.remove(Constants.ARG_USER_KEY);
                                prefEditor.remove(Constants.ARG_USERNAME);
                                prefEditor.clear();
                                prefEditor.commit();

                                startActivity(new Intent(context, HomeActivity.class));
                            })
                            .setNegativeButton("No", (d, w) -> {
                                d.dismiss();
                            })
                            .create().show();
                    return true;
                }
                return false;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), HomeActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}