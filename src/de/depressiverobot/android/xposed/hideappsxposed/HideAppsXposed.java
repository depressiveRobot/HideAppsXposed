/*
 * Copyright (C) 2014 Marvin Frommhold for Hide Apps Xposed project (depressiveRobot@xda)
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.depressiverobot.android.xposed.hideappsxposed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HideAppsXposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	public static final String TAG = "HIDE_APPS_XPOSED: ";
	 
	public static final List<String> PACKAGE_NAMES = new ArrayList<String>(
			Arrays.asList("com.android.launcher3", "com.google.android.googlequicksearchbox"));
	
	private static XSharedPreferences prefs;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		
		prefs = new XSharedPreferences(HideAppsXposedSettings.PACKAGE_NAME, HideAppsXposedSettings.PREFERENCES_NAME);
	}
	
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

		if (PACKAGE_NAMES.contains(lpparam.packageName)) {
			
			final Class<?> classAppsCustomizePagedView = XposedHelpers.findClass("com.android.launcher3.AppsCustomizePagedView", lpparam.classLoader);
			
			// called when launcher is started
			XposedBridge.hookAllMethods(classAppsCustomizePagedView, "setApps", new XC_MethodHook() {
				
                @SuppressWarnings("rawtypes")
				@Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                	
                	prefs.reload();
                	
                	// this will be called before the apps will be added
                	// to the app drawer
                	ArrayList apps = (ArrayList) param.args[0];
                	Iterator appIter = apps.iterator();
                	while (appIter.hasNext()) {
                		Object app = appIter.next();
                		Set<String> appsToHide = prefs.getStringSet(HideAppsXposedSettings.APPS_TO_HIDE_KEY, new HashSet<String>());
                		for (String appToHide : appsToHide) {
                			if (app.toString().contains(appToHide)) {
                				appIter.remove();
                				XposedBridge.log(TAG + "Hiding app: " + appToHide);
                				break;
                			}
                		}
                	}
                }
			});
		}
	}

}