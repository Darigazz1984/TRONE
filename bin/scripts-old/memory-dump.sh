jmap -J-d64 -dump:format=b,file=<path to dump file> <jvm pid>

jhat <path to dump file>

jmap -dump:format=b,file=heapdump 6054

jhat -J-Xmx256m heapdump

