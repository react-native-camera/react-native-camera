#!/bin/bash
mkdir scripts/dirtemp
curl -o scripts/dirtemp/temp.tar.gz 'https://dl.google.com/dl/cpdc/df83c97cbca53eaf/GoogleMobileVision-1.1.0.tar.gz'
tar -xvzf scripts/dirtemp/temp.tar.gz -C scripts/dirtemp
cp -r scripts/dirtemp/FaceDetector/Frameworks/frameworks/FaceDetector.framework ios/Frameworks/FaceDetector/Frameworks/frameworks
rm -rf scripts/dirtemp