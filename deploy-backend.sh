#!/bin/bash
IP="89.117.72.112"
JAR_NAME="gessainvoice-0.0.1-SNAPSHOT.jar"

echo "Building..."
./gradlew clean bootJar -x test

echo "Uploading jar..."
scp build/libs/$JAR_NAME root@$IP:/opt/gessainvoice/releases/

echo "Restarting service..."
ssh root@$IP "systemctl restart gessainvoice"

echo "Checking status..."
ssh root@$IP "systemctl status gessainvoice --no-pager"

echo "Done!"
