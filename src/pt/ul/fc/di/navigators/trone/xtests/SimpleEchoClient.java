/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 *
 * @author kreutz
 */
public class SimpleEchoClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int iterations = 100;
        try {
            Socket socket = new Socket("localhost", 6001);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            MessageC request = new MessageC();
            request.setMessage("pingpong-client");
            MessageC response;
            
            double start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {

                objectOutputStream.writeObject(request);

                response = (MessageC) objectInputStream.readObject();
                
                System.out.println("it: " + i + " msg received: " + response.getMessage() + " to send: " + request.getMessage());
            }

            double finish = System.currentTimeMillis();

            System.out.println("Latency (time per ping): " + (finish - start) / iterations);

        } catch (Exception e) {
        }
    }
}

