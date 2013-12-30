#!/bin/bash

function current_dir {
  echo "$(cd "$(dirname $0)"; pwd)"
}
export PATH=$PATH:$ANDROID_NDK:$(current_dir)/toolchain/bin
./configure --cross-prefix=arm-linux-androideabi- \
--enable-pic \
--disable-asm \
--host=arm-linux 

make STRIP=

