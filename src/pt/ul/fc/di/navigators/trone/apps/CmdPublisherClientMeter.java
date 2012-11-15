/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;


import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;
import pt.ul.fc.di.navigators.trone.utils.DialMeter;
import pt.ul.fc.di.navigators.trone.utils.LineChart;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class CmdPublisherClientMeter {
    static int samplingRate = 1000;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, InterruptedException {
       
        if(args.length<8){
            Log.logInfo(CmdPublisherClientMeter.class.getCanonicalName(), "Argumentos Invalidos", Log.getLineNumber());
            System.out.println("Argumentos Errados: Test_time Sampling_rate num_clients");
            System.exit(0);
        }
        
        
        HashMap<String, MessageBrokerClient> map = new HashMap<String, MessageBrokerClient>();
        HashMap<String, AtomicInteger> counter = new HashMap<String, AtomicInteger>();
        
        int testTime = Integer.parseInt(args[0]); // tempo de duracao do teste
        //int samplingRate = Integer.parseInt(args[1]); //sampling rate
        int startingID = Integer.parseInt(args[2]); // id inicial dos clientes
        int numberOfClients = Integer.parseInt(args[3]); // numero total de clientes
        
        //vamos dar a possibilidade de correr testes "infinitos"
        if (testTime == 0)
            testTime = 28800;
        
        for(int i = 0; i<numberOfClients; i++){
            Log.logInfo(CmdPublisherClientMeter.class.getCanonicalName(), args[4+i], testTime);
            map.put(args[4+i], new MessageBrokerClient(startingID+i, "pubclientConfig.props"));
            counter.put(args[4+i],new AtomicInteger());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("TRONE - Sent request per second ");
        for(String s: map.keySet()){
            sb.append("["+s+"]");
            sb.append(" ");
        }
        
        //Titulo, unidades, Label em cima, inicio, fim, distancia entre os valores, cor de fundo
        DialMeter dm = new DialMeter(sb.toString(), "req/sec", "Sent Requests", 0, 9000, 1000, new Color(240,128,128));
        dm.pack();
        dm.setVisible(true);
        
        CountDownLatch sem = new CountDownLatch(1);
        for(String x: map.keySet()){
            Sender s = new Sender(x, map.get(x), sem, testTime, counter.get(x));
            if(s.register()){
                s.start();
            }else
                Log.logError(CmdPublisherClientMeter.class.getCanonicalName(), "ERRO AO TENTAR REGISTAR TAG: "+x, Log.getLineNumber());
        }
        
       
        
        LineChart lc = new LineChart(sb.toString(), "Sent Events", "Seconds");
        lc.pack();
        lc.addValueNoRange(0, 0);
        Drawer d = new Drawer(dm, lc, samplingRate, counter);
        
        
        sem.countDown();
        Timer t = new Timer();
        t.schedule(d, 0, (int)(samplingRate/**1000*/));
        try {
            Thread.sleep(testTime*1000+1500); // isto é o tempo do teste NÃO MEXER
        } catch (InterruptedException ex) {
            Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
        }
        t.cancel();
        lc.refresh();
        lc.saveChart("Chart_Publisher.jpg", testTime);
        //lc.saveFullChart("Chart_Publsiher.jpg", testTime);
        Log.logInfo(CmdPublisherClientMeter.class.getCanonicalName(), "PUBLIQUEI: "+d.getDisplayedEvents(), Log.getLineNumber());
        //Thread.sleep(750);
        //System.exit(0);
    }
    
    
    static class Sender extends Thread{
        MessageBrokerClient mbc;
        String tag;
        CountDownLatch sem;
        int testDuration;
        AtomicInteger counter;
        SenderPing sp;
        
        Sender(String t, MessageBrokerClient m, CountDownLatch cdl, int td, AtomicInteger c){
            mbc = m;
            tag = t;
            sem = cdl;
            testDuration = td;
            counter = c;
            sp = new SenderPing("pub="+tag, mbc.getTimeIP(), mbc.getTimePort());
        }
        
        boolean register(){
            Request r = null;
            try {
                r = mbc.register(tag);
                if(r!= null && r.isOpSuccess()){
                    
                    Log.logInfo(this, "CLIENTE: "+r.getClientId()+" REGISTADO EM: "+r.getChannelTag(), testDuration);
                }
            } catch (IOException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(r != null)
                return true;
            return false;
        }
        
        void unRegister(){
            try {
                mbc.unRegister(tag);
            } catch (IOException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void run(){
            Request response = null;
            try {
                sem.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            long termTime = System.currentTimeMillis()+(testDuration*1000);
            Event e = new Event();
            Long currentTime = System.currentTimeMillis();
            while(currentTime<termTime){
                e = new Event();
                e.setContent(Long.toString(currentTime % 10) + Long.toString(currentTime % 5));
                try {
                    response = mbc.publishWithCaching(e, tag);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Significa que o pedido foi enviado
                if(response != null){
                    counter.addAndGet(mbc.getNumberOfEventsPerCachedRequest());
                    response = null;
                }
                currentTime = System.currentTimeMillis();
            }
            
            try {
                unRegister();
                mbc.closeConnection();
            } catch (IOException ex) {
                Logger.getLogger(CmdPublisherClientMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
    }
    
    static class Drawer extends TimerTask{
        DialMeter dm;
        LineChart lc;
        //int total;
        int timesCalled;
        int samplingRate;
        int displayedEvents;
        HashMap<String, AtomicInteger> counter;
        
        
        Drawer(DialMeter d, LineChart l, int it, HashMap<String, AtomicInteger> c){
            dm = d;
            lc = l;
            samplingRate = it;
            counter = c;
            timesCalled = 0;
           
        }
        
        public void run(){
            int eventsToShow = sum() - displayedEvents; // soma de todos os eventos enviados menos os que já foram mostrados
            displayedEvents += eventsToShow; //eventos que já foram mostrados
            dm.addValue(eventsToShow/**(1000/samplingRate)*/); //eventos para mostrar
            dm.setNumberOfEvents(displayedEvents); 
            
            dm.refresh();
            
            lc.addValueNoRange(eventsToShow, timesCalled*samplingRate);
            timesCalled++;
        }
        
        private int sum(){
            int result = 0;
            for(String s: counter.keySet()){
                result += counter.get(s).get();
            }
            return result;
        }
        
        int getDisplayedEvents(){
            return this.displayedEvents;
        }
    }
    
    
    static class SenderPing implements Runnable{
        Socket s;
        ObjectOutputStream out;
        String message;
        String ip;
        int port;
        
        public SenderPing(String m, String i, int p){
            s = null;
            out = null;
            message = m;
            ip = i;
            port = p;
        }

        @Override
        public void run() {
            try {
                s = new Socket(ip, port);
                out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
            } catch (UnknownHostException ex) {
                Log.logError(SenderPing.class.getCanonicalName(), "Erro ao criar SOCKET", Log.getLineNumber());
            } catch (IOException ex) {
                Log.logError(SenderPing.class.getCanonicalName(), "Erro ao criar SOCKET", Log.getLineNumber());
            }
            try {
                out.writeObject(message);
            } catch (IOException ex) {
                Log.logError(SenderPing.class.getCanonicalName(), "Erro ao enviar mensagem", Log.getLineNumber());
            }
            try {
                out.close();
                s.close();
            } catch (IOException ex) {
                Log.logError(SenderPing.class.getCanonicalName(), "Erro ao fechar stream e socket", Log.getLineNumber());
            }
            
            
            
        }
        
        
        
    }
}
