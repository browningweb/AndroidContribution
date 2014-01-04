package com.example.libavndkdemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class LibavExamples extends ListActivity implements OnItemClickListener{
	
	private String[] list = new String[]{"Command Line example", "Record Mp3 example"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setListAdapter(new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_1, list));
		 
		 getListView().setOnItemClickListener(this);
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		Intent intent = null;
		if (position == 0) {
			intent = new Intent(this, CommandLineActivity.class);
		}else if (position == 1) {
			intent = new Intent(this, AudioRecordActivity.class);
		}
		startActivity(intent);
	}
	
	
}
