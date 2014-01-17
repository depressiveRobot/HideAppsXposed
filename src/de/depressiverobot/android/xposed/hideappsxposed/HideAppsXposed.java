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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HideAppsXposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	// tag used for logging
	public static final String TAG = "HIDE_APPS_XPOSED: ";
	
	// GEL package and class names
	public static final String APPS_CUSTOMIZE_PAGED_VIEW_CLASS = "com.android.launcher3.AppsCustomizePagedView";
	public static final String ITEM_INFO_CLASS = "com.android.launcher3.ItemInfo";
	public static final String APP_INFO_CLASS = "com.android.launcher3.AppInfo";
	public static final List<String> GEL_PACKAGE_NAMES = new ArrayList<String>(Arrays.asList("com.android.launcher3", "com.google.android.googlequicksearchbox"));
	
	// used to store the preferences of the Hide Apps Xposed app
	private static XSharedPreferences prefs;
	
	private static Field itemInfoTitleField;
	private static Field appInfoComponentNameField;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		
		prefs = new XSharedPreferences(HideAppsXposedSettings.PACKAGE_NAME, HideAppsXposedSettings.PREFERENCES_NAME);
	}
	
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

		if (GEL_PACKAGE_NAMES.contains(lpparam.packageName)) {
			
			final Class<?> appsCustomizePagedViewClass =  XposedHelpers.findClass(APPS_CUSTOMIZE_PAGED_VIEW_CLASS, lpparam.classLoader);
			
    		// as we do not have the GEL classes in the classpath we need to get
    		// the information using Java reflection
			final Class<?> itemInfoClass = XposedHelpers.findClass(ITEM_INFO_CLASS, lpparam.classLoader);
    		itemInfoTitleField = itemInfoClass.getDeclaredField("title");
    		itemInfoTitleField.setAccessible(true);
    		
    		final Class<?> appInfoClass = XposedHelpers.findClass(APP_INFO_CLASS, lpparam.classLoader);
    		appInfoComponentNameField = appInfoClass.getDeclaredField("componentName");
    		appInfoComponentNameField.setAccessible(true);
			
    		XC_MethodHook hideAppsMethodHook = new HideAppsMethodHook();
    		
			// called when launcher is started
			XposedBridge.hookAllMethods(appsCustomizePagedViewClass, "setApps", hideAppsMethodHook);
			
			// called when apps get added while launcher is running
			XposedBridge.hookAllMethods(appsCustomizePagedViewClass, "addApps", hideAppsMethodHook);
			
			// called when apps get updated while launcher is running
			XposedBridge.hookAllMethods(appsCustomizePagedViewClass, "updateApps", hideAppsMethodHook);
		}
	}

	private class HideAppsMethodHook extends XC_MethodHook {
		
		@SuppressWarnings("rawtypes")
		@Override
        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        	
        	prefs.reload();
        	Set<String> appsToHide = prefs.getStringSet(HideAppsXposedSettings.APPS_TO_HIDE_KEY, new HashSet<String>());
        	
        	// this will be called before the apps will be added
        	// to the app drawer
        	ArrayList apps = (ArrayList) param.args[0];
        	Iterator appIter = apps.iterator();
        	while (appIter.hasNext()) {
        		Object app = appIter.next();
        		String label = (String) itemInfoTitleField.get(app);
        		String packageName = ((ComponentName) appInfoComponentNameField.get(app)).getPackageName();
        		for (String appToHide : appsToHide) {
        			if (appToHide.equals(label + HideAppsXposedSettings.LABEL_PACKAGENAME_SEPERATOR + packageName)) {
        				appIter.remove();
        				XposedBridge.log(TAG + "Hiding app: " + label + " (" + packageName + ")");
        				break;
        			}
        		}
        	}
        }
	}
	
}