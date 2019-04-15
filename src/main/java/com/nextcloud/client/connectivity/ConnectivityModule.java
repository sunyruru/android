package com.nextcloud.client.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;

import com.nextcloud.client.account.CurrentAccountProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ConnectivityModule {

    @Provides
    NetworkStateProvider networkStateProvider(Context context, ConnectivityManager connectivityManager, CurrentAccountProvider currentAccountProvider) {
        return new NetworkStateProviderImpl(context, connectivityManager, currentAccountProvider);
    }
}
