package com.innv.rmsgateway.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.innv.rmsgateway.ActivityDashboard;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.JsonWebService;
import com.innv.rmsgateway.classes.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PREFS_KEY_SELECTED_SERVER = "SERVER NAME";
    public static final String PREFS_KEY_SELECTED_PORT = "SERVER PORT";
    public static final String PREFS_KEY_CUSTOM_SERVER = "USER CUSTOM SERVER";
    public static final String PREFS_KEY_SERVER_TOKEN = "SERVER TOKEN";
    public static final String PREFS_KEY_SERVER_SYNC = "SYNC SERVER";
    public static final String PREFS_KEY_USER_PASSWORD = "USER PASSWORD";
    private AutoCompleteTextView urlView;
    private AutoCompleteTextView portView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private Button email_sign_in_button;
    ProgressDialog progressDialog;
    private View mLoginFormView;
    private View mProgressView;
    Boolean isProceed = false;
    public String server;
    public String port;
    public static String email;
    public static String password;
    private Boolean checkIsUserValid = false;
    private Boolean isLoginRequest = false;
    private  Boolean isFirstStart = false;


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) { }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static boolean checkCloudSync(Context ctx){

        Boolean syncServer = Globals.pref.getBoolean(PREFS_KEY_SERVER_SYNC, true);

        if(syncServer) {
            Globals.loadLoginData();
            if (Globals.user != null) {
                String pass = Globals.pref.getString(PREFS_KEY_USER_PASSWORD, "");
                Globals.user.setPassword(pass);
                email = Globals.user.getEmail();
                password = Globals.user.getPassword();

                if (Globals.webLogin(ctx, Globals.orgCode, email, password)) {
                    Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_NETWORK_CONNECTED,"");
                    return true;
                }else{
                    Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_NETWORK_DISCONNECTED,"");
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Globals.setScreenOrientation(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setTitle("Data Sync Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if( getIntent().getExtras() != null) {
            isFirstStart = getIntent().getExtras().getBoolean("FIRST_START");
        }

        setLocale(this);

        Globals.loginContext = this;

        setContentView(R.layout.login_activity);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        urlView = (AutoCompleteTextView) findViewById(R.id.urlView);
        urlView.setText(Globals.wsDomainName);
        portView = (AutoCompleteTextView) findViewById(R.id.port);
        portView.setText(Globals.wsPort);
        mPasswordView = (EditText) findViewById(R.id.password);

        Globals.loadLoginData();

        if(Globals.user != null) {
            mEmailView.setText(Globals.user.getEmail());
            String pass = Globals.pref.getString(PREFS_KEY_USER_PASSWORD, "");
            Globals.user.setPassword(pass);
            mPasswordView.setText(pass);
            email = mEmailView.getText().toString();
            password = mPasswordView.getText().toString();
        }
        server = urlView.getText().toString();
        port = portView.getText().toString();

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLoginEx();
                    return true;
                }
                return false;
            }
        });

        email_sign_in_button = (Button) findViewById(R.id.email_sign_in_button);

        email_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(email_sign_in_button.getText().equals("Logout")){
                    mEmailView.setEnabled(true);
                    urlView.setEnabled(true);
                    portView.setEnabled(true);
                    mPasswordView.setEnabled(true);
                    email_sign_in_button.setText("Login");

                    SharedPreferences.Editor editor = Globals.pref.edit();
                    editor.putBoolean(PREFS_KEY_SERVER_SYNC, false);
                    editor.apply();

                    ActivityDashboard.stopDataSyncService();
                }else {
                    if (isNetworkAvailable()) {
                        isProceed = true;
                        isLoginRequest = true;
                        attemptLoginEx();
                    } else {
                        Toast.makeText(LoginActivity.this, getResources().getText(R.string.err_internet_not_available)
                                , Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        Boolean syncingServer = Globals.pref.getBoolean(PREFS_KEY_SERVER_SYNC, true);
        if(!syncingServer){
            email_sign_in_button.setText("Login");
        }

        else if(!isFirstStart) {
            checkIsUserValid = true;
            new ServerAsyncTask().execute();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void attemptLoginEx() {
        // Reset errors.
        urlView.setError(null);
        portView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        //Save and get from Shared Pref here

        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();
        server = urlView.getText().toString();
        port = portView.getText().toString();

        // Store values at the time of the login attempt.
        if(server.isEmpty()){
            server = Globals.wsDomainName;
        }

        if(port.isEmpty()){
            port = Globals.wsPort;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(server)) {
            urlView.setError(getString(R.string.error_field_required));
            focusView = urlView;
            cancel = true;
        }
        else  if (TextUtils.isEmpty(port)) {
            portView.setError(getString(R.string.error_field_required));
            focusView = portView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(email) && !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new ServerTestAsyncTask().execute();
        }


    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public static void setLocale(Context context) {
        final String lang = PreferenceManager.getDefaultSharedPreferences(context).getString("LANG", "en");
        Configuration config = context.getResources().getConfiguration();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
      //  Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_LANGUAGE_CHANGED, lang);
        //final Resources res = context.getResources();
        //res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public static String getPingAddress(String server){
        return "https://" + server + "/api/login";
    }

    private void openContinueDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.confirmation))
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface _dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Globals.loadLoginData();
                        _dialog.dismiss();

                        //    startActivity(dashIntent);

                    }
                });

                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //CLOSE THE DIALOG
                        _dialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void onLoginRequest(){
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Globals.isInternetAvailable(LoginActivity.this)) {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, getResources().getText(R.string.err_internet_not_available)
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                    //return;
                }
                if (Globals.webLogin(LoginActivity.this, Globals.orgCode, email, password)) {

                    if (checkIsUserValid) {
                        checkIsUserValid = false;
                    }
                    updateView();

                } else {
                    if (isNetworkAvailable()) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mEmailView.setError(getString(R.string.incorrect_user_info_error));
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                            }
                        });
                    }
                    if (!Globals.LOGIN_ERROR.equals("")) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, Globals.LOGIN_ERROR, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });

            }
        }).start();


    }

    private class ServerAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getString(R.string.dialog_wait_title));
            progressDialog.show();

            if(!checkIsUserValid) {
                showProgress(true);
            }

            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            // Parse response data
            JsonWebService.getJSON(getPingAddress(server), (int)Globals.httpSocketTimeOut);
            return null;
        }

        protected void onPostExecute(Void result) {

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if(!checkIsUserValid) {
                showProgress(false);
            }

            if(Globals.lastWsReturnCode == 200 || Globals.lastWsReturnCode == 201){
                if(isLoginRequest){
                    if(isProceed){
                        isProceed = false;
                    }

                    isLoginRequest = false;
                    onLoginRequest();
                }
                else if(Globals.user != null){
                    if(! Globals.user.getRemoved()){
                        if(isProceed){
                            isProceed = false;
                        }
                        checkIsUserValid = false;
                        Globals.loadLoginData();
                        updateView();
                    }
                }else{
                    onLoginRequest();
                }
            } else if(Globals.lastWsReturnCode == 401){
                if(isProceed){
                    isProceed = false;
                    Globals.loadLoginData();
                    //startActivity(dashIntent);
                }
            } else if(Globals.lastWsReturnCode == 404) {
                if (isProceed) {
                    isProceed = false;
                }
                Toast.makeText(LoginActivity.this, getString(R.string.toast_timps_no_run), Toast.LENGTH_LONG).show();
            }
            else if(Globals.lastWsReturnCode == 0) {
                if (isProceed) {
                    isProceed = false;
                }
                Toast.makeText(LoginActivity.this, getString(R.string.toast_server_unreach), Toast.LENGTH_LONG).show();
            } else {
                if (isProceed) {
                    isProceed = false;
                    Toast.makeText(LoginActivity.this, getString(R.string.toast_server_unreach), Toast.LENGTH_LONG).show();
                }
                Toast.makeText(LoginActivity.this, getString(R.string.toast_server_unreach), Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(result);
        }
    }

    private class ServerTestAsyncTask extends AsyncTask<Void, Void, Void> {
        public User user;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(LoginActivity.this.getString(R.string.dialog_wait_title));
            progressDialog.show();
            Globals.lastWsReturnCode = 0;
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            JsonWebService.getJSON(getPingAddress(server), 5000);
            return null;
        }

        protected void onPostExecute(Void result) {
            // openDialog();
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (Globals.lastWsReturnCode == 200 || Globals.lastWsReturnCode == 201) {
                if (Globals.user != null) {
                    if (!Globals.user.getRemoved()) {
                        updateServer();
                    }
                }else{
                    onLoginRequest();
                }

            } else if (Globals.lastWsReturnCode == 401) {
                //Server Connected Successfully
                //Login user now
                updateServer();
            } else if (Globals.lastWsReturnCode == 404) {
                Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.msg_rms_running_server), Toast.LENGTH_LONG).show();
            } else if (Globals.lastWsReturnCode == 0) {
                Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.msg_server_unreach), Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(result);
        }
    }

    public void updateView(){
        mEmailView.setEnabled(false);

        urlView.setEnabled(false);
        portView.setEnabled(false);
        mPasswordView.setEnabled(false);
        email_sign_in_button.setText("Logout");

        SharedPreferences.Editor editor = Globals.pref.edit();
        editor.putString(PREFS_KEY_SERVER_TOKEN, Globals.appid);
        editor.putString(PREFS_KEY_USER_PASSWORD, password);
        editor.putBoolean(PREFS_KEY_SERVER_SYNC, true);
        editor.apply();



        if(isFirstStart){
            isFirstStart = false;
            finish();
        }
    }

    public void updateServer() {
        Globals.wsDomainName = server;
        Globals.wsPort = port;
        SharedPreferences.Editor editor = Globals.pref.edit();
        editor.putString(PREFS_KEY_SELECTED_SERVER, server);
        editor.putString(PREFS_KEY_SELECTED_PORT, port);
        editor.putBoolean(PREFS_KEY_CUSTOM_SERVER, true);
        editor.apply();
        Globals.setDomain();

        new ServerAsyncTask().execute();

    }


}
