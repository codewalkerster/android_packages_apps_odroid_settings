/*
 * Copyright (C) 2017 The Android Open Source Project
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

package hardkernel.odroid.settings.about;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import hardkernel.odroid.settings.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
public class LicenseActivityTest {
    private ActivityController<LicenseActivity> mActivityController;
    private LicenseActivity mActivity;
    private Application mApplication;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mApplication = RuntimeEnvironment.application;
        mActivityController = Robolectric.buildActivity(LicenseActivity.class);
        mActivity = spy(mActivityController.get());
    }

    void assertEqualIntents(Intent actual, Intent expected) {
        assertThat(actual.getAction()).isEqualTo(expected.getAction());
        assertThat(actual.getDataString()).isEqualTo(expected.getDataString());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getCategories()).isEqualTo(expected.getCategories());
        assertThat(actual.getPackage()).isEqualTo(expected.getPackage());
        assertThat(actual.getFlags()).isEqualTo(expected.getFlags());
    }

    @Test
    public void testOnCreateWithValidHtmlFile() {
        doReturn(true).when(mActivity).isFileValid(any());
        mActivity.onCreate(null);

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file:///system/etc/NOTICE.html.gz"), "text/html");
        intent.putExtra(Intent.EXTRA_TITLE, mActivity.getString(
                R.string.about_legal_license));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.android.htmlviewer");

        assertEqualIntents(shadowOf(mApplication).getNextStartedActivity(), intent);
    }
}
