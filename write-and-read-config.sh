TS=`date +%Y%m%d%H%M%S`
LOG=logs/write-and-read-configs-log-$TS

touch $LOG

echo -n "Writing config files netConfig.props and serverConfig.props ... " 
java pt/ul/fc/di/navigators/trone/apps/ConfigsWriter &> $LOG
echo "done."

echo -n "Rading config files netConfig.props and serverConfig.props ... " 
java pt/ul/fc/di/navigators/trone/apps/ConfigsReader &> $LOG
echo "done."

echo "LOGs are available at $LOG file."

