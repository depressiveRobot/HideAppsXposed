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

import java.util.List;

import de.depressiverobot.android.xposed.hideappsxposed.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
 
public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
	
    private List<ApplicationInfo> appsList;
    private List<Boolean> checkList;
    private Context context;
    private PackageManager packageManager;
	private boolean showToast = true;
    
    public ApplicationAdapter(Context context, int textViewResourceId,
            List<ApplicationInfo> appsList, List<Boolean> checkList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        this.checkList = checkList;
        this.packageManager = context.getPackageManager();
    }
 
    public List<Boolean> getCheckList() {
		return this.checkList;
	}

	@Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }
 
    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.snippet_list_row, null);
        }
 
        ApplicationInfo data = appsList.get(position);
        if (null != data) {
        	ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
        	iconview.setImageDrawable(data.loadIcon(packageManager));

        	TextView appName = (TextView) view.findViewById(R.id.app_name);
            appName.setText(data.loadLabel(packageManager));
            
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.app_check);
            checkBox.setTag(Integer.valueOf(position)); // set the tag so we can identify the correct row in the listener
            checkBox.setChecked(checkList.get(position)); // set the status as we stored it        
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                	boolean previousState = checkList.get((Integer)buttonView.getTag());
                    checkList.set((Integer)buttonView.getTag(), isChecked); // get the tag so we know the row and store the status
                    if (showToast && previousState != isChecked) {
                    	Toast.makeText(context, R.string.reboot_toast, Toast.LENGTH_LONG).show();
                    	showToast = false;
                    }
                }
           });
        }
        return view;
    }
};
