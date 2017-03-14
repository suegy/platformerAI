#!/bin/sh

I=1
LIM=2
PORT=4242
while [ $I -le $LIM ]
do
    echo $I $PORT
    python PlatformerAI.py --agent ForwardAgent --port ${PORT} &
    PORT=$(( $PORT + 1 ))
    I=$(( $I + 1 ))
done
python PlatformerAI.py --agent ForwardRandomAgent --port ${PORT} &
