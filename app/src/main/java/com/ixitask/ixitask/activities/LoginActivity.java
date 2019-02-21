package com.ixitask.ixitask.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseLogin;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.username)
    EditText editUname;
    @BindView(R.id.password)
    EditText editPass;
    @BindView(R.id.login)
    Button btnLogin;
    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        editPass.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        ViewUtils.hideKeyboard(this, getCurrentFocus());
        btnLogin.setOnClickListener(view -> attemptLogin());
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * called on login button pressed
     */
    private void attemptLogin() {
        // Reset errors.
        editUname.setError(null);
        editPass.setError(null);

        // Store values at the time of the login attempt.
        String uname = editUname.getText().toString();
        String password = editPass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            editPass.setError(getString(R.string.error_field_required));
            focusView = editPass;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(uname)) {
            editUname.setError(getString(R.string.error_field_required));
            focusView = editUname;
            cancel = true;
        }

        ViewUtils.hideKeyboard(this, editPass);
        ViewUtils.hideKeyboard(this, editUname);
        if (cancel) {
            if (focusView!=null) focusView.requestFocus();
        } else {
            showProgress(true);
            IxitaskService.getApi().userLogin(
                    editUname.getText().toString(),
                    editPass.getText().toString())
                    .enqueue(new Callback<ResponseLogin>() {
                @Override
                public void onResponse(@NonNull Call<ResponseLogin> call, @NonNull Response<ResponseLogin> response) {
                    showProgress(false);
                    ResponseLogin res = response.body();
                    if (res != null) {
                        int status = Integer.parseInt(res.getStatus());
                        Log.d(TAG, res.toString());
                        Log.d(TAG, res.getStatusMessage());
                        if (status==200){
                            ResponseLogin.User loggedInUser = res.getUser();
                            if (loggedInUser != null) {
                                PreferenceManager
                                        .getDefaultSharedPreferences(LoginActivity.this)
                                        .edit()
                                        .putString(Constants.ARG_USER_ID, loggedInUser.getUserid())
                                        .putString(Constants.ARG_USER_KEY, loggedInUser.getUserkey())
                                        .putString(Constants.ARG_USERNAME, loggedInUser.getUsername())
                                        .apply();
                                startActivity(new Intent(LoginActivity.this,
                                        HomeActivity.class));
                            }
                        } else {
                            String message = getString(R.string.error_failed_login,res.getStatusMessage());
                            ViewUtils.dialogError(LoginActivity.this, "Failed", message)
                                    .setPositiveButton(getString(R.string.btn_retry),
                                            (d, w) -> attemptLogin())
                                    .create().show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseLogin> call, Throwable t) {
                    showProgress(false);
                    String message = getString(R.string.error_failed_login,t.getMessage());
                    Log.d(TAG, message);
                    t.printStackTrace();
                    Log.d(TAG,call.request().toString());
                    if (!PermissionUtils.isNetworkAvailable(LoginActivity.this))
                        ViewUtils.dialogError(LoginActivity.this, "Failed",
                                getString(R.string.error_no_internet))
                                .setPositiveButton(getString(R.string.btn_retry),
                                        (d, w) -> attemptLogin())
                                .create().show();
                    else
                        ViewUtils.dialogError(LoginActivity.this, "Failed", message)
                            .setPositiveButton(getString(R.string.btn_retry),
                                    (d, w) -> attemptLogin())
                            .create().show();
                }
            });
        }
    }

    /**
     * validate password text
     * @param password password text input
     * @return true if valid
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 6; //password has to be more than 6 character
    }

    /**
     * trigger animation whenever the login button is pressed
     * showing a progress dialog and hiding login form
     * @param show true if login submitted
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }
}

