package com.example.libavndkdemo;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;

public class CropService extends IntentService {


	public static final String EXTRA_STATUS_RECEIVER = "receiver";
	public static String RESULT = "result";
	public static String TIME_TAKEN = "time";

	private Videokit vk = new Videokit();


	
	public CropService(String name) {
		super("CropService");
	}
	
	public CropService() {
	    super("CropService");
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver 		=	intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		
		String inFileName = intent.getStringExtra("input");
		String outFileName = intent.getStringExtra("output");
		
		
		long oTime = System.currentTimeMillis();
/*		String text	=	vk.crop(new String[]
				 {
//				"avcon", "-i", inFileName, "-vf", "\"crop=1080:1080:0:0\"", outFileName
//				"avconv", "-i", inFileName, "-vf", "\"crop=1080:1080:0:0\"", outFileName
				// "-vf","\"crop=400:400:0:0\""
//				"avconv", "-i", inFileName, "-y", "-c:v" ,"libx264", "-preset" ,"ultrafast", "-acodec", "aac" ,"-strict", "experimental", "-ac", "2", "-ar", "44100", "-ab", "192k", "-qscale", "0", "-q:a", "0" ,"-q:v" ,"0", "-vf","crop=400:400:0:0", outFileName
				 "avconv", "-i", inFileName,"-i","/mnt/sdcard/PrisonBreak.mp3", "-y", outFileName
		        });*/
		  vk.encodeTest();

		long nTime = System.currentTimeMillis();
		System.out.println("cropping done");
		String time  = (nTime-oTime)/1000+" secs";
		Bundle bundle = new Bundle();
		bundle.putString(TIME_TAKEN, time);
		bundle.putInt(RESULT, Activity.RESULT_OK);
		receiver.send(Activity.RESULT_OK, bundle);
		vk.exitProgram();


	}

}
