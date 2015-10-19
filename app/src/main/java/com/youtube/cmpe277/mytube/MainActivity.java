package com.youtube.cmpe277.mytube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.api.services.youtube.YouTubeScopes;

public class MainActivity

        extends
        AppCompatActivity

        implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        TokenTask.ITokenTask {


    private static final int RC_SIGN_IN = 1;
    private static final int RC_PERM_GET_ACCOUNTS = 2;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;


    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    private GoogleApiClient mGoogleApiClient;

    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {

            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .addScope(new Scope(YouTubeScopes.YOUTUBE))
                .build();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {

            if (resultCode == RESULT_OK) {

                String mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                getUsername(mEmail);
            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR
                && resultCode == RESULT_OK) {

            String mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            getUsername(mEmail);
        }

        if (requestCode == RC_SIGN_IN) {

            if (resultCode != RESULT_OK) {

                mShouldResolve = false;
            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Please select an account to Sign In", Toast.LENGTH_SHORT).show();
            }

            mIsResolving = false;
        }
    }

    private void getUsername(String mEmail) {

        if (mEmail == null) {

            pickUserAccount();
        } else {

            if (isDeviceOnline()) {

                new TokenTask(this, mEmail).execute();
            } else {

                Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isDeviceOnline() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            return true;
        }

        return false;
    }


    private void showErrorDialog(ConnectionResult connectionResult) {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (apiAvailability.isUserResolvableError(resultCode)) {

                apiAvailability.getErrorDialog(this, resultCode, RC_SIGN_IN,
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {

                                mShouldResolve = false;
                            }
                        }).show();
            }
            else {

                String errorString = apiAvailability.getErrorString(resultCode);
                Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

                mShouldResolve = false;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == RC_PERM_GET_ACCOUNTS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        mShouldResolve = false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!mIsResolving && mShouldResolve) {

            if (connectionResult.hasResolution()) {

                try {

                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                }
                catch (IntentSender.SendIntentException e) {

                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {

                showErrorDialog(connectionResult);
            }
        }
    }

    public void didTouchSignInButton(View v) {

        onSignInClicked();
    }

    private void onSignInClicked() {

        mShouldResolve = true;
        pickUserAccount();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    @Override
    public void startTabbedActitvity(String accessToken) {

        Constants.setAccessToken(accessToken);
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    @Override
    public void handleExceptions(final Exception e) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (e instanceof UserRecoverableAuthException) {

                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    @Override
    public Activity getActivity() {

        return (Activity)this;
    }
}