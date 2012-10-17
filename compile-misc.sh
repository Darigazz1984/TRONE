TS=`date +%Y%m%d%H%M%S`
LOG=logs/javac-log-$TS

touch $LOG

DIR=pt/ul/fc/di/navigators/trone/xtests

for i in ../src/$DIR/*
do
  FN=`echo $i | sed 's/^.*\///'`
  echo -n "Generating byte code for $FN ... "
  javac -d . -s . -classpath ../libs/*:../src/ $i -Xlint:unchecked &> .tmp-$TS
  cat .tmp-$TS >> $LOG
  echo "done."
done

echo "Compiling LOGs are available at $LOG file."

