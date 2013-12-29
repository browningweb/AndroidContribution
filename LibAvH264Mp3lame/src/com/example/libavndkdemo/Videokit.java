package com.example.libavndkdemo;

public final class Videokit {

  static {
//    System.loadLibrary("final_file");
	  System.loadLibrary("avutil");
      System.loadLibrary("avcodec");
      System.loadLibrary("avformat");
      System.loadLibrary("avdevice");
      System.loadLibrary("avresample");
      System.loadLibrary("swscale");
      System.loadLibrary("avfilter");
      System.loadLibrary("cluff");

}

 /* public native String runOnMe(String[] args);
//  public native String stringFromJNI();
//  public native String runStringFromJNI();
  public native String  unimplementedStringFromJNI();*/
  
  public native String crop(String[] args);
  public native String encodeTest();
  public native String exitProgram();

}
