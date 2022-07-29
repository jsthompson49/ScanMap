#!/bin/bash

echo "Starting Scan Map Tool ..."

java -version

java -cp "/home/pi/Projects/ScanMap/lib/ScanMap.jar:/home/pi/opencv-4.6.0/build/bin/opencv-460.jar" -Djava.library.path="/home/pi/opencv-4.6.0/build/lib" edu.pi.scanmap.ScanMapTool