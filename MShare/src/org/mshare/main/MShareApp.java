/*
Copyright 2011-2013 Pieter Pareit

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mshare.main;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * 和Activity中获得的Context并不相同，这和SharedPreferences有关系吗？
 * @author HM
 *
 */
public class MShareApp extends Application {

    private static final String TAG = MShareApp.class.getSimpleName();

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    /**
     * @return the Context of this application
     */
    public static Context getAppContext() {
        if (sContext == null) {
            Log.e(TAG, "Global context not set");
        }
        return sContext;
    }
    private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
    }
}
