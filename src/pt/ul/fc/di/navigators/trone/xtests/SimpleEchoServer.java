package pt.ul.fc.di.navigators.trone.xtests;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author kreutz
 */
public class SimpleEchoServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
    int iterations = 100;
    
    try {

        ServerSocket serverSocket = new ServerSocket(6001);
        Socket socket = serverSocket.accept();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        MessageC response = new MessageC();
        response.setMessage("pingpong-server");
        MessageC request;
        
        for (int i = 0; i < iterations; ++i) {
            
            request = (MessageC)objectInputStream.readObject();
            System.out.println("it: " + i + " msg received: " + request.getMessage() + " to send as response: " + response.getMessage());
                        
            objectOutputStream.writeObject(response);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
}
