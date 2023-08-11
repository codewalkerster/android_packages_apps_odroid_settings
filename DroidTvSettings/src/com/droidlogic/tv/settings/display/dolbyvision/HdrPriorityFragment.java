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
import androidx.preference.SwitchPreference;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.util.Log;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.ProgressingDialogUtil;
import com.droidlogic.tv.settings.dialog.old.Action;

import java.util.List;
import java.util.ArrayList;

public class HdrPriorityFragment extends SettingsPreferenceFragment {
    private static final String TAG = "HdrPriorityFragment";
    private static HdrPriorityFragment uniqueInstance;
    RadioPreference prePreference;
    RadioPreference curPreference;

    private static final int DOLBY_VISION           = 0;
    private static final int HDR10                  = 1;
    private static final int SDR                    = 2;
    public int preType                              = 0;
    public int curType                              = 0;
    private OutputModeManager mOutputModeManager;
    private ProgressingDialogUtil mProgressingDialogUtil;
    private Context themedContext;

    public static HdrPriorityFragment newInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new HdrPriorityFragment();
        }
        return uniqueInstance;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mOutputModeManager = OutputModeManager.getInstance(getActivity());
        themedContext = getPreferenceManager().getContext();
        mProgressingDialogUtil = new ProgressingDialogUtil();
        updatePreferenceFragment(savedInstanceState);
    }

    private void updatePreferenceFragment(Bundle savedInstanceState) {
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.hdr_priority);
        Preference activePref = null;
        final List<Action> hdrInfoList = getActions();
        for (final Action hdrInfo : hdrInfoList) {
            final String hdrTag = hdrInfo.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(hdrTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(hdrInfo.getTitle());
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

            if (hdrInfo.isChecked()) {
                radioPreference.setChecked(true);
                prePreference = curPreference = radioPreference;
                activePref = radioPreference;
            }
            screen.addPreference(radioPreference);
        }
        if (activePref != null && savedInstanceState == null) {
            scrollToPreference(activePref);
        }
        setPreferenceScreen(screen);
    }

    private ArrayList<Action> getActions() {
        int mode = mOutputModeManager.getHdrPriority();
        boolean customConfig       = mOutputModeManager.isSupportNetflix();
        boolean displaydebugConfig = mOutputModeManager.isSupportDisplayDebug();
        Log.d(TAG,"Current Hdr Priority: " + mode);
        Log.d(TAG,"customConfig "+ customConfig);
        Log.d(TAG,"displaydebugConfig "+ displaydebugConfig);

        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder().key(Integer.toString(DOLBY_VISION)).title(getString(R.string.dolby_vision))
                .checked(mode == DOLBY_VISION).build());
        actions.add(new Action.Builder().key(Integer.toString(HDR10)).title(getString(R.string.hdr10))
                .checked(mode == HDR10).build());
        //netflix not display sdr
        if (displaydebugConfig) {
            actions.add(new Action.Builder().key(Integer.toString(SDR)).title(getString(R.string.sdr))
                .checked(mode == SDR).build());
        }
        return actions;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                preType = mOutputModeManager.getHdrPriority();
                curType = Integer.valueOf(radioPreference.getKey().toString());
                curPreference = radioPreference;

                mOutputModeManager.setHdrPriority(curType);
                String newHdrPriorityTitle = radioPreference.getTitle().toString();
                mProgressingDialogUtil.showWarningDialogOnResolutionChange(themedContext, newHdrPriorityTitle,
                        new ProgressingDialogUtil.DialogCallBackInterface() {
                            @Override
                            public void positiveCallBack() {

                            }
                            @Override
                            public void negativeCallBack() {
                                mOutputModeManager.setHdrPriority(preType);
                                updatePreferenceFragment(null);
                            }
                        });
                curPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
