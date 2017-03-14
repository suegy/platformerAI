#!/bin/sh

I=1
LIM=6
OUT=PlatformerAIResult.m
while [ $I -le $LIM ]
do
    echo processing iMario${I}.m ...
    cat PlatformerAI${I}.m >> ${OUT}
    I=$(( $I + 1 ))
done
echo "results saved to ${OUT}" 