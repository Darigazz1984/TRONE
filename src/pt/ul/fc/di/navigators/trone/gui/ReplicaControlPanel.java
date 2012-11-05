/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pt.ul.fc.di.navigators.trone.data.Command;
import pt.ul.fc.di.navigators.trone.utils.Define;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class ReplicaControlPanel {
   private JPanel panel; //container para tudo
   
   private JLabel label; //image do servidor
   private JLabel label1; //numero da reploca
   
   private JLabel dot; //imagem que vai conter o estado da replica
   //icones representativos do estado da replica
   private Icon greenDot; //tudo ok
   private Icon redDot; //em baixo
   private Icon yellowDot; //sobre ataque ou lenta
   
   
   
   
   private JButton killButton;//matar a replica
   private JButton startButton;//iniciar a replica
   private JButton lieButton;//meter a replica a mentir
   private JButton slowButton;//atrasar a replica
   
   
   
   //Variaveis para se ligar ao servidor
   private String myIP;
   private int port;
   private int repNumber;
   private Socket cSocket;
   
   Semaphore sem;
   
   boolean lie;
   boolean slow;
   
   
   public ReplicaControlPanel(String number, String ip, int p){
       //ip da replica a que se vai ligar
       myIP = ip;
       port = p;
       repNumber = Integer.parseInt(number)+1;
       //representacao do estado da replica
       lie = false;
       slow = false;
       
       
       
       //estados da replica
       greenDot = new ImageIcon("img/greendot.jpg");
       redDot = new ImageIcon("img/reddot.jpg");
       yellowDot = new ImageIcon("img/yellowdot.jpg");
       
      
       
       //nome/numero da replica
       label = new JLabel();
       label.setIcon(new ImageIcon("img/server.png"));
       label.setVisible(true);
       
       label1 = new JLabel(""+repNumber);
       label1.setFont(new Font("Serif", Font.BOLD, 25));
       
       
       
       //painel em que tudo encaixa
       panel = new JPanel();
       panel.setLayout(new FlowLayout());
       panel.setVisible(true);
       panel.setBackground(Color.white);
       
       //Iniciacao Butoes
       
       killButton = new JButton();
       killButton.setBackground(Color.white);
       //killButton.setBorder(null);
       startButton = new JButton();
       startButton.setBackground(Color.white);
       //startButton.setBorder(null);
       lieButton = new JButton();
       lieButton.setBackground(Color.white);
       //lieButton.setBorder(null);
       slowButton = new JButton();
       slowButton.setBackground(Color.white);
       //slowButton.setBorder(null);
       
       //Imagens dos butoes
       killButton.setIcon(new ImageIcon("img/no.png"));
       startButton.setIcon(new ImageIcon("img/ok.png"));
       lieButton.setIcon(new ImageIcon("img/lie.jpg"));
       slowButton.setIcon(new ImageIcon("img/slow.jpg"));
       
       dot = new JLabel(redDot);
       dot.setIcon(redDot);
       dot.setVisible(true);
       
       
       addListeners();
       /*sem = new Semaphore(1);
       Timer t = new Timer();
       Alive ping = new Alive(myIP, port,sem);
       t.schedule(ping, 0, 2000);*/
       setTimer();
       
   }
   
   public JPanel getPanel(){
       
       panel.add(label);
       panel.add(label1);
       panel.add(startButton);
       panel.add(killButton);
       panel.add(lieButton);
       panel.add(slowButton);
       panel.add(dot);
       return panel;
   }
   
   
   public void setTimer(){
       sem = new Semaphore(1);
       Timer t = new Timer();
       Alive ping = new Alive(myIP, port,sem);
       t.schedule(ping, 1000, 2000);
   }
   
   private void addListeners(){
        killButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
                public void actionPerformed(ActionEvent e) { 
                    Command c = new Command();
                    c.setCommand(Define.ReplicaCommand.KILL);
                    sendCommand(c);
                    killButton.setSelected(true); startButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(false); dot.setIcon(redDot); }
        });
        
        startButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
        public void actionPerformed(ActionEvent e) { 
                
                
                
                
                //COPIAR CODIGO
                String command = "./remoteExec.sh weq312@@ "+myIP+" /home/igor/Dropbox/ReplicaExecCode/CopyScript.sh";
                Runtime rt = Runtime.getRuntime();
                Process pr;
                
                
                try {
                    pr = rt.exec(command);
                } catch (IOException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                }
                //INICIAR REPLICA
               
                command = "./remoteExec.sh weq312@@ "+myIP+" /home/igor/Execution/bin/run-replicas-remote.sh 1 "+(repNumber-1)+" xterm";
                try {
                    pr = rt.exec(command);
                    
                } catch (IOException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                }
                startButton.setSelected(true); killButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(false); dot.setIcon(greenDot); }
        });
        
        lieButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
                public void actionPerformed(ActionEvent e) { 
                    Command c = new Command();
                    c.setCommand(Define.ReplicaCommand.LIE);
                    sendCommand(c);
                    if(lie){
                        lie =false;
                        dot.setIcon(greenDot);

                    }else{
                        dot.setIcon(yellowDot);
                        lie = true;
                    }

                    startButton.setSelected(false); killButton.setSelected(false); lieButton.setSelected(true); slowButton.setSelected(false);}
        });
        
        slowButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
                public void actionPerformed(ActionEvent e) { 
                    Command c = new Command();
                    c.setCommand(Define.ReplicaCommand.SLOW);
                    sendCommand(c);

                    if(slow){
                        slow =false;
                        dot.setIcon(greenDot);
                    }else{

                        slow = true;
                        dot.setIcon(yellowDot);
                    }
                    startButton.setSelected(false); killButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(true);  }
         });    
   }
   
   //Este metodo e reponsavel por enviar os comandos para a replica
   
   private void sendCommand(Command c){
       
       Socket cSocket = null;
       ObjectOutputStream out = null;
        try {
            //sem.acquire();
            cSocket = new Socket(myIP, port);
        } catch (UnknownHostException ex) {
             Log.logError(this.getClass().getCanonicalName(), "Erro ao criar socket", Log.getLineNumber());
        } catch (IOException ex) {
             Log.logError(this.getClass().getCanonicalName(), "Erro ao criar socket", Log.getLineNumber());
        } /*catch (InterruptedException ex) {
             Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer acquire do sem", Log.getLineNumber());
        }*/
        
        try {
            out = new ObjectOutputStream(cSocket.getOutputStream());
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Erro ao criar stream", Log.getLineNumber());
        }
        
        try {
            out.writeObject(c);
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Erro ao enviar comando", Log.getLineNumber());
        }
        
        try {
            out.close();
            cSocket.close();
            //sem.release();
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Erro ao fechar socket e stream", Log.getLineNumber());
        }
        
   }
   
   
   /*
    * Esta classe e reponsavel por fazer ping ao servidor
    */
   
   class Alive extends TimerTask{
       String ip;
       int port;
       Semaphore sem;
       
       

        private Alive(String i, int p, Semaphore s) {
            ip = i;
            port = p;
            sem = s;
        }
        
        @Override
        public void run() {
            Socket cSocket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            Command cOut = null, cIn = null;
            boolean proceed = true;
            //criar socket
            try {
                sem.acquire();
                cSocket = new Socket(ip, port);
            } catch (UnknownHostException ex) {
                Log.logError(this.getClass().getCanonicalName(), "Erro ao criar socket", Log.getLineNumber());
                dot.setIcon(redDot);
                proceed = false;
                setTimer();
            } catch (IOException ex) {
                Log.logError(this.getClass().getCanonicalName(), "Erro ao criar socket", Log.getLineNumber());
                dot.setIcon(redDot);
                proceed = false;
                setTimer();
            } catch (InterruptedException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer acquire do semaforo", Log.getLineNumber());
            }
            //criar streams
            if(proceed){
                try{
                    out = new ObjectOutputStream(cSocket.getOutputStream());
                    in = new ObjectInputStream(cSocket.getInputStream());
                } catch (IOException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Erro ao criar streams", Log.getLineNumber());
                    dot.setIcon(redDot);
                    proceed = false;
                    setTimer();
                }
            }
           
           //enviar e receber resposta
           if(out != null && in != null && proceed){
               cOut = new Command();
               cOut.setCommand(Define.ReplicaCommand.PING);
               //Enviar 
               try {
                    out.writeObject(cOut);
               } catch (IOException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Erro ao enviar PING", Log.getLineNumber());
                    dot.setIcon(redDot);
                    setTimer();
               }
               
               //Receber
               try {
                   while (cIn == null && !(cIn instanceof Command)) {
                               cIn = (Command) in.readObject();
                   }
                   //cIn = (Command)in.readObject(); 
               } catch (IOException ex) {
                   Log.logError(this.getClass().getCanonicalName(), "Erro ao receber PONG", Log.getLineNumber());
                   dot.setIcon(redDot);
                   setTimer();
               } catch (ClassNotFoundException ex) {
                   Log.logError(this.getClass().getCanonicalName(), "Erro ao receber PONG", Log.getLineNumber());
                   dot.setIcon(redDot);
                   setTimer();
               }
               
               if(cIn.getCommand().equals(Define.ReplicaCommand.PONG)){
                   if(lie || slow){
                       dot.setIcon(yellowDot);
                   }else
                       dot.setIcon(greenDot);
               }
                
                
           }//fim do if 
           //Fechar tudo
           try {
               out.close();
               in.close();
               cSocket.close();
               sem.release();
           } catch (IOException ex) {
                Log.logError(this.getClass().getCanonicalName(), "Erro ao fechar socket e streams", Log.getLineNumber());
                setTimer();
           }
          
        }//fim run
       
   }//fim classe
}
