/*
 * Copyright (C) 2012 ProjectElite
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

package com.android.settings.projectelite;

import android.util.Log;
import android.os.ServiceManager;
import android.content.pm.IPackageManager;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.content.SharedPreferences;
import java.io.File;
import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import android.app.ProgressDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import 	android.preference.SwitchPreference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class GeneralSettings extends SettingsPreferenceFragment {

    private static final String BLOCK_ADS = "blockads";

        private SwitchPreference mBlockAds;

        String rtnString = new String("");
    private static final String LOG_TAG = "GeneralSettings";
   

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.advanced_settings);

    /* Block Ads */
        mBlockAds = (SwitchPreference) findPreference(BLOCK_ADS);
	mBlockAds.setChecked((checkValue(0) == 1) ? true : false);
}
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	boolean value;

    if (preference == mBlockAds) {
		value = mBlockAds.isChecked();
                if (value) {
                    execCommand("blockads", true);
                } else { 
                    execCommand("showads", true);
                }
		return true;
     } 

        
	return false;
	}
public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {}

        public int checkValue(final int getValue) {              
            switch (getValue) {
                case 0: // Block Ads
                    File newHosts = new File("/system/etc/hosts");
                        
                    if (newHosts.length() > 30) {
                        mBlockAds.setSummary(R.string.block_ads_summary_on);

                        return 1;
                    } else {

                        mBlockAds.setSummary(R.string.block_ads_summary_off);
                        return 0;
                    }
            }
            return 0;
        }
       

        public void execCommand(String strCommand, boolean isShort) {                    
            
			try {
            	Process process;
            	
				process = Runtime.getRuntime().exec("su");
	          	DataOutputStream os = new DataOutputStream(process.getOutputStream());
	          	DataInputStream osRes = new DataInputStream(process.getInputStream());

                os.writeBytes(strCommand + "\n");
                os.flush();                
				int x = osRes.available();
				byte[] data = new byte[1];
				
				x = osRes.read(data,0,1);
				rtnString = (new String(data));
				x = osRes.available();
				
				while(x > 0){
					x = osRes.read(data,0,1);
					x = osRes.available();
					rtnString = rtnString + (new String(data));
				}
				
                os.writeBytes("exit\n");
                os.flush();
                os.close();
                osRes.close();
                process.destroy();
               
            } catch (IOException e) {
                // Ignore Exception
            }
        }
}
