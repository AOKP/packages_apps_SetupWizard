/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package com.cyanogenmod.setupwizard.ui;

import static com.cyanogenmod.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_APPLY_DEFAULT_THEME;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_PRIVACY_GUARD;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_SEND_METRICS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ThemeConfig;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.setupwizardlib.util.WizardManagerHelper;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import cyanogenmod.hardware.CMHardwareManager;
import cyanogenmod.providers.CMSettings;

public class LineageSettingsActivity extends BaseSetupWizardActivity {

    public static final String TAG = LineageSettingsActivity.class.getSimpleName();

    public static final String PRIVACY_POLICY_URI = "http://lineageos.org/legal";

    private SetupWizardApp mSetupWizardApp;

    private View mMetricsRow;
    private View mDefaultThemeRow;
    private View mNavKeysRow;
    private View mPrivacyGuardRow;
    private CheckBox mMetrics;
    private CheckBox mDefaultTheme;
    private CheckBox mNavKeys;
    private CheckBox mPrivacyGuard;

    private boolean mHideNavKeysRow = false;
    private boolean mHideThemeRow = false;

    private View.OnClickListener mMetricsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mMetrics.isChecked();
            mMetrics.setChecked(checked);
            mSetupWizardApp.getSettingsBundle().putBoolean(KEY_SEND_METRICS, checked);
        }
    };

    private View.OnClickListener mDefaultThemeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mDefaultTheme.isChecked();
            mDefaultTheme.setChecked(checked);
            mSetupWizardApp.getSettingsBundle().putBoolean(KEY_APPLY_DEFAULT_THEME, checked);
        }
    };

    private View.OnClickListener mNavKeysClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mNavKeys.isChecked();
            mNavKeys.setChecked(checked);
            mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
        }
    };

    private View.OnClickListener mPrivacyGuardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mPrivacyGuard.isChecked();
            mPrivacyGuard.setChecked(checked);
            mSetupWizardApp.getSettingsBundle().putBoolean(KEY_PRIVACY_GUARD, checked);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setContentView(R.layout.setup_lineage_settings);
        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(R.string.setup_services);
        ImageView icon = (ImageView) findViewById(R.id.header_icon);
        icon.setImageResource(R.drawable.ic_features);
        icon.setVisibility(View.VISIBLE);
        setNextText(R.string.next);
        String privacy_policy = getString(R.string.services_privacy_policy);
        String policySummary = getString(R.string.services_explanation, privacy_policy);
        SpannableString ss = new SpannableString(policySummary);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // At this point of the setup, the device has already been unlocked (if frp
                // had been enabled), so there should be no issues regarding security
                final Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(PRIVACY_POLICY_URI));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to start activity " + intent.toString(), e);
                }
            }
        };
        ss.setSpan(clickableSpan,
                policySummary.length() - privacy_policy.length() - 1,
                policySummary.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView privacyPolicy = (TextView) findViewById(R.id.privacy_policy);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        privacyPolicy.setText(ss);

        mMetricsRow = findViewById(R.id.metrics);
        mMetricsRow.setOnClickListener(mMetricsClickListener);
        String metricsHelpImproveCM =
                getString(R.string.services_help_improve_cm, getString(R.string.os_name));
        String metricsSummary = getString(R.string.services_metrics_label,
                metricsHelpImproveCM, getString(R.string.os_name));
        final SpannableStringBuilder metricsSpan = new SpannableStringBuilder(metricsSummary);
        metricsSpan.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, metricsHelpImproveCM.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView metrics = (TextView) findViewById(R.id.enable_metrics_summary);
        metrics.setText(metricsSpan);
        mMetrics = (CheckBox) findViewById(R.id.enable_metrics_checkbox);

        mDefaultThemeRow = findViewById(R.id.theme);
        mHideThemeRow = hideThemeSwitch(this);
        if (mHideThemeRow) {
            mDefaultThemeRow.setVisibility(View.GONE);
        } else {
            mDefaultThemeRow.setOnClickListener(mDefaultThemeClickListener);
            String defaultTheme =
                    getString(R.string.services_apply_theme,
                            getString(R.string.default_theme_name));
            String defaultThemeSummary = getString(R.string.services_apply_theme_label,
                    defaultTheme);
            final SpannableStringBuilder themeSpan =
                    new SpannableStringBuilder(defaultThemeSummary);
            themeSpan.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0, defaultTheme.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView theme = (TextView) findViewById(R.id.enable_theme_summary);
            theme.setText(themeSpan);
            mDefaultTheme = (CheckBox) findViewById(R.id.enable_theme_checkbox);
        }

        mNavKeysRow = findViewById(R.id.nav_keys);
        mNavKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = (CheckBox) findViewById(R.id.nav_keys_checkbox);
        boolean needsNavBar = true;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            needsNavBar = windowManager.needsNavigationBar();
        } catch (RemoteException e) {
        }
        mHideNavKeysRow = hideKeyDisabler(this);
        if (mHideNavKeysRow || needsNavBar) {
            mNavKeysRow.setVisibility(View.GONE);
        } else {
            boolean navKeysDisabled = isKeyDisablerActive(this);
            mNavKeys.setChecked(navKeysDisabled);
        }

        mPrivacyGuardRow = findViewById(R.id.privacy_guard);
        mPrivacyGuardRow.setOnClickListener(mPrivacyGuardClickListener);
        mPrivacyGuard = (CheckBox) findViewById(R.id.privacy_guard_checkbox);
        mPrivacyGuard.setChecked(CMSettings.Secure.getInt(getContentResolver(),
                CMSettings.Secure.PRIVACY_GUARD_DEFAULT, 0) == 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisableNavkeysOption();
        updateMetricsOption();
        updateThemeOption();
        updatePrivacyGuardOption();
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        startActivityForResult(intent, 1);
    }

    private void updateMetricsOption() {
        final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
        boolean metricsChecked =
                !myPageBundle.containsKey(KEY_SEND_METRICS) || myPageBundle
                        .getBoolean(KEY_SEND_METRICS);
        mMetrics.setChecked(metricsChecked);
        myPageBundle.putBoolean(KEY_SEND_METRICS, metricsChecked);
    }

    private void updateThemeOption() {
        if (!mHideThemeRow) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean themesChecked;
            if (myPageBundle.containsKey(KEY_APPLY_DEFAULT_THEME)) {
                themesChecked = myPageBundle.getBoolean(KEY_APPLY_DEFAULT_THEME);
            } else {
                themesChecked = getResources().getBoolean(
                        R.bool.check_custom_theme_by_default);
            }
            mDefaultTheme.setChecked(themesChecked);
            myPageBundle.putBoolean(KEY_APPLY_DEFAULT_THEME, themesChecked);
        }
    }

    private void updateDisableNavkeysOption() {
        if (!mHideNavKeysRow) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean enabled = CMSettings.Secure.getInt(getContentResolver(),
                    CMSettings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0;
            boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                    enabled;
            mNavKeys.setChecked(checked);
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
        }
    }

    private void updatePrivacyGuardOption() {
        final Bundle bundle = mSetupWizardApp.getSettingsBundle();
        boolean enabled = CMSettings.Secure.getInt(getContentResolver(),
                CMSettings.Secure.PRIVACY_GUARD_DEFAULT, 0) != 0;
        boolean checked = bundle.containsKey(KEY_PRIVACY_GUARD) ?
                bundle.getBoolean(KEY_PRIVACY_GUARD) :
                enabled;
        mPrivacyGuard.setChecked(checked);
        bundle.putBoolean(KEY_PRIVACY_GUARD, checked);
    }

    private static boolean hideKeyDisabler(Context context) {
        final CMHardwareManager hardware = CMHardwareManager.getInstance(context);
        return !hardware.isSupported(CMHardwareManager.FEATURE_KEY_DISABLE);
    }

    private static boolean isKeyDisablerActive(Context context) {
        final CMHardwareManager hardware = CMHardwareManager.getInstance(context);
        return hardware.get(CMHardwareManager.FEATURE_KEY_DISABLE);
    }

    private static boolean hideThemeSwitch(Context context) {
        return SetupWizardUtils.getDefaultThemePackageName(context)
                .equals(ThemeConfig.SYSTEM_DEFAULT);
    }
}
