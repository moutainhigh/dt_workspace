/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.minnw.beacon;

import java.util.ArrayList;
import java.util.List;


import com.minnw.beacon.R;
import com.minnw.beacon.adapter.UUIDAdapter;
import com.minnw.tools.Base64Tool;
import com.minnw.tools.Tools;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ChangPassWordActivity extends FinishActivity {
	EditText curr_edittext;
	EditText chang_textview;
	String serviceIndex;
	String position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changpassword);
		position = getIntent().getStringExtra("position");
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		init();

	}

	private void init() {
		curr_edittext = (EditText) findViewById(R.id.curr_textview);
		chang_textview = (EditText) findViewById(R.id.chang_textview);
		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chang_textview.getText().toString()
						.equals(curr_edittext.getText().toString().trim())) {
					if (chang_textview.getText().toString().trim().length() == 8) {
						String s = curr_edittext.getText().toString().trim();
						String vlaues = Base64Tool.ASCIIToBase64(s);
						DeviceListActivity.mService.write_uuid(vlaues,
								Integer.parseInt(serviceIndex.split(";")[0]),
								Integer.parseInt(serviceIndex.split(";")[1]));
						finish();
					} else {
						Toast.makeText(getApplicationContext(),
								getString(R.string.changpasswordtip2), 1)
								.show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.changpasswordtip1), 1).show();
				}
			}
		});
		ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
