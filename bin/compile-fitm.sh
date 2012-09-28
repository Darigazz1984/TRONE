TS=`date +%Y%m%d%H%M%S`
LOG=logs/javac-log-$TS

touch $LOG

for i in ../src/pt/ul/fc/di/navigators/trone/apps/{Cmd,Conf}*
do
  FN=`echo $i | sed 's/^.*\///'`
  echo -n "Generating byte code for $FN ... "
  javac -d . -s . -classpath ../libs/*:../src/ $i -Xlint:unchecked &> .tmp-$TS
  echo "done."
  cat .tmp-$TS >> $LOG
done
rm -f .tmp-$TS

echo "Compiling LOGs are available at $LOG file."

#./write-and-read-config.sh
