[ $1 ] && [ $2 ] && [ $3 ] && [ $4 ] || { echo "Usage: $0 PubOrSub(1 = Pub; 2 = Sub; 3 = Both) clientsPerChannel mode(xterm|background) channelTag1 channelTag2 channelTag3 ... "; exit; }

PUBLISHER=pt/ul/fc/di/navigators/trone/apps/CmdPublisherClient
SUBSCRIBER=pt/ul/fc/di/navigators/trone/apps/CmdSubscriberClient

PUBSUB=$1
shift 

CLIENTSPERCHANNEL=$1
shift

MODETORUN=$1
shift

NROUNDS=1000
NEVENTS=100
NMILLITOSLEEP=15000

case $PUBSUB in
1) 
  LIST=$PUBLISHER
  ;;
2)
  LIST=$SUBSCRIBER
  ;;
3)
  LIST="$SUBSCRIBER $PUBLISHER"
esac

JAVACMD="java -Xms256m -Xmx4g"

for TRONECLIENT in $LIST
do
    for CHANNELTAG in $*
    do
        NAME=`echo $TRONECLIENT | sed 's/^.*\///'`
        for i in `seq 1 $CLIENTSPERCHANNEL`
        do
            if [ "$MODETORUN" = "xterm" ]
            then
              xterm -T "RUNNING $NAME $i for channel $CHANNELTAG" -e "$JAVACMD $TRONECLIENT $CHANNELTAG $NROUNDS $NEVENTS $NMILLITOSLEEP; sleep 60" &
            else
              LOG=$NAME-$CHANNELTAG-$i-logs-`date +%Y%m%d%H%M%S`
              $JAVACMD $TRONECLIENT $CHANNELTAG $NROUNDS $NEVENTS $NMILLITOSLEEP &> $LOG &
              echo "INFO: running $NAME with logs on $LOG"
            fi
        done
    done
done

