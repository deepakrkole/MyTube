package com.youtube.cmpe277.mytube;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class TokenTask extends AsyncTask {

    ITokenTask tokenTaskObj;

    public interface  ITokenTask {

        public void startTabbedActitvity(String accessToken);
        public void handleExceptions(Exception exc);
        public Activity getActivity();
    }


    private final static String scopeString = "oauth2:" + "https://www.googleapis.com/auth/youtube";

    String userEmail;

    public TokenTask(Activity activity, String name) {

        this.userEmail = name;
        tokenTaskObj = (ITokenTask) activity;
    }

    private String fetchToken() throws IOException {

        try {

            return GoogleAuthUtil.getToken(tokenTaskObj.getActivity(), userEmail, scopeString);
        } catch (UserRecoverableAuthException userRecoverableException) {

            tokenTaskObj.handleExceptions(userRecoverableException);
        } catch (GoogleAuthException fatalException) {

            tokenTaskObj.handleExceptions(fatalException);
        }
        return null;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            String accessToken = fetchToken();
            if (accessToken != null) {

                System.out.println("Access Token "+accessToken);
                tokenTaskObj.startTabbedActitvity(accessToken);
            }
        } catch (IOException e) {

        }
        return null;
    }
}