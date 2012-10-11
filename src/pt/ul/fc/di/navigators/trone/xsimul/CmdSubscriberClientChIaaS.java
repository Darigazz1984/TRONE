package pt.ul.fc.di.navigators.trone.xsimul;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class CmdSubscriberClientChIaaS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Request clientReq;
        MessageBrokerClient cchm;
        String channelTag = "iaas";
        int numberOfEventsPerRound, numberOfRounds, timeToSleepPerRound;
        int numberOfEventsReceived = 0;

        numberOfRounds = 10;
        numberOfEventsPerRound = 100;
        timeToSleepPerRound = 1000;

        try {
            Log.logInfoFlush(CmdSubscriberClientChNetwork.class.getSimpleName(), "MESSAGE BROKER: STARTING ...", Log.getLineNumber());

           //FIX ME: THIS MUST BE CHANGED TO GET THE RIGHT CLIENT ID
            cchm = new MessageBrokerClient(1);

            Log.logInfoFlush(CmdSubscriberClientChNetwork.class.getSimpleName(), "MESSAGE BROKER: UP AND RUNNING ...", Log.getLineNumber());

            clientReq = cchm.subscribe(channelTag);

            if (clientReq != null) {
                
                Log.logInfo(CmdSubscriberClientChNetwork.class.getSimpleName(), "CLIENT ID: " + clientReq.getClientId() + " method: " + clientReq.getMethod() + " tag: " + clientReq.getChannelTag(), Log.getLineNumber());

                if (clientReq.isOpSuccess()) {

                    Thread.sleep(5000);

                    Log.logInfo(CmdSubscriberClientChNetwork.class.getSimpleName(), "CLIENT START TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                    Request cReq;
                    long spendTime = 0;

                    for (int round = 0; round < numberOfRounds; round++) {

                        long roundTime = System.currentTimeMillis();

                        //for (int i = 0; i < (numberOfEventsPerRound); i++) {
                        for (int i = 0; i < (numberOfEventsPerRound / cchm.getNumberOfEventsPerPoll() + 1); i++) {
                            cReq = cchm.pollAllEventsFromChannel(channelTag);
                            if (cReq != null) {
                                if (cReq.isOpSuccess()) {
                                    numberOfEventsReceived += cReq.getAllEvents().size();
                                    //Log.logInfo("N EVENTS RECEIVED: " + cReq.getAllEvents().size());
                                }
                            }

                        }

                        roundTime = System.currentTimeMillis() - roundTime;

                        spendTime += roundTime;

                        Log.logInfo(CmdSubscriberClientChNetwork.class.getSimpleName(), "ROUND " + round + " COMPLETED with nEvents RECV equal to " + numberOfEventsReceived + " in " + roundTime + " milliseconds", Log.getLineNumber());

                        Thread.sleep(timeToSleepPerRound);

                    }

                    cchm.unSubscribe(channelTag);

                    Log.logInfo(CmdSubscriberClientChNetwork.class.getSimpleName(), "CLIENT END TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                    Log.logInfo(CmdSubscriberClientChNetwork.class.getSimpleName(), "NUMBER OF EVENTS RECEIVED: " + numberOfEventsReceived + " IN " + spendTime / 1000 + " SECONDS", Log.getLineNumber());
                }
            } else {
                Log.logWarning(CmdSubscriberClientChNetwork.class.getSimpleName(), "could not subscribe within channel: " + channelTag, Log.getLineNumber());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(CmdSubscriberClientChNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
