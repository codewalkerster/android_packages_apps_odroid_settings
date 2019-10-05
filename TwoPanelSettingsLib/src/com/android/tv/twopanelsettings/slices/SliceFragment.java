/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.tv.twopanelsettings.slices;

import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;
import static android.app.slice.Slice.HINT_PARTIAL;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.SliceContent;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.tv.twopanelsettings.R;
import com.android.tv.twopanelsettings.TwoPanelSettingsFragment;
import com.android.tv.twopanelsettings.TwoPanelSettingsFragment.SliceFragmentCallback;
import com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData.SliceLiveDataImpl;
import com.android.tv.twopanelsettings.slices.SlicePreferencesUtil.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A screen presenting a slice in TV settings.
 */
@Keep
public class SliceFragment extends SettingsPreferenceFragment implements Observer<Slice>,
        SliceFragmentCallback {
    private static final int SLICE_REQUEST_CODE = 10000;
    private static final String TAG = "SliceFragment";
    private ListContent mListContent;
    private Slice mSlice;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mUriString = null;
    private CharSequence mScreenTitle;
    private CharSequence mScreenSubtitle;
    private Icon mScreenIcon;
    private PendingIntent mPreferenceFollowupIntent;
    private int mFollowupPendingIntentResultCode;
    private Intent mFollowupPendingIntentExtras;
    private String mLastFocusedPreferenceKey;

    private static final String KEY_PREFERENCE_FOLLOWUP_INTENT = "key_preference_followup_intent";
    private static final String KEY_PREFERENCE_FOLLOWUP_RESULT_CODE =
            "key_preference_followup_result_code";
    private static final String KEY_SCREEN_TITLE = "key_screen_title";
    private static final String KEY_SCREEN_SUBTITLE = "key_screen_subtitle";
    private static final String KEY_SCREEN_ICON = "key_screen_icon";
    private static final String KEY_LAST_PREFERENCE = "key_last_preference";
    private static final String KEY_URI_STRING = "key_uri_string";

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            handleUri(uri);
            super.onChange(selfChange, uri);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mUriString = getArguments().getString(SlicesConstants.TAG_TARGET_URI);
        ContextSingleton.getInstance().grantFullAccess(getContext(), Uri.parse(mUriString));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        this.setTitle(mScreenTitle);
        this.setSubtitle(mScreenSubtitle);
        this.setIcon(mScreenIcon);
        this.getPreferenceScreen().removeAll();

        getSliceLiveData().observeForever(this);
        if (TextUtils.isEmpty(mScreenTitle)) {
            mScreenTitle = getArguments().getCharSequence(SlicesConstants.TAG_SCREEN_TITLE, "");
        }
        showProgressBar();
        super.onResume();
        getContext().getContentResolver().registerContentObserver(
                SlicePreferencesUtil.getStatusPath(mUriString), false, mContentObserver);
        fireFollowupPendingIntent();
    }

    private SliceLiveDataImpl getSliceLiveData() {
        return ContextSingleton.getInstance()
                .getSliceLiveData(getActivity(), Uri.parse(mUriString));
    }

    private void fireFollowupPendingIntent() {
        if (mPreferenceFollowupIntent == null) {
            return;
        }
        try {
            mPreferenceFollowupIntent.send(getContext(),
                    mFollowupPendingIntentResultCode, mFollowupPendingIntentExtras);
        } catch (CanceledException e) {
            Log.e(TAG, "Followup PendingIntent for slice cannot be sent", e);
        }
        mPreferenceFollowupIntent = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        getSliceLiveData().removeObserver(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceScreen preferenceScreen = getPreferenceManager()
                .createPreferenceScreen(getContext());
        setPreferenceScreen(preferenceScreen);

        TypedValue themeTypedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        mContextThemeWrapper = new ContextThemeWrapper(getActivity(), themeTypedValue.resourceId);

    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.VIEW_UNKNOWN;
    }

    private void update() {
        // TODO: Remove ListContent
        mListContent = new ListContent(mSlice);
        // TODO: Compare and update the existing preferences instead of rebuilding the screen.
        PreferenceScreen preferenceScreen =
                getPreferenceManager().getPreferenceScreen();

        if (preferenceScreen == null) {
            return;
        }

        List<SliceContent> items = mListContent.getRowItems();
        if (items == null || items.size() == 0) {
            return;
        }

        SliceItem screenTitleItem = SlicePreferencesUtil.getScreenTitleItem(items);
        if (screenTitleItem == null) {
            setTitle(mScreenTitle);
        } else {
            Data data = SlicePreferencesUtil.extract(screenTitleItem);
            CharSequence title = SlicePreferencesUtil.getText(data.mTitleItem);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
                mScreenTitle = title;
            } else {
                setTitle(mScreenTitle);
            }

            CharSequence subtitle = SlicePreferencesUtil.getText(data.mSubtitleItem);
            setSubtitle(subtitle);

            Icon icon = SlicePreferencesUtil.getIcon(data.mStartItem);
            setIcon(icon);
        }

        List<Preference> newPrefs = new ArrayList<>();
        for (SliceContent contentItem : items) {
            SliceItem item = contentItem.getSliceItem();
            Preference preference = SlicePreferencesUtil.getPreference(item, mContextThemeWrapper,
                    getClass().getCanonicalName());
            if (preference != null) {
                newPrefs.add(preference);
            }
        }
        updatePreferenceScreen(preferenceScreen, newPrefs);
        if (mLastFocusedPreferenceKey != null) {
            scrollToPreference(mLastFocusedPreferenceKey);
        }
    }


    private void back() {
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            TwoPanelSettingsFragment parentFragment =
                    (TwoPanelSettingsFragment) getCallbackFragment();
            if (parentFragment.isFragmentInTheMainPanel(this)) {
                parentFragment.navigateBack();
            }
        }
    }

    private void forward() {
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            TwoPanelSettingsFragment parentFragment =
                    (TwoPanelSettingsFragment) getCallbackFragment();
            if (parentFragment.isFragmentInTheMainPanel(this)) {
                parentFragment.navigateToPreviewFragment();
            }
        }
    }

    private void updatePreferenceScreen(PreferenceScreen screen, List<Preference> newPrefs) {
        // Remove all the preferences in the screen that satisfy such two cases:
        // (a) Preference without key
        // (b) Preference with key which does not appear in the new list.
        int index = 0;
        while (index < screen.getPreferenceCount()) {
            boolean needToRemoveCurrentPref = true;
            Preference oldPref = screen.getPreference(index);
            if (oldPref.getKey() != null) {
                for (Preference newPref : newPrefs) {
                    if (newPref.getKey() != null && newPref.getKey().equals(oldPref.getKey())) {
                        needToRemoveCurrentPref = false;
                        break;
                    }
                }
            }
            if (needToRemoveCurrentPref) {
                screen.removePreference(oldPref);
            } else {
                index++;
            }
        }

        //Iterate the new preferences list and give each preference a correct order
        for (int i = 0; i < newPrefs.size(); i++) {
            Preference newPref = newPrefs.get(i);
            boolean neededToAddNewPref = true;
            // If the newPref has a key and has a corresponding old preference, update the old
            // preference and give it a new order.
            if (newPref.getKey() != null) {
                for (int j = 0; j < screen.getPreferenceCount(); j++) {
                    Preference oldPref = screen.getPreference(j);
                    if (oldPref.getKey() != null && oldPref.getKey().equals(newPref.getKey())) {
                        oldPref.setIcon(newPref.getIcon());
                        oldPref.setTitle(newPref.getTitle());
                        oldPref.setSummary(newPref.getSummary());
                        oldPref.setEnabled(newPref.isEnabled());
                        if ((oldPref instanceof TwoStatePreference)
                                && (newPref instanceof TwoStatePreference)) {
                            ((TwoStatePreference) oldPref)
                                    .setChecked(((TwoStatePreference) newPref).isChecked());
                        }
                        if ((oldPref instanceof HasSliceAction)
                                && (newPref instanceof HasSliceAction)) {
                            ((HasSliceAction) oldPref)
                                    .setSliceAction(((HasSliceAction) newPref).getSliceAction());
                        }
                        oldPref.setOrder(i);
                        neededToAddNewPref = false;
                        break;
                    }
                }
            }
            // If the newPref cannot find a corresponding old preference, or it does not have a key,
            // add it to the screen with the correct order.
            if (neededToAddNewPref) {
                newPref.setOrder(i);
                screen.addPreference(newPref);
            }
        }
    }

    @Override
    public void onPreferenceFocused(Preference preference) {
        setLastFocused(preference);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof SliceRadioPreference) {
            SliceRadioPreference radioPref = (SliceRadioPreference) preference;
            if (!radioPref.isChecked()) {
                radioPref.setChecked(true);
                return true;
            }

            try {
                SliceActionImpl action = radioPref.getSliceAction();
                PendingIntent pendingIntent = action.getAction();
                Intent fillInIntent =
                        new Intent().putExtra(EXTRA_PREFERENCE_KEY, preference.getKey());
                IntentSender intentSender = pendingIntent.getIntentSender();
                startIntentSenderForResult(
                        intentSender, SLICE_REQUEST_CODE, fillInIntent, 0, 0, 0, null);
                for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                    Preference pref = getPreferenceScreen().getPreference(i);
                    if (pref instanceof SliceRadioPreference && pref != preference) {
                        ((SliceRadioPreference) pref).setChecked(false);
                    }
                }
            } catch (SendIntentException e) {
                Log.e(TAG, "PendingIntent for slice cannot be sent", e);
            }
        } else if (preference instanceof TwoStatePreference
                && preference instanceof HasSliceAction) {
            // TODO - Show loading indicator here?
            try {
                SliceActionImpl action = ((HasSliceAction) preference).getSliceAction();
                if (action.isToggle()) {
                    // Update the intent extra state
                    boolean isChecked = ((TwoStatePreference) preference).isChecked();
                    Intent i = new Intent()
                            .putExtra(EXTRA_TOGGLE_STATE, isChecked)
                            .putExtra(EXTRA_PREFERENCE_KEY, preference.getKey());
                    action.getActionItem().fireAction(getContext(), i);
                } else {
                    action.getActionItem().fireAction(null, null);
                }

            } catch (PendingIntent.CanceledException e) {
                ((TwoStatePreference) preference).setChecked(
                        !((TwoStatePreference) preference).isChecked());
                Log.e(TAG, "PendingIntent for slice cannot be sent", e);
            }
            return true;
        } else if (preference instanceof SlicePreference) {
            SlicePreference actionPref = (SlicePreference) preference;
            if (actionPref.getSliceAction() != null) {
                try {
                    SliceActionImpl action = actionPref.getSliceAction();
                    PendingIntent pendingIntent = action.getAction();
                    Intent fillInIntent =
                            new Intent().putExtra(EXTRA_PREFERENCE_KEY, preference.getKey());
                    IntentSender intentSender = pendingIntent.getIntentSender();
                    startIntentSenderForResult(
                            intentSender, SLICE_REQUEST_CODE, fillInIntent, 0, 0, 0, null);
                    if (actionPref.getFollowupSliceAction() != null) {
                        mPreferenceFollowupIntent = actionPref.getFollowupSliceAction().getAction();
                    }
                } catch (SendIntentException e) {
                    Log.e(TAG, "PendingIntent for slice cannot be sent", e);
                }
                return true;
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            return;
        }
        mFollowupPendingIntentExtras = data;
        mFollowupPendingIntentResultCode = resultCode;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PREFERENCE_FOLLOWUP_INTENT, mPreferenceFollowupIntent);
        outState.putInt(KEY_PREFERENCE_FOLLOWUP_RESULT_CODE, mFollowupPendingIntentResultCode);
        outState.putCharSequence(KEY_SCREEN_TITLE, mScreenTitle);
        outState.putCharSequence(KEY_SCREEN_SUBTITLE, mScreenSubtitle);
        outState.putParcelable(KEY_SCREEN_ICON, mScreenIcon);
        outState.putString(KEY_LAST_PREFERENCE, mLastFocusedPreferenceKey);
        outState.putString(KEY_URI_STRING, mUriString);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPreferenceFollowupIntent =
                    savedInstanceState.getParcelable(KEY_PREFERENCE_FOLLOWUP_INTENT);
            mFollowupPendingIntentResultCode =
                    savedInstanceState.getInt(KEY_PREFERENCE_FOLLOWUP_RESULT_CODE);
            mScreenTitle = savedInstanceState.getCharSequence(KEY_SCREEN_TITLE);
            mScreenSubtitle = savedInstanceState.getCharSequence(KEY_SCREEN_SUBTITLE);
            mScreenIcon = savedInstanceState.getParcelable(KEY_SCREEN_ICON);
            mLastFocusedPreferenceKey = savedInstanceState.getString(KEY_LAST_PREFERENCE);
            mUriString = savedInstanceState.getString(KEY_URI_STRING);
        }
    }

    @Override
    public void onChanged(@NonNull Slice slice) {
        if (!getSliceLiveData().mUpdatePending.compareAndSet(true, false)) {
            return;
        }

        mSlice = slice;
        // Make TvSettings guard against the case that slice provider is not set up correctly
        if (slice == null || slice.getHints() == null) {
            return;
        }

        if (slice.getHints().contains(HINT_PARTIAL)) {
            showProgressBar();
        } else {
            hideProgressBar();
        }
        update();
    }

    private void showProgressBar() {
        View progressBar = getView().findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        View progressBar = getView().findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setSubtitle(CharSequence subtitle) {
        View view = this.getView();
        TextView decorSubtitle = view == null
                ? null
                : (TextView) view.findViewById(R.id.decor_subtitle);
        if (decorSubtitle != null) {
            if (TextUtils.isEmpty(subtitle)) {
                decorSubtitle.setVisibility(View.INVISIBLE);
            } else {
                decorSubtitle.setVisibility(View.VISIBLE);
                decorSubtitle.setText(subtitle);
            }
        }
        mScreenSubtitle = subtitle;
    }

    private void setIcon(Icon icon) {
        View view = this.getView();
        ImageView  decorIcon = view == null ? null : (ImageView) view.findViewById(R.id.decor_icon);
        if (decorIcon != null && icon != null) {
            decorIcon.setImageDrawable(icon.loadDrawable(mContextThemeWrapper));
        }
        mScreenIcon = icon;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view =
                (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        LayoutInflater themedInflater = LayoutInflater.from(view.getContext());
        final View newTitleContainer = themedInflater.inflate(
                R.layout.slice_title_container, container, false);
        view.removeView(view.findViewById(R.id.decor_title_container));
        view.addView(newTitleContainer, 0);

        final View newContainer =
                themedInflater.inflate(R.layout.slice_progress_bar, container, false);
        ((ViewGroup) newContainer).addView(view);
        return newContainer;
    }

    public void setLastFocused(Preference preference) {
        mLastFocusedPreferenceKey = preference.getKey();
    }

    private void handleUri(Uri uri) {
        String uriString = uri.getQueryParameter(SlicesConstants.PARAMETER_URI);
        // Provider should provide the correct slice uri in the parameter if it wants to do certain
        // action(includes go back, forward, error message), otherwise TvSettings would ignore it.
        if (uriString == null || !uriString.equals(mUriString)) {
            return;
        }
        String direction = uri.getQueryParameter(SlicesConstants.PARAMETER_DIRECTION);
        if (direction != null) {
            if (direction.equals(SlicesConstants.FORWARD)) {
                forward();
            } else if (direction.equals(SlicesConstants.BACKWARD)) {
                back();
            }
        }

        String errorMessage = uri.getQueryParameter(SlicesConstants.PARAMETER_ERROR);
        if (errorMessage != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
