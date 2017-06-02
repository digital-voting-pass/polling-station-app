#!/bin/sh
wget http://u7285p4947.web0097.zxcs.nl/gdrive-linux-x64
chmod +x gdrive-linux-x64


BRANCH=test
REPO_NAME=repo

FIXED_BRANCH=$(echo $BRANCH | sed 's/\//-/g')
ARCHIVE=$REPO_NAME-$FIXED_BRANCH-$(date +%Y-%m-%d_%H_%M_%S)-$COMMIT.tar.bz2
echo "Creating archive $ARCHIVE"
tar cfj $ARCHIVE /home/travis/build/digital-voting-pass/digital-voting-pass-app/app/build/reports/androidTests/connected/*
FILESIZE=$(stat -c%s "$ARCHIVE")
echo "Finished archive (size $FILESIZE), starting Google Drive upload"
./gdrive-linux-x64 upload --refresh-token $GDRIVE_REFRESH_TOKEN --parent $GDRIVE_DIR "$ARCHIVE"
echo "Finished Google Drive upload"
