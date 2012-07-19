/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.*;
import java.net.Socket;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.SerializeExternalize;

/**
 *
 * @author kreutz
 */
public class SimpleEchoClientNew {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int port = 6002;
        int iterations = 10000;

        try {

            Socket socket = new Socket("localhost", port);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            MessageS res;
            MessageS req = new MessageS();
            req.setMessage("pingpong-client");

            double start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {

                objectOutputStream.writeObject(req);
                objectOutputStream.flush();

                res = (MessageS) objectInputStream.readObject();

            }
            double finish = System.currentTimeMillis();
            System.out.println("Latency for CACHE SETUP (time per ping): " + (finish - start) / iterations);

            MessageX response;
            MessageX request = new MessageX();
            request.setMessage("pingpong-client");

            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(request);
                objectOutputStream.flush();

                response = (MessageX) objectInputStream.readObject();

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for SERIALIZABLE (time per ping): " + (finish - start) / iterations);

            MessageSerializable responseB;
            MessageSerializable requestB = new MessageSerializable();
            requestB.setMessage("pingpong-client");
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(requestB);
                objectOutputStream.flush();

                responseB = (MessageSerializable) objectInputStream.readObject();

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for SERIALIZABLE B (time per ping): " + (finish - start) / iterations);

            MessageSerializable responseSer;
            MessageSerializable requestSer = new MessageSerializable();
            requestSer.setMessage("pingpong-client");
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(SerializeExternalize.serializeObject(requestSer));
                objectOutputStream.flush();

                byte[] nBytes = (byte[]) objectInputStream.readObject();
                responseSer = (MessageSerializable) SerializeExternalize.deserializeObject(nBytes);

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for SERIALIZABLE BY HAND (time per ping): " + (finish - start) / iterations);

            MessageExternalizable responseExt;
            MessageExternalizable requestExt = new MessageExternalizable();

            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(requestExt);

                responseExt = (MessageExternalizable) objectInputStream.readObject();

                responseExt.getMessage();

            }
            finish = System.currentTimeMillis();

            System.out.println("Latency for EXTERNALIZE (time per ping): " + (finish - start) / iterations);

            Request responseX;
            Request requestX = new Request();
            requestX.addEvent(new Event());
            requestX.setChannelTag("network");
            requestX.setMethod(METHOD.PUBLISH);
            
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(requestX);
                objectOutputStream.flush();

                responseX = (Request) objectInputStream.readObject();

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for REQUEST SERIALIZABLE (time per ping): " + (finish - start) / iterations);

            Request responseZ;
            
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                Request requestZ = new Request();
                requestZ.setEvent(new Event());
                requestZ.setChannelTag("network");
                requestZ.setMethod(METHOD.SUBSCRIBE);
                
                objectOutputStream.writeObject(requestZ);
                objectOutputStream.flush();

                responseZ = (Request) objectInputStream.readObject();

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for REQUEST SERIALIZABLE INSIDE LOOP (time per ping): " + (finish - start) / iterations);
            
            Request responseW;
            Request requestZ = new Request();
                
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                requestZ.setEvent(new Event());
                requestZ.setChannelTag("network");
                requestZ.setMethod(METHOD.POLL);
                
                objectOutputStream.writeObject(requestZ);
                objectOutputStream.flush();

                responseW = (Request) objectInputStream.readObject();

            }
            finish = System.currentTimeMillis();
            System.out.println("Latency for REQUEST SERIALIZABLE INSIDE LOOP V2 (time per ping): " + (finish - start) / iterations);
            
        } catch (Exception e) {
        }
    }
}
