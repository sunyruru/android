package com.nextcloud.client.whatsnew;

import android.content.Context;
import android.content.res.Resources;

import com.nextcloud.client.account.CurrentAccountProvider;
import com.nextcloud.client.appinfo.AppInfo;
import com.nextcloud.client.preferences.AppPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class WhatsNewModule {

    @Provides
    @Singleton
    WhatsNewService whatsNewService(
        Resources resources,
        AppPreferences preferences,
        CurrentAccountProvider accountProvider,
        AppInfo appInfo
    ) {
        return new WhatsNewService(resources, preferences, accountProvider, appInfo);
    }
}
