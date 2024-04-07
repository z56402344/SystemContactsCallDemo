package com.example.callplusdemo.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

public final class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    private Authenticator getAuthenticator() {
        if (mAuthenticator == null) {
            mAuthenticator = new Authenticator(this);
        }

        return mAuthenticator;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            Log.d("bugtags", "AuthenticatorService-->onBind()-->getIBinder()");
            return getAuthenticator().getIBinder();
        } else {
            Log.d("bugtags","AuthenticatorService-->onBind()-->null");
            return null;
        }
    }

    public class Authenticator extends AbstractAccountAuthenticator {

        public Authenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->getAccountRemovalAllowed()");
            return super.getAccountRemovalAllowed(response, account);
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            Log.d("bugtags","Authenticator-->editProperties()");
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->addAccount()");
            return null;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->confirmCredentials()");
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->getAuthToken()");
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            Log.d("bugtags","Authenticator-->getAuthTokenLabel()");
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->updateCredentials()");
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
            Log.d("bugtags","Authenticator-->hasFeatures()");
            return null;
        }
    }
}
