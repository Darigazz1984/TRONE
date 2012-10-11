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
public class CmdPublisherClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, NoSuchAlgorithmException {

        if (args.length >= 4) {

            String channelTag;
            Request clientReq;
            MessageBrokerClient cchm;
            int numberOfEventsPerRound, numberOfRounds, timeToSleepPerRound, id;
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

                Log.logInfoFlush(CmdPublisherClient.class.getSimpleName(), "MESSAGE BROKER: STARTING ...", Log.getLineNumber());

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ((eventContentSize / IdGenerator.getUniqueIdMD5().length()) + 1); i++) {
                    sb.append(IdGenerator.getUniqueIdMD5());
                }
                String eventContent;
                if (eventContentSize > 1)
                    eventContent = sb.substring(0, eventContentSize - 1);
                else
                    eventContent = new String();

                cchm = new MessageBrokerClient(id);

                Request xx = new Request();
                for (int i = 0; i < cchm.getNumberOfEventsPerCachedRequest(); i++) {
                    Event ex = new Event();
                    if (eventContentSize > 0)
                        ex.setContent(Integer.toString(i % 10) + eventContent + Integer.toString(i % 5) );
                    else 
                        ex.setContent(Integer.toString(i % 10) + Integer.toString(i % 5));
                    xx.addEvent(ex);
                }

                Log.logInfoFlush(CmdPublisherClient.class.getSimpleName(), "MESSAGE BROKER: UP AND RUNNING ...", Log.getLineNumber());

                startTime = CurrentTime.getTimeInMilliseconds();
                
                clientReq = cchm.register(channelTag);
                
                if (clientReq != null) {
                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLIENT ID: " + clientReq.getClientId() + " execution of method: " + clientReq.getMethod() + " SUCCESS: " + clientReq.isOpSuccess(), Log.getLineNumber());
                } else {
                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "REQUEST EQUAL NULL", Log.getLineNumber());
                }


                if (clientReq != null && clientReq.isOpSuccess()) {

                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLIENT START TIME:" + System.currentTimeMillis(), Log.getLineNumber());

                    long spendTime = 0;
                    int numberOfEventsSent = 0;
                    Request rReq;
                    for (int round = 0; round < numberOfRounds; round++) {

                        long roundTime = System.currentTimeMillis();

                        for (int i = 0; i < numberOfEventsPerRound; i++) {
                            Event e = new Event();
                            e.setContent(Integer.toString(i % 10) + eventContent + Integer.toString(i % 5));
                            rReq = cchm.publishWithCaching(e, channelTag);
                            
                            if (rReq != null) {
                                if (rReq.isOpSuccess()) {
                                    //
                                } else {
                                    Log.logError(CmdPublisherClient.class.getSimpleName(), "AN ERROR OCCURRED", Log.getLineNumber());
                                }
                            }
                            numberOfEventsSent++;
                        }

                        long midTime = System.currentTimeMillis();
                        roundTime = midTime - roundTime;

                        Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "INLOOP CLIENT ID: " + cchm.getClientId() + "  NUMBER OF EVENTS SENT: " + numberOfEventsSent + " IN " + spendTime + " MILLI SECONDS FROM CHANNEL: " + channelTag + " START TIME: " + startTime / 1000 + " seconds END TIME: " + midTime / 1000 + " seconds EXECUTION TIME: " + (midTime - startTime) / 1000 + " seconds REQUEST SERIALIZED SIZE: " + Size.serializableObjectSizeOf(xx) + " WITH N EVENTS: " + cchm.getNumberOfEventsPerCachedRequest() + " EVENT CONTENT SIZE: " + eventContentSize, Log.getLineNumber());

                        spendTime += roundTime;

                        Log.logInfo(CmdPublisherClient.class.getSimpleName(), "ROUND " + round + " COMPLETED with nEvents PUBLISHED equal to " + numberOfEventsSent + " in " + roundTime + " milliseconds", Log.getLineNumber());

                        Thread.sleep(timeToSleepPerRound);
                    }

                    endTime = CurrentTime.getTimeInMilliseconds();

                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLIENT END TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                    Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLIENT ID: " + cchm.getClientId() + " NUMBER OF EVENTS SENT: " + numberOfEventsSent + " IN " + spendTime + " MILLI SECONDS TO CHANNEL: " + channelTag + " START TIME: " + startTime / 1000 + " seconds END TIME: " + endTime / 1000 + " seconds TOTAL EXECUTION TIME: " + (endTime - startTime) / 1000 + " seconds REQUEST SERIALIZED SIZE: " + Size.serializableObjectSizeOf(xx) + " WITH N EVENTS: " + cchm.getNumberOfEventsPerCachedRequest() + " EVENT CONTENT SIZE: " + eventContentSize, Log.getLineNumber());

                    //Log.logInfo(CmdSubscriberClient.class.getSimpleName(), "START TIME: " + startTime/1000 + " seconds END TIME: " + endTime/1000 + " seconds TOTAL EXECUTION TIME: " + (endTime-startTime)/1000 + " seconds", Log.getLineNumber());

                } else {
                    if (clientReq != null) {
                        Log.logWarning(CmdPublisherClient.class.getSimpleName(), "could not register PUBLISHER ID: " + clientReq.getClientId() + " within channel: " + clientReq.getChannelTag(), Log.getLineNumber());
                    }
                }

                cchm.unRegister(channelTag);
                cchm.closeConnection();
                Log.logInfo(CmdPublisherClient.class.getSimpleName(), "CLOSING CONNECTION", Log.getLineNumber());
                
            } catch (Exception ex) {
                Logger.getLogger(CmdPublisherClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.logError(CmdPublisherClient.class.getSimpleName(), "Usage: java PublisherClientForCommandLineUse nameOfTheChannel numberOfRounds numberOfEventsPerRound timeToSleepPerRound", Log.getLineNumber());
        }
    }
}
