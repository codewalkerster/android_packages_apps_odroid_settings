package hardkernel.odroid.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;

public abstract class LeanbackAddBackPreferenceFragment extends LeanbackPreferenceFragmentCompat {
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
                    unwrap(v.getContext()).onBackPressed();
                }
            }
        });
    }
    private static Activity unwrap(Context context) {
        while (!(context instanceof Activity) &&
                (context instanceof ContextWrapper)) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        return (Activity) context;
    }
}
