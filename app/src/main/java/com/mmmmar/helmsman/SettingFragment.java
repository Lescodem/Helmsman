package com.mmmmar.helmsman;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;


public class SettingFragment extends PreferenceFragment {

    private EditTextPreference pf_edit_service;
    private EditTextPreference pf_edit_characteristic;
    private SettingManager settingManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference_screen);
        pf_edit_service = (EditTextPreference) findPreference(getString(R.string.pf_key_service));
        pf_edit_characteristic = (EditTextPreference) findPreference(getString(R.string.pf_key_characteristic));

        settingManager = SettingManager.getInstance(getActivity());

        pf_edit_service.setSummary(settingManager.getServiceValue());
        pf_edit_characteristic.setSummary(settingManager.getCharacteristicValue());
    }

}
