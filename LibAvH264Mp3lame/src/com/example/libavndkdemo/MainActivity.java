package com.example.libavndkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity{
	
	private Button btnRun;
	
	
	private String inFileName =  "/mnt/sdcard/input.mp4";//Environment.getExternalStorageDirectory().getPath() + "/Music/JniTest/oldpani.mp4";
	private String outFileName = "/mnt/sdcard/output.flv";

	private LibavTest vk = new LibavTest();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnRun	=	(Button) findViewById(R.id.cropButon);
		btnRun.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						long oTime = System.currentTimeMillis();
						vk.run(new String[]
								 {
								 "avconv", "-i", inFileName, "-y", outFileName
						        });
//						  vk.encodeTest();

						long nTime = System.currentTimeMillis();
						String time  = (nTime-oTime)/1000+" secs";
						System.out.println("Done time taken "+time);
						vk.exitProgram();
						
					}
				}).start();
			}
		});
	}
	
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



}
