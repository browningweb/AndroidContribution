package com.example.multiknobprogressbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.example.view.InfiniteKnobSeekBar;
import com.example.view.InfiniteKnobSeekBar.ThumbsProgressListener;

public class TestActivity extends Activity implements ThumbsProgressListener {

	private TextView mTxtProgress = null;
	private InfiniteKnobSeekBar mScaleBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		mTxtProgress = (TextView) findViewById(R.id.currentTxt);
		mScaleBar = (InfiniteKnobSeekBar) findViewById(R.id.infiniteKnobSeekBar1);
		mScaleBar.setThumbsCount(6);
		mScaleBar.setThumbsProgressListener(mThumbsProgressListener);
		mScaleBar.setMovingText(mTxtProgress);
		mScaleBar.setMovingText(mTxtProgress);
	}

	private ThumbsProgressListener mThumbsProgressListener = new ThumbsProgressListener() {

		@Override
		public boolean onThumbProgressChanged(int index, long progress) {
			int minutes = (int) ((progress / (1000 * 60)) % 60);
			int hours = (int) ((progress / (1000 * 60 * 60)) % 24);
			int exactMins = (int) (minutes / 15.0) * 15;
			// String minutesStr = String.format("%02d", minutes);
			String minutesStr = String.format("%02d", exactMins);

			if (progress > (86400000 - 60000)) {
				mTxtProgress.setText(Integer.toString(11) + ":"
						+ Integer.toString(59));
				return false;
			} else if (hours == 12) {
				String time = 12 + ":" + minutesStr;
				mTxtProgress.setText(time);
				return true;
			} else {
				String hoursStr = String.format("%02d", hours % 12);
				String time = hoursStr + ":" + minutesStr;
				mTxtProgress.setText(time);
				return true;
			}
		}

		@Override
		public void onProgressStart() {
			mTxtProgress.setVisibility(View.VISIBLE);
		}

		@Override
		public void onProgressStop() {
			mTxtProgress.setVisibility(View.INVISIBLE);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_test, menu);
		return true;
	}

	@Override
	public void onProgressStart() {
		mTxtProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onThumbProgressChanged(int index, long progress) {
		return false;
	}

	@Override
	public void onProgressStop() {
		mTxtProgress.setVisibility(View.VISIBLE);
	}

}
