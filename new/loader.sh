#!/bin/bash

cd /home/otranto/projects/PlatformerAI/downloads
#python loader2.py /home/otranto/projects/PlatformerAI/downloads/archives/ /home/otranto/projects/PlatformerAI/out/production/PlatformerAI /home/otranto/projects/PlatformerAI/log.txt /home/otranto/projects/PlatformerAI/out/production/PlatformerAI/ yes

python ./load.py -ufrom /home/otranto/projects/PlatformerAI/downloads/archives/ -uto /home/otranto/projects/PlatformerAI/out/production/PlatformerAI -log /home/otranto/projects/PlatformerAI/log.txt -benchmark /home/otranto/projects/PlatformerAI/out/production/PlatformerAI/ -delete no
