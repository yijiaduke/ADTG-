#!/bin/bash

# MUST RUN AS adtg user

LOG="./delivery.log"
ERRLOG="./delivery.err"

cd /srv/delivery
touch $LOG
touch $ERRLOG

echo "$(date +%F-%H:%M:%S:%N) -- Starting... Proccess ID: $$... Continue." >>$LOG
echo "$(date +%F-%H:%M:%S:%N) -- Starting... Proccess ID: $$... Continue." >>$ERRLOG

if [ -f ./proc.id ]; then
	# process id exists
	PID=$(cat ./proc.id 2>>$ERRLOG)  #get stored pid.
	echo "$(date +%F-%H:%M:%S:%N) -- Current PID is ${PID}" >>$LOG 2>>$ERRLOG
	
	ps -p ${PID} >>$ERRLOG 2>>$ERRLOG

	RES_CODE=$?

	echo "$(date +%F-%H:%M:%S:%N) -- PID RES_CODE = ${RES_CODE}" >>$LOG 2>>$ERRLOG

	if [ $RES_CODE -eq 0 ]; then
		#Process still running.
		echo "$(date +%F-%H:%M:%S:%N) -- Process Still Running... Abort." >>$LOG 2>>$ERRLOG
		exit 1
	else
		echo "$(date +%F-%H:%M:%S:%N) -- Process NOT RUNNING... Continue." >>$LOG 2>>$ERRLOG
	fi	
else
	echo "$(date +%F-%H:%M:%S:%N) -- No ./proc.id file... Continue." >>$LOG 2>>$ERRLOG
fi


#Store Process
echo $$ >./proc.id  2>>$ERRLOG

/opt/gradle/gradle-8.8/bin/gradle run >>$LOG 2>>$ERRLOG



echo "$(date +%F-%H:%M:%S:%N) -- Removing ./proc.id" &>>$LOG
rm -rf ./proc.id  >>$LOG 2>>$ERRLOG


echo "$(date +%F-%H:%M:%S:%N) -- Done... Proccess ID: $$... Terminated." >>$LOG
echo "$(date +%F-%H:%M:%S:%N) -- Done... Proccess ID: $$... Terminated." >>$ERRLOG
