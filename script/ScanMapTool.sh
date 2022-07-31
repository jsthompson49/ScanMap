#!/usr/bin/bash

echo "Starting Scan Map Tool ..."

java -cp "/home/pi/Projects/ScanMap/bin/ScanMap.jar:/home/pi/build/bin/opencv-460.jar:/home/pi/Projects/ScanMap/lib/*" -Djava.library.path="/home/pi/build/lib" edu.pi.scanmap.ScanMapTool