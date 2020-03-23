package com.hardkernel.odroid.settings.update;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import com.hardkernel.odroid.settings.util.DroidUtils;

import java.io.File;

import static android.app.Activity.RESULT_OK;


public class UpdateFragment extends LeanbackAddBackPreferenceFragment {

    private static final String TAG = "UpdateFragment";
    public static final int FILE_SELECT_CODE = 101;

    private static final String KEY_UPDATE = "update_title";
    private static final String KEY_FROM_ONLINE = "update_from_online";
    private static final String KEY_SELECT_SERVER = "selected_server";
    private static final String KEY_FROM_STORAGE = "update_from_storage";

    private static PreferenceScreen update;
    private static Preference update_server;
    private static SwitchPreference checkAtUpdate;

    public static UpdateFragment newInstance() { return new UpdateFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        refreshStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        setPreferencesFromResource(R.xml.update, null);
        update_server = findPreference(KEY_SELECT_SERVER);
        update = (PreferenceScreen)findPreference(KEY_UPDATE);

        String server = null;
        switch (updateManager.getServer()) {
            case updateManager.KEY_OFFICIAL:
                server = getString(R.string.update_official_server);
                break;
            case updateManager.KEY_MIRROR:
                server = getString(R.string.update_mirror_server);
                break;
            case updateManager.KEY_CUSTOM:
                server = getString(R.string.update_custom_server);
                break;
        }
        update_server.setSummary(server);

        checkAtUpdate = (SwitchPreference) findPreference(updateManager.KEY_CHECK_UPDATE);
        checkAtUpdate.setChecked(updateManager.isCheckAtBoot());

        Preference version = new Preference(getContext());
        version.setTitle(DroidUtils.is64Bit()? "This is 64bit Android"
                : "This is 32bit Android");
        version.setEnabled(false);
        update.addPreference(version);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        Context context = getContext();

        switch (key) {
            case KEY_FROM_ONLINE:
                UpdatePackage.checkLatestVersion(context);
                break;
            case KEY_FROM_STORAGE:
                updatePackageFromStorage();
                break;
            case updateManager.KEY_CHECK_UPDATE:
                updateManager.setCheckUpdate(checkAtUpdate.isChecked());
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void updatePackageFromStorage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);

        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Update"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(),
                    "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UpdateFragment.FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Context context = getContext();
                    Uri uri = data.getData();
                    String path = getPath(context, uri);
                    if (path == null)
                        return;
                    UpdatePackage.installPackage(context, new File(path));
                }
                break;
        }
    }

    private static String getPath(Context context, Uri uri) {
        final boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isDownloadDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);

                String[] contentUriPrefixesToTry = new String[] {
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads",
                };
                try {
                    for (String contentUriPrefix : contentUriPrefixesToTry ) {
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix),
                                Long.valueOf(id));
                        try {
                            String path = getDataColumn(context, contentUri, null, null);
                            if (path != null) {
                                return path;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NumberFormatException e) {
                    return id.replaceFirst("raw:", "");
                }
            }
        } else if ( "file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return  null;
    }

    private static String getDataColumn (Context context, Uri uri,
                                         String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = { MediaStore.Images.Media.DATA };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadProvider.
     */
    private static boolean isDownloadDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}
