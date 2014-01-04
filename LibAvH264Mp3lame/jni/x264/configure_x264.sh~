#!/bin/bash
CCPrefix=$1
function current_dir {
  echo "$(cd "$(dirname $0)"; pwd)"
}
SYSROOT=$ANDROID_NDK/platforms/android-14/arch-x86
export PATH=$PATH:$ANDROID_NDK:$(current_dir)/toolchain/bin
./configure --cross-prefix=$CCPrefix \
--enable-pic \
--disable-asm \
--host=arm-linux \


make STRIP=

