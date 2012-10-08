package pt.ul.fc.di.navigators.trone.apps;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;
import pt.ul.fc.di.navigators.trone.utils.*;

/**
 *
 * @author kreutz
 */
public class CmdSubscriberClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length >= 4) {

            String channelTag;
            Request clientReq;
            MessageBrokerClient cchm;
            int numberOfEventsPerRound, numberOfRounds, timeToSleepPerRound, id;
            int numberOfEventsReceived = 0;
            long startTime, endTime;
            int eventContentSize;
            
            channelTag = args[0];
            numberOfRounds = Integer.parseInt(args[1]);
            numberOfEventsPerRound = Integer.parseInt(args[2]);
            timeToSleepPerRound = Integer.parseInt(args[3]);
            id = Integer.parseInt(args[4]);

            if (args.length < 6) {
                eventContentSize = Define.DEFAULTEVENTSIZE;
            } else {
                eventContentSize = Integer.parseInt(args[5]);
            }

            try {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < ((eventContentSize / IdGenerator.getUniqueIdMD5().length()) + 1); i++) {
                    sb.append(IdGenerator.getUniqueIdMD5());
                }

                String eventContent;
                if (eventContentSize > 1) 
                    eventContent = sb.substring(0, eventContentSize - 1);
                else 
                    eventContent = new String();

                Log.logInfoFlush(CmdSubscriberClient.class.getSimpleName(), "MESSAGE BROKER: STARTING ...", Log.getLineNumber());

                cchm = new MessageBrokerClient(id);

                Request xx = new Request();
                for (int i = 0; i < cchm.getNumberOfEventsPerCachedRequest(); i++) {
                    Event ex = new Event();
                    if (eventContentSize > 0)
                        ex.setContent(Integer.toString(i % 10) + eventContent + Integer.toString(i % 5));
                    else 
                        ex.setContent(Integer.toString(i % 10) + Integer.toString(i % 5));
                    xx.addEvent(ex);
                }
                

                startTime = CurrentTime.getTimeInMilliseconds();

                Log.logInfoFlush(CmdSubscriberClient.class.getSimpleName(), "MESSAGE BROKER: UP AND RUNNING ...", Log.getLineNumber());

                clientReq = cchm.subscribe(channelTag);

                Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "CLIENT ID: " + clientReq.getClientId() + " method: " + clientReq.getMethod() + " tag: " + clientReq.getChannelTag(), Log.getLineNumber());
                Thread.sleep(4000);
                if (clientReq.isOpSuccess()) {



                    Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "CLIENT START TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                    Request cReq = new Request();
                    long spendTime = 0;

                  
                    for (int round = 0; round < numberOfRounds; round++) {

                        long roundTime = System.currentTimeMillis();

                        int nEvents = 0;
                      
                        for (int i = 0; i < (numberOfEventsPerRound / cchm.getNumberOfEventsPerCachedRequest() + 1); i++) {
                            cReq = cchm.pollEventsFromChannel(channelTag, cchm.getNumberOfEventsPerPoll());
                            if (cReq != null) {
                                if (cReq.isOpSuccess()) {
                                    numberOfEventsReceived += cReq.getAllEvents().size();
                                    nEvents += cReq.getAllEvents().size();
                                } else {
                                    Log.logError(CmdPublisherClient.class.getSimpleName(), "AN ERROR OCCURRED", Log.getLineNumber());
                                }
                            }

                        }

                        long midTime = System.currentTimeMillis();
                        roundTime = midTime - roundTime;

                        //Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "INLOOP START TIME: " + startTime/1000 + " seconds END TIME: " + midTime/1000 + " seconds EXECUTION TIME: " + (midTime-startTime)/1000 + " seconds", Log.getLineNumber());
                        Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "INLOOP CLIENT ID: " + cchm.getClientId() + " NUMBER OF EVENTS RECEIVED: " + numberOfEventsReceived + " IN " + spendTime + " MILLI SECONDS FROM CHANNEL: " + channelTag + " START TIME: " + startTime / 1000 + " seconds END TIME: " + midTime / 1000 + " seconds EXECUTION TIME: " + (midTime - startTime) / 1000 + " seconds REQUEST SERIALIZED SIZE: " + Size.serializableObjectSizeOf(xx) + " WITH N EVENTS: " + cchm.getNumberOfEventsPerPoll() + " EVENT CONTENT SIZE: " + eventContentSize, Log.getLineNumber());

                        spendTime += roundTime;

                        if (cReq != null) {
                            Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "ROUND " + round + " COMPLETED with nEvents RECV equal to " + nEvents + " in " + roundTime + " milliseconds", Log.getLineNumber());
                        } else {
                            Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "ROUND " + round + " COMPLETED with nEvents RECV equal to " + 0 + " in " + roundTime + " milliseconds", Log.getLineNumber());
                        }

                        Thread.sleep(timeToSleepPerRound);

                    }

                    cchm.unSubscribe(channelTag);

                    endTime = CurrentTime.getTimeInMilliseconds();
                    cchm.closeConnection();
                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLOSING CONNECTION", Log.getLineNumber());

                    Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "CLIENT END TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                    Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "CLIENT ID: " + cchm.getClientId() + " NUMBER OF EVENTS RECEIVED: " + numberOfEventsReceived + " IN " + spendTime + " MILLI SECONDS FROM CHANNEL: " + channelTag + " START TIME: " + startTime / 1000 + " seconds END TIME: " + endTime / 1000 + " seconds TOTAL EXECUTION TIME: " + (endTime - startTime) / 1000 + " seconds REQUEST SERIALIZED SIZE: " + Size.serializableObjectSizeOf(xx) + " WITH N EVENTS: " + cchm.getNumberOfEventsPerPoll() + " EVENT CONTENT SIZE: " + eventContentSize, Log.getLineNumber());

                    //Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "START TIME: " + startTime/1000 + " seconds END TIME: " + endTime/1000 + " seconds TOTAL EXECUTION TIME: " + (endTime-startTime)/1000 + " seconds", Log.getLineNumber());

                } else {
                    Log.logWarning(CmdSubscriberClient.class.getSimpleName(), "could not subscribe SUBSCRIBER ID: " + clientReq.getClientId() + " within channel: " + clientReq.getChannelTag(), Log.getLineNumber());
                }
                
               
            } catch (Exception ex) {
                Logger.getLogger(CmdSubscriberClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.logError(CmdSubscriberClient.class.getSimpleName(), "Usage: java PublisherClientForCommandLineUse nameOfTheChannel numberOfRounds numberOfEventsPerRound timeToSleepPerRound", Log.getLineNumber());
        }
        
    }
}
