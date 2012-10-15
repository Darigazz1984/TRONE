/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.ui.RefineryUtilities;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;
import pt.ul.fc.di.navigators.trone.utils.LineChart;
import pt.ul.fc.di.navigators.trone.utils.Log;
/**
 *
 * @author igor
 */
public class CmdPublisherClientChart {
   
    static HashMap<String, Integer> values;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, Exception {
        int totalTestTime; // Tempo total que o teste deve demorar
        int testRate; //Tempo de cada amostra
        String channelTag; //Nome do canal
        int totalEventsSent; //Numero total de eventos
       
        values = new HashMap<String, Integer>();
        values.put("cpu", 0);
        values.put("storage", 0);
        int id;
        
        if(args.length < 3){
            Log.logError(CmdPublisherClientChart.class.getSimpleName(), "Erro nos argumentos.", Log.getLineNumber());
            System.out.println("Argumentos errados: Tempo_total_Teste sample_rate id");
            System.exit(0);
        }
        
        //Grafico
        LineChart lc = new LineChart("Eventos Enviados por Segundo CPU+STORAGE", "Eventos Enviados", "Tempo Decorrido em Segundos");
        lc.pack();
        RefineryUtilities.centerFrameOnScreen(lc);
        lc.setVisible(true);
        lc.addValue(0, 0, 1);
        lc.refresh();
        
        //
        totalTestTime = Integer.parseInt(args[0]);
        testRate = Integer.parseInt(args[1]); 
        id = Integer.parseInt(args[2]);
        MessageBrokerClient cchm = null;
        MessageBrokerClient cchm2 = null;
        try {
            cchm = new MessageBrokerClient(id);
            cchm2 = new MessageBrokerClient(id+1);
            cchm.register("cpu");
            cchm2.register("storage");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        
        Log.logInfo(CmdPublisherClientChart.class.getSimpleName(), "Cliente com gráfico a iniciar.", Log.getLineNumber());
        //medir o tamanho do request enviado
        Request xx = new Request();
                for (int i = 0; i < cchm.getNumberOfEventsPerCachedRequest(); i++) {
                    Event ex = new Event();
                    ex.setContent(Integer.toString(i % 10) + Integer.toString(i % 5));
                    xx.addEvent(ex);
                }
        CountDownLatch sem = new CountDownLatch(1);
        Semaphore sem1 = new Semaphore(1);
        Semaphore sem2 = new Semaphore(1);
        Sender s1 = new Sender(cchm, values, "cpu", sem, totalTestTime*1000, "cpu", sem1);
        Sender s2 = new Sender(cchm2, values, "storage", sem, totalTestTime*1000, "storage", sem2);
        
        
        s1.start();
        s2.start();
        sem.countDown();
        
        Timer t = new Timer();
        t.scheduleAtFixedRate(new Drawer(lc,sem1,sem2, testRate ),0, (testRate*1000));
        
        Thread.sleep(totalTestTime*1000+1000);
        t.cancel();
        
    }
    
    
    
    
    static class Sender extends Thread{
        MessageBrokerClient mbc;
        int counter;
        CountDownLatch sem;
        long totalTime;
        String myTag;
        Semaphore sem1;
        String name;
        HashMap<String, Integer> value;
        String cname;
        Sender(MessageBrokerClient cchm, HashMap<String, Integer> v, String cn, CountDownLatch s, long t, String tag, Semaphore se){
            mbc = cchm;
            value = v;
            cname = cn;
            sem = s;
            totalTime = t;
            myTag = tag;
            sem1 = se;
        }
        
        
        public void run(){
            try {
                sem.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
            }
            Event e = new Event();
            Request resp = null;
            long termTime = System.currentTimeMillis()+totalTime;
            
            e.setContent(Long.toString(termTime % 10) + Long.toString(termTime % 5));
            
            while(System.currentTimeMillis() < termTime){
                try {
                    resp = mbc.publishWithCaching(e, myTag);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(resp != null)
                try {
                    sem1.acquire();
                       Integer t = value.get(cname);
                       t = t+10;
                       value.put(cname, t);
                    sem1.release();
                } catch (InterruptedException ex) {
                    Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
            }
            
            try {
                mbc.closeConnection();
            } catch (IOException ex) {
                Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
    }
    
   static class Drawer extends TimerTask{
        LineChart lc;
        Semaphore sem1, sem2;
        int total;
        int timesCalled;
        int intervalTime;
        public Drawer(LineChart l, Semaphore s1, Semaphore s2, int intervalTime){
            lc = l;
            sem1 = s1;
            sem2 = s2;
            total = 0;
            timesCalled = 1;
            this.intervalTime = intervalTime;
        }
        
        @Override
        public void run() {
            try {
                sem1.acquire();
                sem2.acquire();
                total = values.get("cpu").intValue()+values.get("storage").intValue();
                values.put("cpu", 0);
                values.put("storage", 0);
                sem1.release();
                sem2.release();
                lc.addValue(total, timesCalled*intervalTime, 15);
                lc.refresh();
            } catch (InterruptedException ex) {
                Logger.getLogger(CmdPublisherClientChart.class.getName()).log(Level.SEVERE, null, ex);
            }
            timesCalled++;
            total = 0;
        }   
    }    
}
