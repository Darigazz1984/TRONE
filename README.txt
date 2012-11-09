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
THIS SET UP WILL RUN ONLY WITH QoP = BFT AND QoS = TOTALORDER 
(a) go to the bin/ directory;

(b) compile all required stuff: ./compile-fitm.sh

(c) write the config files in place:
	/***************CONFIGURE REPLICAS*****************/
	1. go to folder /config 
	2. open file hosts.config and write the ip address of the FIT Event replicas
	/***************CONFIGURE PUBLISHERS***************/
	3. go back the /bin folder
	4. open the pubclientConfig.props 
	5. Make sure that userSBFT is at value 1
	6. Make sure that useOrdered is at value 1
	/***************CONFIGURE SUBSCRIBERS***************/
	7. open the subclientConfig.props
	8. Make sure that userSBFT is at value 1
	9. Make sure that useOrdered is at value 1
	/***************CONFIGURE CONTROLLER***************/	
	The controller only works on a specific file system. This is one of the future improvements to make

(d) execute the replica: ./run-replicas.sh 4 0 xterm where 4 represents the number of replicas and 0 the starting id of the first replica (all others will be the increment of the starting id)
    (you can use "xterm" windows of run in background - it is recommended to use xterm to see the execution logs)

(e) execute the clients: ./runMeter.sh execTime samplingTime startingId numberOfClients
	- execTime -> Time that the simulation will run, specified in seconds. if the value is 0 the simulation will run for 8hours
	- samplingTime -> refresh rate of the meters
	- startingId -> the starting id of the client
	- numberOfClients -> number of publishers and subscribers. Currently only 4 at max are supported
	EXEMPLE: ./runMeter.sh 180 1 0 2 -> this will start a simulation for 3minutes withe a refresh rate of 1 second, starting id 0 and 2 clients

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

