DIR=pt/ul/fc/di/navigators/trone/xtests
APP=PingPongServer

#java -Xrunhprof:cpu=samples,file=log.txt,depth=3 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=. -Xms256m -Xmx2g $DIR/$APP
java -Xrunhprof:heap=sites,cpu=samples,depth=10,monitor=y,thread=y,doe=y,file=log.txt -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=. -Xms256m -Xmx2g $DIR/$APP
