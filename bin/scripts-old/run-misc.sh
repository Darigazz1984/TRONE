[ $1 ] || { echo "Use: $0 useXterm(yes|no)"; exit; }

DIR=pt/ul/fc/di/navigators/trone/xtests

ls $DIR

echo -n "CHOSE ONE OF THE APP: "
read APP

if [ "$1" = "yes" ]
then
  xterm -T "RUNNING $APP" -e "java $DIR/$APP; sleep 1000" &
else
  java $DIR/$APP
fi
