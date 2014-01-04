package com.example.libavndkdemo;

public final class Mp3Encoder {

  static {
//    System.loadLibrary("final_file");
	  System.loadLibrary("avutil");
      System.loadLibrary("avcodec");
      System.loadLibrary("avformat");
      System.loadLibrary("avdevice");
      System.loadLibrary("avresample");
      System.loadLibrary("swscale");
      System.loadLibrary("avfilter");
      System.loadLibrary("androidlibav");

}


  public native void initAudio(String filePath);
  public native void writeAudioFrame(short[] samples, int length);
  public native void close();

}
