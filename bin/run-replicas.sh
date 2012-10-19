[ $1 ] && [ $2 ] && [ $3 ] || { echo "Usage: $0 numberOfReplicas StartingId mode(xterm|background)"; exit; }

NREPLICAS=$1
NREPLICAS=$((NREPLICAS-1))
STARTINGID=$2
JAVACMD="java -Xms256m -Xmx4g -cp ../libs/*:"

for i in `seq 0 $NREPLICAS`
do
  if [ "$3" = "xterm" ]
  then
    xterm -T "RUNNING REPLICA $i" -e "$JAVACMD pt/ul/fc/di/navigators/trone/apps/CmdServerWithPoolOfThreads $i; sleep 20" &
  else
    LOG=CmdServerWithPoolOfThreads-$i-logs-`date +%Y%m%d%H%M%S`
    $JAVACMD pt/ul/fc/di/navigators/trone/apps/CmdServerWithPoolOfThreads $((STARTINGID+i)) &> $LOG &
    echo "INFO: running CmdServerWithPoolOfThreads with logs on $LOG"
  fi
  sleep 1
done
