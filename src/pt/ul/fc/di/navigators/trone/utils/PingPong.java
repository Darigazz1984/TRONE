/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author kreutz
 */
public class PingPong {

    private String myIp;
    private int myPort;
    private int numberOfMessages;
    private int sizeOfEachMessage;
    private Object messageToSend;

    public PingPong(String ip, int port, int nMessages, Object message) {
        myIp = ip;
        myPort = port;
        numberOfMessages = nMessages;
        sizeOfEachMessage = 0;
        messageToSend = message;
    }

    public PingPong(String ip, int port, int nMessages, int messageSize) {
        myIp = ip;
        myPort = port;
        numberOfMessages = nMessages;
        sizeOfEachMessage = messageSize;
        messageToSend = null;
    }

    public void client(boolean oneConnection) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        if (oneConnection) {
            System.out.println("[INFO] ONE LONG TERM CONNECTION");
            clientOneConnection();
        } else {
            System.out.println("[INFO] MANY SHORT TERM CONNECTIONS");
            clientOneConnectionPerMessage();
        }
    }

    public void server(boolean oneConnection) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        if (oneConnection) {
            System.out.println("[INFO] ONE LONG TERM CONNECTION");
            serverOneConnection();
        } else {
            System.out.println("[INFO] MANY SHORT TERM CONNECTIONS");
            serverOneConnectionPerMessage();
        }
    }

    private void clientOneConnection() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {

        Socket mySocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;
        long spendTime;
        Object req;

        mySocket = new Socket(myIp, myPort);
        cOut = new ObjectOutputStream(mySocket.getOutputStream());
        cIn = new ObjectInputStream(mySocket.getInputStream());

        if (mySocket != null) {

            if (sizeOfEachMessage > 0) {
                req = new ObjectOfAnySize(sizeOfEachMessage);
            } else {
                req = messageToSend;
            }
            
            System.out.println("Request SIZEOF: " + Size.serializableObjectSizeOf(req));

            spendTime = System.currentTimeMillis();
            for (int i = 0; i < numberOfMessages; i++) {

                cOut.writeObject(req);
                req = null;
                //cOut.flush();

                req = cIn.readObject();
               
            }
            spendTime = System.currentTimeMillis() - spendTime;

            showStats(spendTime, req);

            cOut.close();
            cIn.close();
            mySocket.close();

        } else {
            System.err.println("WARNING: could not connect to SERVER: " + myIp + " and PORT: " + myPort);
        }
    }

    private void clientOneConnectionPerMessage() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException {

        Socket mySocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;
        long spendTime;
        Object req;

        if (sizeOfEachMessage > 0) {
            req = new ObjectOfAnySize(sizeOfEachMessage);
        } else {
            req = messageToSend;
        }

        System.out.println("Request SIZEOF: " + Size.serializableObjectSizeOf(req));

        spendTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfMessages; i++) {

            mySocket = new Socket(myIp, myPort);
            cOut = new ObjectOutputStream(mySocket.getOutputStream());
            cIn = new ObjectInputStream(mySocket.getInputStream());

            if (mySocket != null) {

                cOut.writeObject(req);
                
                cOut.flush();
                
                req = cIn.readObject();

            } else {
                System.err.println("WARNING: could not connect to SERVER: " + myIp + " and PORT: " + myPort);
            }

            cOut.close();
            cIn.close();
            mySocket.close();

        }

        spendTime = System.currentTimeMillis() - spendTime;

        showStats(spendTime, req);

    }

    private void serverOneConnection() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {


        Socket mySocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;
        long spendTime = 0;
        Object req = null;
        
        ServerSocket serverSocket = new ServerSocket(myPort);
        mySocket = serverSocket.accept();

        if (mySocket != null) {

            cOut = new ObjectOutputStream(mySocket.getOutputStream());
            cIn = new ObjectInputStream(mySocket.getInputStream());

            spendTime = System.currentTimeMillis();
                
            for (int i = 0; i < numberOfMessages; i++) {
                
                req = cIn.readObject();
                
                cOut.writeObject(req);
                //cOut.flush();
                
                req = null;

            }

            spendTime = System.currentTimeMillis() - spendTime;

            cOut.close();
            cIn.close();
            mySocket.close();

        } else {
            System.err.println("WARNING: could not connect to SERVER: " + myIp + " and PORT: " + myPort);
        }


        showStats(spendTime, req);

    }

    private void serverOneConnectionPerMessage() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException {

        Socket mySocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;
        long spendTime = 0;
        Object req = null;

        ServerSocket serverSocket = new ServerSocket(myPort);

        spendTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfMessages; i++) {

            mySocket = serverSocket.accept();

            if (mySocket != null) {

                cOut = new ObjectOutputStream(mySocket.getOutputStream());
                cIn = new ObjectInputStream(mySocket.getInputStream());

                req = cIn.readObject();

                cOut.writeObject(req);
                
                cOut.flush();

                cOut.close();
                cIn.close();
                mySocket.close();

            } else {
                System.err.println("WARNING: could not connect to SERVER: " + myIp + " and PORT: " + myPort);
            }

        }

        spendTime = System.currentTimeMillis() - spendTime;

        showStats(spendTime, req);

    }

    private void showStats(long spendTime, Object o) throws IOException {
        System.out.println("[STATS] NUMBER OF MESSAGES: " + numberOfMessages + " SIZE OF EACH MESSAGE: " + Size.serializableObjectSizeOf(o));
        System.out.println("[STATS] TIME SPEND (IN MILLI SECONDS): " + spendTime + " MESSAGES PER SECOND: " + (float) numberOfMessages / (float) (spendTime / 1000.0) + " NUMBER OF BYTES PER SECOND: " + ((float)numberOfMessages / (float)(spendTime / 1000.0)) * (float)Size.serializableObjectSizeOf(o));
    }
}

class ObjectOfAnySize implements Serializable {

    private byte[] nBytes;

    public ObjectOfAnySize() {
        super();
    }
    
    public ObjectOfAnySize(int size) {
        nBytes = new byte[size];
    }

    public int getSize() {
        return nBytes.length;
    }

    public void setSize(int size) {
        nBytes = new byte[size];
    }

    public void setSezo() {
        setValue('0');
    }

    public void setValue(char c) {
        for (int i = 0; i < nBytes.length; i++) {
            nBytes[i] = (byte) c;
        }
    }
    
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.nBytes = (byte[]) objectInput.readObject();
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(nBytes);
    }
    
}