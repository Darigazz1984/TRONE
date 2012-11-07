/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import pt.ul.fc.di.navigators.trone.gui.DisplayPanel;
import pt.ul.fc.di.navigators.trone.gui.ReplicaControlPanel;
import pt.ul.fc.di.navigators.trone.gui.ReplicasControlWindow;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class CmdReplicaControler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
       
       LinkedList<ReplicaControlPanel> repControlPanel = new LinkedList<ReplicaControlPanel>();
       HashMap<String, String> ipPort = new HashMap<String, String>();
       ReplicasControlWindow rcw;
       
       
       
       /*
        * Fazer para ler uma pasta com as especificações das replicas, isto é so teste
        */
       
       FileInputStream fstream = new FileInputStream("controller.props");
       DataInputStream in = new DataInputStream(fstream);
       BufferedReader br = new BufferedReader(new InputStreamReader(in));
       String strLine;
       int number = 0;
        try {
            while ((strLine = br.readLine()) != null)   {
                //Queremos saltar os comentarios
                 if(!strLine.startsWith("#") && !strLine.isEmpty()){
                     repControlPanel.add(new ReplicaControlPanel(""+number, strLine.split("=")[0],Integer.parseInt(strLine.split("=")[1])));
                     number++;
                 }
            }
        } catch (IOException ex) {
            Log.logError( CmdReplicaControler.class.getCanonicalName(), "Erro ao ler ficheiro", Log.getLineNumber());
        }
       
       
       rcw = new ReplicasControlWindow("TRONE - Replicas", number);
       
       for(ReplicaControlPanel r: repControlPanel){
           rcw.addFrame(r);
       }
       
       rcw.show();
       //Painel que vai mostrar o tempo um evento demora de uma ponta a outra do sistema
       
       ConcurrentHashMap<String, LinkedList<Long>> pub = new ConcurrentHashMap<String, LinkedList<Long>>();
       
       ConcurrentHashMap<String, LinkedList<Long>> sub = new ConcurrentHashMap<String, LinkedList<Long>>();
       Semaphore sem = new Semaphore(1);
       //DisplayPanel dp = new DisplayPanel("Averege time per request");
       //dp.changeText("0 seconds");
       //dp.build();
      // Tempo t = new Tempo(pub, sub, sem, dp);
      // Timer timer = new Timer();
      // timer.schedule(t, 10000, 30000);
       
      // Server s = new Server(1250, sem);
      // s.run();
    }
    
    /*
     * Thread para actualizar o contador
     */
    
    static class Tempo extends TimerTask{
        ConcurrentHashMap<String, LinkedList<Long>> eventsPublisher;
        ConcurrentHashMap<String, LinkedList<Long>> eventsSubscriber;
        Semaphore block;
        DisplayPanel displayPanel;
        
        Tempo(ConcurrentHashMap<String, LinkedList<Long>> pub, ConcurrentHashMap<String, LinkedList<Long>> sub, Semaphore b, DisplayPanel dp){
            eventsPublisher = pub;
            eventsSubscriber = pub;
            block = b;
            displayPanel = dp;
        }
        @Override
        public void run() {
            LinkedList<Long> p = null;
            LinkedList<Long> s = null;
            double[] values;
            int i = 0;
            int counter;
            try {
                block.acquire();
                    values = new double[eventsPublisher.size()];
                    for(int z = 0; z<values.length; z++){
                        values[z] = 0;
                    }
                    
                    for(String k: eventsPublisher.keySet()){
                        counter = 0;
                        p = eventsPublisher.get(k);
                        s = eventsSubscriber.get(k);
                        while(counter < s.size()){
                            values[i]= values[i] +(s.get(counter)-p.get(counter));
                        }
                        values[i] = values[i]/counter;
                        i++;
                    }
                    
                    double total = 0;
                    for(int z = 0; z< values.length; z++){
                        total += values[z];
                    }
                    total = total / values.length;
                block.release();
                displayPanel.changeText(""+total+"seconds");
            } catch (InterruptedException ex) {
                Log.logError(Tempo.class.getCanonicalName(), "Erro ao adquirir sem", Log.getLineNumber());
            }
        }
    }
    
    /*
     * Thread para receber os tempos dos clientes
     */
    static class Server implements Runnable{
        ServerSocket ss;
        ConcurrentHashMap<String, LinkedList<Long>> eventsPublisher;
        ConcurrentHashMap<String, LinkedList<Long>> eventsSubscriber;
        Semaphore sem;
        
        Server(int port, Semaphore s){
            ss = null;
            sem = s;
            try { 
                ss = new ServerSocket(port);
            } catch (IOException ex) {
                Log.logError(Server.class.getCanonicalName(), "Erro ao criar o porto do servidor para receber o tempo do request", Log.getLineNumber());
            }
        }
        @Override
        public void run() {
            Socket client = null;
            ObjectInputStream stream = null;
            if(ss != null){
                while(true){
                    try {
                        client = ss.accept();
                        stream = new ObjectInputStream(client.getInputStream());

                        if(stream != null){
                            try {
                                String channel[] = ((String)stream.readObject()).split("=");
                                if(channel[0].equals("pub")){
                                    if(eventsPublisher.contains(channel[1])){
                                        sem.acquire();
                                        eventsPublisher.get(channel[1]).add(System.currentTimeMillis());
                                        sem.release();
                                    }else{
                                        LinkedList<Long> ll = new LinkedList<Long>();
                                        ll.add(System.currentTimeMillis());
                                        eventsPublisher.put(channel[1], ll);
                                    }
                                }else{
                                    if(eventsSubscriber.contains(channel[1])){
                                        sem.acquire();
                                        eventsPublisher.get(channel[1]).add(System.currentTimeMillis());
                                        sem.release();
                                    }else{
                                        LinkedList<Long> ll = new LinkedList<Long>();
                                        ll.add(System.currentTimeMillis());
                                        sem.acquire();
                                        eventsSubscriber.put(channel[1], ll);
                                        sem.release();
                                    }
                                }


                            } catch (InterruptedException ex) {
                                Log.logError(Server.class.getCanonicalName(), "Erro ao adquirir sem", Log.getLineNumber());
                            } catch (ClassNotFoundException ex) {
                                Log.logError(Server.class.getCanonicalName(), "Erro ao ler string", Log.getLineNumber());
                            }
                        }

                    } catch (IOException ex) {
                        Log.logError(Server.class.getCanonicalName(), "Erro ao aceitar ligação do cliente", Log.getLineNumber());
                    }
                }
            }else{
                Log.logError(Server.class.getCanonicalName(),"Erro ao iniciar servidor", Log.getLineNumber());
            }
        }
        
    }
}
