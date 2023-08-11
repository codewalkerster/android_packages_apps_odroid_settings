/* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.display.dolbyvision;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.util.Log;
import android.text.TextUtils;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.ProgressingDialogUtil;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;

import java.util.List;
import java.util.ArrayList;

public class HdrPolicyFragment extends SettingsPreferenceFragment {
    private static final String LOG_TAG = "HdrPolicyFragment";

    private static final String HDR_POLICY_SINK = "hdr_policy_sink";
    private static final String HDR_POLICY_SOURCE = "hdr_policy_source";

    private static final String DV_HDR_SOURCE = "1";
    private static final String DV_HDR_SINK = "0";
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private String mNewHdrPolicy;
    private String mOldHdrPolicy;
    private ProgressingDialogUtil mProgressingDialogUtil;
    private Context themedContext;
    private Bundle mSavedInstanceState;

    public static HdrPolicyFragment newInstance() {
        return new HdrPolicyFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mDisplayCapabilityManager = DisplayCapabilityManager.getDisplayCapabilityManager(getActivity());
        themedContext = getPreferenceManager().getContext();
        mProgressingDialogUtil = new ProgressingDialogUtil();
        mSavedInstanceState = savedInstanceState;
        updatePreferenceFragment(mSavedInstanceState);
    }

    private ArrayList<Action> getActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder()
                .key(HDR_POLICY_SINK)
                .title(getString(R.string.hdr_policy_sink))
                .checked(DV_HDR_SINK.equals(mDisplayCapabilityManager.getHdrStrategy()))
                .build());

        actions.add(new Action.Builder()
                .key(HDR_POLICY_SOURCE)
                .title(getString(R.string.hdr_policy_source))
                .checked(DV_HDR_SOURCE.equals(mDisplayCapabilityManager.getHdrStrategy()))
                .build());

        return actions;
    }

    public void onClickHandle(String key) {
        String hdrPolicyType = HDR_POLICY_SINK;
        if (key.equals(HDR_POLICY_SOURCE)) {
            hdrPolicyType = HDR_POLICY_SOURCE;
        }
        mDisplayCapabilityManager.setHdrStrategyInternal(hdrPolicyType);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        mOldHdrPolicy = mNewHdrPolicy;
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                mNewHdrPolicy = radioPreference.getKey();
                Log.d(LOG_TAG, "mOldHdrPolicy = " + mOldHdrPolicy + ", mNewHdrPolicy = " + mNewHdrPolicy);
                radioPreference.setChecked(true);
                String newHdrPolicyTitle = radioPreference.getTitle().toString();
                mProgressingDialogUtil.showWarningDialogOnResolutionChange(themedContext, newHdrPolicyTitle,
                        new ProgressingDialogUtil.DialogCallBackInterface() {
                            @Override
                            public void positiveCallBack() {}

                            @Override
                            public void negativeCallBack() {
                                onClickHandle(mOldHdrPolicy);
                                mUIHandler.sendEmptyMessage(MSG_PLUG_FRESH_UI);
                            }
                        });

            } else {
                radioPreference.setChecked(true);
                Log.i(LOG_TAG, "not checked");
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void updatePreferenceFragment(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "updatePreferenceFragment: updateUI!!");
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.device_hdr_policy);
        Preference activePref = null;

        final List<Action> dvInfoList = getActions();
        for (final Action dvInfo : dvInfoList) {
            final String dvTag = dvInfo.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(dvTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(dvInfo.getTitle());
            //radioPreference.setRadioGroup(DV_RADIO_GROUP);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

            if (dvInfo.isChecked()) {
                mNewHdrPolicy = dvTag;
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
        if (activePref != null && savedInstanceState == null) {
            scrollToPreference(activePref);
        }
        setPreferenceScreen(screen);
    }

    private static final int MSG_PLUG_FRESH_UI = 0;
    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLUG_FRESH_UI:
                    updatePreferenceFragment(mSavedInstanceState);
                    break;
                default:
                    break;
            }
        }
    };

}
