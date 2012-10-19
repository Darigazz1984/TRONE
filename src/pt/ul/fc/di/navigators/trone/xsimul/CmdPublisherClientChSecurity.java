package pt.ul.fc.di.navigators.trone.xsimul;

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
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class CmdPublisherClientChSecurity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, NoSuchAlgorithmException {

        Request clientReq;
        MessageBrokerClient cchm;
        String channelTag = "security";
        int numberOfEventsPerRound, numberOfRounds, timeToSleepPerRound;

        numberOfRounds = 10;
        numberOfEventsPerRound = 1000;
        timeToSleepPerRound = 2000;

        try {

            Log.logInfoFlush(CmdPublisherClientChNetwork.class.getSimpleName(), "MESSAGE BROKER: STARTING ...", Log.getLineNumber());

            //FIX ME: THIS MUST BE CHANGED TO GET THE RIGHT CLIENT ID
            cchm = new MessageBrokerClient(0, "");

            Log.logInfoFlush(CmdPublisherClientChNetwork.class.getSimpleName(), "MESSAGE BROKER: UP AND RUNNING ...", Log.getLineNumber());

            clientReq = cchm.register(channelTag);

            if (clientReq != null) {
                Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "CLIENT ID: " + clientReq.getClientId() + " execution of method: " + clientReq.getMethod() + " SUCCESS: " + clientReq.isOpSuccess(), Log.getLineNumber());
            } else {
                Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "REQUEST EQUAL NULL", Log.getLineNumber());
            }


            if (clientReq != null && clientReq.isOpSuccess()) {

                Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "CLIENT START TIME:" + System.currentTimeMillis(), Log.getLineNumber());

                long spendTime = 0;
                int numberOfEventsSent = 0;
                Event e = new Event();
                Request rReq;
                for (int round = 0; round < numberOfRounds; round++) {

                    long roundTime = System.currentTimeMillis();

                    for (int i = 0; i < numberOfEventsPerRound; i++) {
                        e.setContent(clientReq.getClientId());
                        rReq = cchm.publishWithCaching(e, channelTag);
                        if (rReq != null) {
                            if (rReq.isOpSuccess()) {
                                //numberOfEventsSent++;
                            }
                        }
                        numberOfEventsSent++;
                    }

                    roundTime = System.currentTimeMillis() - roundTime;

                    spendTime += roundTime;

                    Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "ROUND " + round + " COMPLETED with nEvents PUBLISHED equal to " + numberOfEventsSent + " in " + roundTime + " milliseconds", Log.getLineNumber());

                    Thread.sleep((long) (timeToSleepPerRound * 1.2));
                }

                Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "CLIENT END TIME: " + System.currentTimeMillis(), Log.getLineNumber());

                Log.logInfo(CmdPublisherClientChNetwork.class.getSimpleName(), "NUMBER OF EVENTS SENT: " + numberOfEventsSent + " IN " + spendTime / 1000 + " SECONDS", Log.getLineNumber());

            } else {
                if (clientReq != null) {
                    Log.logWarning(CmdPublisherClientChNetwork.class.getSimpleName(), "could not register PUBLISHER ID: " + clientReq.getClientId() + " within channel: " + clientReq.getChannelTag(), Log.getLineNumber());
                }
            }

            cchm.unRegister(channelTag);

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(CmdPublisherClientChNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
