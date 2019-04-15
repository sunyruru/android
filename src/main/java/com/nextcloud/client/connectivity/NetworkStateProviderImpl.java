package com.nextcloud.client.connectivity;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.Device;
import com.nextcloud.client.account.CurrentAccountProvider;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

public class NetworkStateProviderImpl implements NetworkStateProvider {

    private static final String TAG = NetworkStateProviderImpl.class.getSimpleName();

    private Context context;
    private ConnectivityManager connectivityManager;
    private CurrentAccountProvider currentAccountProvider;

    @Inject
    NetworkStateProviderImpl(Context context, ConnectivityManager connectivityManager, CurrentAccountProvider currentAccountProvider) {
        this.context = context;
        this.connectivityManager = connectivityManager;
        this.currentAccountProvider = currentAccountProvider;
    }

    @Override
    public boolean isWalled() {
        if (isUnmetered()) {
            try {
                Account account = currentAccountProvider.getCurrentAccount();
                if (account != null) {
                    OwnCloudAccount ocAccount = new OwnCloudAccount(account, context);
                    OwnCloudVersion serverVersion = AccountUtils.getServerVersion(account);

                    String url;
                    if (serverVersion.compareTo(OwnCloudVersion.nextcloud_13) > 0) {
                        url = ocAccount.getBaseUri() + "/index.php/204";
                    } else {
                        url = ocAccount.getBaseUri() + "/status.php";
                    }

                    GetMethod get = new GetMethod(url);
                    OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(account, context);

                    int status = client.executeMethod(get);

                    if (serverVersion.compareTo(OwnCloudVersion.nextcloud_13) > 0) {
                        return !(status == HttpStatus.SC_NO_CONTENT &&
                            (get.getResponseContentLength() == -1 || get.getResponseContentLength() == 0));
                    } else {
                        if (status == HttpStatus.SC_OK) {
                            try {
                                // try parsing json to verify response
                                // check if json contains maintenance and it should be false

                                String json = get.getResponseBodyAsString();
                                return new JSONObject(json).getBoolean("maintenance");
                            } catch (Exception e) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                Log_OC.e(TAG, "Error checking internet connection", e);
            } catch (com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException e) {
                Log_OC.e(TAG, "Account not found", e);
            } catch (OperationCanceledException | AuthenticatorException e) {
                Log_OC.e(TAG, e.getMessage());
            }
        } else {
            return Device.getNetworkType(context).equals(JobRequest.NetworkType.ANY);
        }

        return true;
    }

    @Override
    public boolean isUnmetered() {
        try {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork.isConnectedOrConnecting()) {
                switch (activeNetwork.getType()) {
                    case ConnectivityManager.TYPE_VPN:
                        // check if any other network is wifi
                        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
                            if (networkInfo.isConnectedOrConnecting() &&
                                networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                return true;
                            }
                        }
                        return false;

                    case ConnectivityManager.TYPE_WIFI:
                        return true;

                    default:
                        return false;
                }
            } else {
                return false;
            }
        } catch (NullPointerException exception) {
            return false;
        }
    }
}
