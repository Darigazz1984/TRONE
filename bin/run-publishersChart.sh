NAME="CPU_STORAGE"
LOG=$NAME-logs-`date +%Y%m%d%H%M%S`
java -Xms256m -Xmx4g -cp ../libs/*: pt/ul/fc/di/navigators/trone/apps/CmdPublisherClientChart $1 $2 $3&> $LOG
