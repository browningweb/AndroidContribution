package com.example.libavndkdemo;

public final class LibavTest {

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


  public native void run(String[] args);
  public native void exitProgram();

}
