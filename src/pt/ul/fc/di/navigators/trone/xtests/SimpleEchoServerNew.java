/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.SerializeExternalize;

/**
 *
 * @author kreutz
 */
public class SimpleEchoServerNew {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int port = 6002;
        int iterations = 10000;

        try {

            System.out.println("Running server at port " + port + " (waiting for connections)");
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());


            MessageS res = new MessageS();
            MessageS req;

            for (int i = 0; i < 1000; ++i) {

                req = (MessageS) objectInputStream.readObject();

                objectOutputStream.writeObject(res);

            }

            MessageX response = new MessageX();
            response.setMessage("pingpong-server");
            MessageX request;
            for (int i = 0; i < iterations; ++i) {

                request = (MessageX) objectInputStream.readObject();

                objectOutputStream.writeObject(response);

            }

            MessageSerializable responseB = new MessageSerializable();
            response.setMessage("pingpong-server");
            MessageSerializable requestB;

            for (int i = 0; i < iterations; ++i) {

                requestB = (MessageSerializable) objectInputStream.readObject();

                objectOutputStream.writeObject(responseB);

            }

            MessageSerializable responseSer = new MessageSerializable();
            response.setMessage("pingpong-server");
            MessageSerializable requestSer;

            for (int i = 0; i < iterations; ++i) {

                byte[] nBytes = (byte[]) objectInputStream.readObject();
                requestSer = (MessageSerializable) SerializeExternalize.deserializeObject(nBytes);

                objectOutputStream.writeObject(SerializeExternalize.serializeObject(responseSer));

            }


            MessageExternalizable responseExt = new MessageExternalizable();
            MessageExternalizable requestExt;
            for (int i = 0; i < iterations; ++i) {

                requestExt = (MessageExternalizable) objectInputStream.readObject();

                requestExt.getMessage();

                objectOutputStream.writeObject(responseExt);

            }

            Request requestX;
            Request responseX = new Request();
            responseX.addEvent(new Event());
            responseX.setChannelTag("security");
            responseX.setMethod(METHOD.UNREGISTER);
            for (int i = 0; i < iterations; ++i) {

                requestX = (Request) objectInputStream.readObject();

                requestX.getMethod();

                objectOutputStream.writeObject(responseX);

            }

            for (int i = 0; i < iterations; ++i) {

                requestX = (Request) objectInputStream.readObject();

                requestX.getMethod();

                objectOutputStream.writeObject(responseX);

            }

            for (int i = 0; i < iterations; ++i) {

                requestX = (Request) objectInputStream.readObject();

                requestX.getMethod();

                objectOutputStream.writeObject(responseX);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
