NAME="LOG_PUBLISHER"
LOG=$NAME-logs-`date +%Y%m%d%H%M%S`
java -Xms256m -Xmx4g -cp ../libs/*: pt/ul/fc/di/navigators/trone/apps/CmdPublisherClientMeter $1 $2 $3 $4 cpu storage apache iaas&> $LOG
