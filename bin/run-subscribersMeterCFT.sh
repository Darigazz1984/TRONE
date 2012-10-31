NAME="LOG_SUBSCRIBER"
LOG=$NAME-logs-`date +%Y%m%d%H%M%S`
java -Xms256m -Xmx4g -cp ../libs/*: pt/ul/fc/di/navigators/trone/apps/CmdSubscriberClientMeter $1 $2 $3 $4 dns logs openstack smtp&> $LOG
