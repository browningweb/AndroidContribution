

package com.example.libavndkdemo;

import java.nio.ShortBuffer;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AudioRecordActivity extends Activity implements OnClickListener {

    private static final String LOG_TAG = "AudioRecordActivity";
	long startTime = 0;
    boolean recording = false;

    boolean isPlaying = false;

    
    private volatile Mp3Encoder recorder;

    // frame size depends on codec setting in native code this can varies with setting 
    // i will handle it in native later
//    private static final int FRAME_SIZE = 1152;
    private int sampleAudioRateInHz = 44100;

    /* audio data getting thread */
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    volatile boolean runAudioThread = true;

    private MediaPlayer mediaPlayer;
    
    int frameSize;
    
    private static final String audioPath = "/mnt/sdcard/encodeMp3.mp3";
  
    private Button btnRecorderControl, btnPlay;
    
    
    int inBufferSize;
    int outBufferSize;
    short[] pending = new short[0];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        btnRecorderControl = (Button) findViewById(R.id.button1);
        btnRecorderControl.setOnClickListener(this);
        
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        btnPlay.setEnabled(false);
        initRecorder();
    }


    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recording = false;
    }




    //---------------------------------------
    // initialize ffmpeg_recorder
    //---------------------------------------
	private void initRecorder() {
		Log.w(LOG_TAG, "init recorder");
		recorder = new Mp3Encoder();
		int success = recorder.initAudio(audioPath);
		if (success == 0) {
			frameSize = recorder.getFrameSize();
			if (frameSize == 0) {
				Toast.makeText(getApplicationContext(), "Native Error", Toast.LENGTH_SHORT).show();
				finish();
			}
		}else{
			Toast.makeText(getApplicationContext(), "Native initialization Error", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		Log.i(LOG_TAG, "recorder initialize success");

	}

    public void startRecording() {

        try {
            startTime = System.currentTimeMillis();
            recording = true;
            audioRecordRunnable = new AudioRecordRunnable();
    		audioThread = new Thread(audioRecordRunnable);
            audioThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {

        runAudioThread = false;

        if (recorder != null && recording) {
            recording = false;
            Log.v(LOG_TAG,"Finishing recording, calling stop and release on recorder");
            try {
            	recorder.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder = null;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recording) {
                stopRecording();
            }

            finish();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

	private void writeAudioSamples(short[] buffer, int bufferReadResult) {

		int pendingArrLength = pending.length;
		short[] newArray = new short[bufferReadResult + pendingArrLength];

		System.arraycopy(pending, 0, newArray, 0, pendingArrLength);
		System.arraycopy(buffer, 0, newArray, pendingArrLength,bufferReadResult);

		int len = newArray.length;
		int q = Math.abs(len / frameSize);
		int r = len % frameSize;

		ShortBuffer shortBuffer = ShortBuffer.wrap(newArray);
		for (int i = 0; i < q && recording; i++) {
			short dst[] = new short[frameSize];
			shortBuffer.get(dst);
			recorder.writeAudioFrame(dst, dst.length);
		}
		pending = new short[r];
		shortBuffer.get(pending);
	}

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = new short[bufferSize];

            Log.d(LOG_TAG, "audioRecord.startRecording()");
            audioRecord.startRecording();

            /* ffmpeg_audio encoding loop */
            while (runAudioThread) {
                //Log.v(LOG_TAG,"recording? " + recording);
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                if (bufferReadResult > 0) {
                    // If "recording" isn't true when start this thread, it never get's set according to this if statement...!!!
                    // Why?  Good question...
                    if (recording) {
                        try {
                        	writeAudioSamples(audioData, bufferReadResult);
                        } catch (Exception e) {
                            Log.v(LOG_TAG,e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.v(LOG_TAG,"AudioThread Finished, release audioRecord");

            /* encoding finish, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v(LOG_TAG,"audioRecord released");
            }
        }
    }

    private void playAudio(){
    	btnPlay.setText("Pause");
    	mediaPlayer = new MediaPlayer();
    	try {
    		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer arg0) {
					btnPlay.setText("Play");
					isPlaying = false;
				}
			});
			mediaPlayer.setDataSource(audioPath);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    private void pauseAudio(){
    	mediaPlayer.stop();
    	btnPlay.setText("Play");
    	isPlaying = false;
    }

    @Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnPlay) {
			if (isPlaying) {
				pauseAudio();
			}else{
				playAudio();
			}
			
		} else {
			if (!recording) {
				startRecording();
				Log.w(LOG_TAG, "Start Button Pushed");
				btnRecorderControl.setText("Stop");
				btnPlay.setEnabled(false);
			} else {
				// This will trigger the audio recording loop to stop and then
				// set isRecorderStart = false;
				stopRecording();
				Log.w(LOG_TAG, "Stop Button Pushed");
				btnRecorderControl.setText("Start");
				btnPlay.setEnabled(true);
			}
		}

	}
}
