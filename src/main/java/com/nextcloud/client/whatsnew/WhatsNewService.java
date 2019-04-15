package com.nextcloud.client.whatsnew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.nextcloud.client.account.CurrentAccountProvider;
import com.nextcloud.client.appinfo.AppInfo;
import com.nextcloud.client.preferences.AppPreferences;
import com.owncloud.android.R;
import com.owncloud.android.features.FeatureItem;
import com.owncloud.android.ui.activity.PassCodeActivity;

public class WhatsNewService {

    private Resources resources;
    private AppPreferences preferences;
    private CurrentAccountProvider accountProvider;
    private AppInfo appInfo;

    WhatsNewService(Resources resources, AppPreferences preferences, CurrentAccountProvider accountProvider, AppInfo appInfo) {
        this.resources = resources;
        this.preferences = preferences;
        this.accountProvider = accountProvider;
        this.appInfo = appInfo;
    }

    public void launchActivityIfNeeded(Activity activity) {
        if (!resources.getBoolean(R.bool.show_whats_new) || activity instanceof WhatsNewActivity) {
            return;
        }

        if (shouldShow(activity)) {
            activity.startActivity(new Intent(activity, WhatsNewActivity.class));
        }
    }

    FeatureItem[] getWhatsNew() {
        int itemVersionCode = 30030099;

        if (!isFirstRun() && appInfo.getVersionCode() >= itemVersionCode
            && preferences.getLastSeenVersionCode() < itemVersionCode) {
            return new FeatureItem[]{new FeatureItem(R.drawable.whats_new_device_credentials,
                                                     R.string.whats_new_device_credentials_title, R.string.whats_new_device_credentials_content,
                                                     false, false)};
        } else {
            return new FeatureItem[0];
        }
    }

    private boolean shouldShow(Context callingContext) {
        return !(callingContext instanceof PassCodeActivity) && getWhatsNew().length > 0;
    }

    public boolean isFirstRun() {
        return accountProvider.getCurrentAccount() == null;
    }
}
