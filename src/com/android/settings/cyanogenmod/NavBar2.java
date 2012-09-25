/*
 * Copyright (C) 2012 Android Developer Alliance
 * Copyright (C) 2012 Hiemanshu Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.ada;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

public class NavBar2 extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String ENABLE_NAVIGATION_BAR = "enable_navigation_bar";
    private static final String NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    private static final String NAVIGATION_BAR_WIDTH = "navigation_bar_width";

    public static final String PREFS_NAV_BAR = "navbar";

    CheckBoxPreference mEnableNavigationBar;
    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarHeightLandscape;
    ListPreference mNavigationBarWidth;

    private static final String TAG = "NavBar";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();

        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        mEnableNavigationBar = (CheckBoxPreference) findPreference("enable_nav_bar");
        mEnableNavigationBar.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1);
      
        // don't allow devices that must use a navigation bar to disable it
        if (hasNavBarByDefault) {
            prefs.removePreference(mEnableNavigationBar);
        }

        mNavigationBarHeight = (ListPreference) findPreference("navigation_bar_height");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarHeightLandscape = (ListPreference) findPreference("navigation_bar_height_landscape");
        mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);

        mNavigationBarWidth = (ListPreference) findPreference("navigation_bar_width");
        mNavigationBarWidth.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableNavigationBar) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBarWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int width = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH,
                    width);
            createRebootDialog().show();
            toggleBar();
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT,
                    height);
            createRebootDialog().show();
            toggleBar();
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE,
                    height);
            createRebootDialog().show();
            toggleBar();
            return true;

        }

        return false;
    }

    public void toggleBar() {
        boolean isBarOn = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, 1) == 1;
        Settings.System.putInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, isBarOn ? 0 : 1);
        Settings.System.putInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, isBarOn ? 1 : 0);
    }

    public int mapChosenDpToPixels(int dp) {
        switch (dp) {
            case 54:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_54);
            case 48:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_48);
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_42);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_36);
            case 30:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_30);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_24);
        }
        return -1;
    }

    public AlertDialog createRebootDialog() {
        AlertDialog diag = new AlertDialog.Builder(getActivity())
            .setTitle(getResources().getString(R.string.navbar_reboot_title))
            .setMessage(
                getResources().getString(R.string.navbar_reboot_message))
            .setCancelable(false)
            .setNeutralButton(
                getResources().getString(R.string.navbar_reboot_no),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
            .setPositiveButton(
                getResources().getString(R.string.navbar_reboot_yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        PowerManager pm = (PowerManager) getActivity()
                            .getSystemService(Context.POWER_SERVICE);
                        pm.reboot("Rebooting with new NavBar height");
                    }
                })
            .create();
        return diag;
    }

}
