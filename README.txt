TRONE Project

This is a first version (0.1) of the Fault and Intrusion Tolerant (FIT) Event Broker.
It is a early version, still under development and lots of improvements.

======================================================================
BASIC INFO:
======================================================================

(a) It is a NetBeans project;

(b) you can use it directly through NetBeans, however, it is recommended to use Linux console;

(c) any comments, corrections, suggestions and improvements will be highly welcome;

======================================================================
STEPS TO USE AND TEST THE FIT BROKER:
======================================================================

(a) go to the bin/ directory;

(b) compile all required stuff: ./compile-all.sh

(c) write the config files in place: ./write-and-read-config.sh - CURRENTLY WORKING BUT NOT WRITING THE RIGHT CONFIGS
    for configuring channels just add/remove channels in the bin/channels folder, make shure you use NAMECHANNEL.props
    (required only on the first time or every time the configuration changes inside ConfigsWriter.java class)
 
(d) execute the replica: ./run-replicas.sh 4 xterm
    (you can use "xterm" windows of run in background - it is recommended to use xterm to see the execution logs)

(e) execute the ./run.sh it will start 2 publishers for channels mysql and apache and 2 subscribers, one for each channel. These channels are BFT
    (this command in particular will instantiate three channels and two clients per channel, being one publisher and one subscriber)
    (you can chose and run many different configurations)

======================================================================
SOME FAST NOTES:
======================================================================

(a) there are many configurations, either for replicas and clients;

(b) there are different logging levels;
    (you can chose the logging level based on your needs, going from zero logs to debug mode)
    (the logging levels are enabled or disabled inside Log.java)

(c) some configurations are still inside classes and should be moved out to configuration files in a near future;

(d) the communication part of the system still requires many performance tweaks/improvements;

(e) different communications protocols, such as TCP and UDP, should be allowed;
    (the user should be able to chose the best suited protocol)

(f) proactive/reactive recovery should be provided and evaluated;

(g) system replica management through Zookeeper may be a good first choice;
    (this will allow the dynamic replicas information management)

(h) the implemented algorithms still have to be described (in details) and further evaluated;

(i) some of the configuration properties could be dynamically and online managed;

