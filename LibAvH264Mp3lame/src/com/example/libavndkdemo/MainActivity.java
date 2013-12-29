package com.example.libavndkdemo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity  implements DetachableResultReceiver.Receiver{
	
	private Button cropButton;
	
	
	private Videokit vk = new Videokit();
	private String inFileName =  "/mnt/sdcard/output.mp4";//Environment.getExternalStorageDirectory().getPath() + "/Music/JniTest/oldpani.mp4";
	private String outFileName = "/mnt/sdcard/outwith.mp4";

	private EditText editFileName = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editFileName = (EditText) findViewById(R.id.editText1);
		Button fileSelectButton	=	(Button) findViewById(R.id.selectButton);
		fileSelectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				String title = "Select Video";
				 photoPickerIntent.setType("video/*"); 
				 photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
			     startActivityForResult(Intent.createChooser(photoPickerIntent,
			                new String(title)), 10);
			}
		});
		
		cropButton	=	(Button) findViewById(R.id.cropButon);
		cropButton.setEnabled(false);
		cropButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CropService.class);
				DetachableResultReceiver receiver = new DetachableResultReceiver(new Handler());
				receiver.setReceiver((DetachableResultReceiver.Receiver) MainActivity.this);
				intent.putExtra(CropService.EXTRA_STATUS_RECEIVER, receiver);
				intent.putExtra("input", inFileName);//editFileName.getText().toString());
				intent.putExtra("output", outFileName);
				getApplicationContext().startService(intent);
			}
		});
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		 if(requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
		        Uri _uri = data.getData();

		        //User had pick an image.
		        Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Video.Media.DATA }, null, null, null);
		        cursor.moveToFirst();

		        //Link to the image
		        final String videoPath = cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DATA));
		        editFileName.setText(videoPath);
		        cursor.close();
		        cropButton.setEnabled(true);
		    }
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		if(resultCode == RESULT_OK) {
			String time = resultData.getString(CropService.TIME_TAKEN);
			Toast.makeText(getApplicationContext(), "Cropping done time taken "+time, Toast.LENGTH_LONG).show();
		}
	}

}
