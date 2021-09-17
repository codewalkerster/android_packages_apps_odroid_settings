package hardkernel.odroid.settings;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import android.view.View;
import android.widget.TextView;

import static java.security.AccessController.getContext;

public abstract class LeanbackAddBackPreferenceFragment extends LeanbackPreferenceFragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView decorTitle = (TextView) view.findViewById(R.id.decor_title);

        decorTitle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    ((Activity)getContext()).onBackPressed();
                }
            }
        });
    }
}
