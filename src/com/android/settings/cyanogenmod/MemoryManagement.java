/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import java.io.File;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class MemoryManagement extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";

    public static final String KSM_PREF = "pref_ksm";

    public static final String KSM_PREF_DISABLED = "0";

    public static final String KSM_PREF_ENABLED = "1";

    private static final String ZRAM_PREF = "pref_zram_size";

    private static final String ZRAM_PERSIST_PROP = "persist.service.zram"; // was compcache

    private static final String ZRAM_DEFAULT = SystemProperties.get("ro.zram.default"); // was compcache

    private static final String HEAPSIZE_PREF = "pref_heapsize";

    private static final String HEAPSIZE_PROP = "dalvik.vm.heapsize";

    private static final String HEAPSIZE_PERSIST_PROP = "persist.sys.vm.heapsize";

    private static final String HEAPSIZE_DEFAULT = "16m";

    private ListPreference mzRAM;

    private CheckBoxPreference mKSMPref;

    private ListPreference mHeapsizePref;

    private int swapAvailable = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {

            addPreferencesFromResource(R.xml.memory_management);

            PreferenceScreen prefSet = getPreferenceScreen();

            mzRAM = (ListPreference) prefSet.findPreference(ZRAM_PREF);
            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);
            mHeapsizePref = (ListPreference) prefSet.findPreference(HEAPSIZE_PREF);

            if (isSwapAvailable()) {
                if (SystemProperties.get(ZRAM_PERSIST_PROP) == "1")
                    SystemProperties.set(ZRAM_PERSIST_PROP, ZRAM_DEFAULT);
                mzRAM.setValue(SystemProperties.get(ZRAM_PERSIST_PROP, ZRAM_DEFAULT));
                mzRAM.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mzRAM);
            }

            if (Utils.fileExists(KSM_RUN_FILE)) {
                mKSMPref.setChecked(KSM_PREF_ENABLED.equals(Utils.fileReadOneLine(KSM_RUN_FILE)));
            } else {
                prefSet.removePreference(mKSMPref);
            }

            mHeapsizePref.setValue(SystemProperties.get(HEAPSIZE_PERSIST_PROP,
                    SystemProperties.get(HEAPSIZE_PROP, HEAPSIZE_DEFAULT)));
            mHeapsizePref.setOnPreferenceChangeListener(this);

        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mKSMPref) {
            Utils.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeapsizePref) {
            if (newValue != null) {
                SystemProperties.set(HEAPSIZE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }

        if (preference == mzRAM) {
            if (newValue != null) {
                SystemProperties.set(ZRAM_PERSIST_PROP, (String) newValue);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if swap support is available on the system
     */
    private boolean isSwapAvailable() {
        if (swapAvailable < 0) {
            swapAvailable = new File("/proc/swaps").exists() ? 1 : 0;
        }
        return swapAvailable > 0;
    }
}