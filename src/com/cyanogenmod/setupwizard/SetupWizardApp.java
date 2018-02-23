/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.setupwizard;


import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class SetupWizardApp extends Application {

    public static final String TAG = SetupWizardApp.class.getSimpleName();
    // Leave this off for release
    public static final boolean DEBUG = false;

    public static final String ACTION_FINISHED = "com.cyanogenmod.setupwizard.SETUP_FINISHED";

    public static final String ACTION_SETUP_WIFI = "com.android.net.wifi.SETUP_WIFI_NETWORK";

    public static final String ACTION_SETUP_FINGERPRINT = "android.settings.FINGERPRINT_SETUP";
    public static final String ACTION_SETUP_LOCKSCREEN = "com.android.settings.SETUP_LOCK_SCREEN";

    public static final String EXTRA_FIRST_RUN = "firstRun";
    public static final String EXTRA_ALLOW_SKIP = "allowSkip";
    public static final String EXTRA_AUTO_FINISH = "wifi_auto_finish_on_connect";
    public static final String EXTRA_USE_IMMERSIVE = "useImmersiveMode";
    public static final String EXTRA_THEME = "theme";
    public static final String EXTRA_MATERIAL_LIGHT = "material_light";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DETAILS = "details";

    public static final String KEY_DETECT_CAPTIVE_PORTAL = "captive_portal_detection_enabled";
    public static final String KEY_SEND_METRICS = "send_metrics";
    public static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    public static final String KEY_APPLY_DEFAULT_THEME = "apply_default_theme";
    public static final String KEY_BUTTON_BACKLIGHT = "pre_navbar_button_backlight";
    public static final String KEY_PRIVACY_GUARD = "privacy_guard_default";

    private static final String[] THEME_PACKAGES = {
            "org.cyanogenmod.theme.chooser",
            "com.cyngn.theme.chooser",
            "com.cyngn.themestore"
    };

    public static final int REQUEST_CODE_SETUP_WIFI = 0;
    public static final int REQUEST_CODE_SETUP_CAPTIVE_PORTAL= 4;
    public static final int REQUEST_CODE_SETUP_BLUETOOTH= 5;
    public static final int REQUEST_CODE_UNLOCK = 6;
    public static final int REQUEST_CODE_SETUP_FINGERPRINT = 7;
    public static final int REQUEST_CODE_SETUP_LOCKSCREEN = 9;

    public static final int RADIO_READY_TIMEOUT = 10 * 1000;

    private boolean mIsRadioReady = false;
    private boolean mIsAuthorized = false;
    private boolean mIgnoreSimLocale = false;

    private final Bundle mSettingsBundle = new Bundle();
    private final Handler mHandler = new Handler();

    private final Runnable mRadioTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mIsRadioReady = true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        final boolean isOwner = SetupWizardUtils.isOwner();
        if (!isOwner) {
            Thread t = new Thread(){
                @Override
                public void run() {
                    disableThemeComponentsForSecondaryUser();
                }
            };
            t.run();
        }  else {
            disableCaptivePortalDetection();
        }
        mHandler.postDelayed(mRadioTimeoutRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
    }

    public boolean isRadioReady() {
        return mIsRadioReady;
    }

    public void setRadioReady(boolean radioReady) {
        if (!mIsRadioReady && radioReady) {
            mHandler.removeCallbacks(mRadioTimeoutRunnable);
        }
        mIsRadioReady = radioReady;
    }

    public boolean isAuthorized() {
        return mIsAuthorized;
    }

    public void setIsAuthorized(boolean isAuthorized) {
        mIsAuthorized = isAuthorized;
    }

    public boolean ignoreSimLocale() {
        return mIgnoreSimLocale;
    }

    public void setIgnoreSimLocale(boolean ignoreSimLocale) {
        mIgnoreSimLocale = ignoreSimLocale;
    }

    public Bundle getSettingsBundle() {
        return mSettingsBundle;
    }

    public void disableCaptivePortalDetection() {
        Settings.Global.putInt(getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 0);
    }

    public void enableCaptivePortalDetection() {
        Settings.Global.putInt(getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 1);
    }

    private void disableThemeComponentsForSecondaryUser() {
        PackageManager pm = getPackageManager();
        for(String pkgName : THEME_PACKAGES) {
            try {
                pm.getApplicationInfo(pkgName, 0);
                pm.setApplicationEnabledSetting(pkgName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // don't care
            }
        }
    }
}
