/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.qs.tiles;

import static com.android.internal.logging.MetricsLogger.VIEW_UNKNOWN;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.service.quicksettings.Tile;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.animation.Expandable;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QsEventLogger;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.res.R;

import android.content.Intent;

import android.util.Log;

import javax.inject.Inject;

/** Quick settings tile: Spectrum kernel profile switcher **/
public class SpectrumTile extends QSTileImpl<BooleanState> {

    public static final String TILE_SPEC = "spectrum";

    // System domain property — no root/su required
    // SELinux: platform_app gets set_prop(system_prop) via vendor sepolicy
    // init.spectrum.rc triggers on this property to apply kernel profiles
    private static final String SPECTRUM_PROP = "persist.sys.spectrum.profile";

    // Profile names matching init.spectrum.rc
    private static final String[] PROFILE_NAMES = {
        "Balance",      // 0
        "Performance",  // 1
        "Battery",      // 2
        "Gaming"        // 3
    };

    private static final int[] PROFILE_ICONS = {
        R.drawable.ic_qs_spectrum_balance,
        R.drawable.ic_qs_spectrum_performance,
        R.drawable.ic_qs_spectrum_battery,
        R.drawable.ic_qs_spectrum_gaming
    };

    private int mCurrentProfile = 0;

    private static final String TAG = "SpectrumTile";

    /**
     * Set spectrum profile via SystemProperties.set() directly.
     * No su/root needed — property is in system_prop domain,
     * platform_app has set_prop permission via device sepolicy.
     */
    private void setSpectrumProfile(int profile) {
        try {
            SystemProperties.set(SPECTRUM_PROP, String.valueOf(profile));
            Log.i(TAG, "Set spectrum profile to " + profile + " (" + PROFILE_NAMES[profile] + ")");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set spectrum profile", e);
        }
    }


    @Inject
    public SpectrumTile(
            QSHost host,
            QsEventLogger uiEventLogger,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger
    ) {
        super(host, uiEventLogger, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        mCurrentProfile = getCurrentProfile();
    }

    private int getCurrentProfile() {
        try {
            return Integer.parseInt(SystemProperties.get(SPECTRUM_PROP, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick(@Nullable Expandable expandable) {
        mCurrentProfile = (mCurrentProfile + 1) % PROFILE_NAMES.length;
        setSpectrumProfile(mCurrentProfile);
        refreshState();
    }

    @Override
    protected void handleLongClick(@Nullable Expandable expandable) {
        // Long press resets to Balance (profile 0)
        mCurrentProfile = 0;
        setSpectrumProfile(0);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_spectrum_label);
    }

    @Override
    public int getMetricsCategory() {
        return VIEW_UNKNOWN;
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (listening) {
            mCurrentProfile = getCurrentProfile();
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        mCurrentProfile = getCurrentProfile();
        state.value = (mCurrentProfile != 0); // active if not default
        state.icon = ResourceIcon.get(PROFILE_ICONS[mCurrentProfile]);
        state.label = mContext.getString(R.string.quick_settings_spectrum_label);
        state.secondaryLabel = PROFILE_NAMES[mCurrentProfile];
        state.contentDescription = mContext.getString(
                R.string.quick_settings_spectrum_label) + ": " + PROFILE_NAMES[mCurrentProfile];
        state.state = (mCurrentProfile == 0) ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE;
    }
}
