/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;
    
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class ClientControlPanel {
    private JPanel panel, panel2; //panel 1 contem tudo, panel 2 contem o tempo e o num de clientes
    private JButton start, stop;
    private JComboBox numClients;
    private String clientOptions[] = {"1","2","3","4"};
    private JTextField duration;
    private JLabel label1, label2;
    
    private String pubIP, subIP, pass, path, user;
    
    public ClientControlPanel(String p, String s, String pa, String pth, String us){
        pubIP = p;
        subIP = s;
        pass = pa;
        path = pth;
        user = us;
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setVisible(true);
        panel.setBackground(Color.white);
        
        
        panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.add(Box.createVerticalGlue());
        panel2.setVisible(true);
        
        start = new JButton();
        start.setIcon(new ImageIcon("img/ok.png"));
        
        stop = new JButton();
        stop.setIcon(new ImageIcon("img/no.png"));
        
        numClients = new JComboBox(clientOptions);
        duration = new JTextField("0", 10);
        
        label1 = new JLabel();
        label2 = new JLabel();
        label1.setText("CLIENTS");
        label2.setText("DURATION");
        addListeners();
        
    }
    
    public JPanel getPanel(){
        panel2.add(label1);
        panel2.add(numClients);
        panel2.add(label2);
        panel2.add(duration);
        panel.add(panel2);
        panel.add(start);
        panel.add(stop);
        return panel;
    }
    
    private void addListeners(){
        start.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
                public void actionPerformed(ActionEvent e) {
                    String command = "./remoteExecClients.sh "+pass+" "+subIP+" "+path+"CopyScript.sh "+user+" "+duration.getText() +" 1 0 "+numClients.getSelectedItem();
                    System.out.println("COPY SUB:"+command);
                    Runtime rt = Runtime.getRuntime();
                    Process pr;

                    try {
                        pr = rt.exec(command);

                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer sleep entre comandos", Log.getLineNumber());
                    }
                    
                    command = "./remoteExecClients.sh "+pass+" "+pubIP+" "+path+"CopyScript.sh "+user+" "+duration.getText() +" 1 0 "+numClients.getSelectedItem();
                    System.out.println("COPY PUB:"+command);
                    try {
                        pr = rt.exec(command);

                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer sleep entre comandos", Log.getLineNumber());
                    }
                    
                    
                    command = "./remoteExecClients.sh "+pass+" "+subIP+" "+path+"ExecScriptSub.sh "+user+" "+duration.getText() +" 1 4 "+numClients.getSelectedItem();
                    System.out.println("RUN SUB:"+command);
                    try {
                        pr = rt.exec(command);

                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer sleep entre comandos", Log.getLineNumber());
                    }
                    
                    
                    command = "./remoteExecClients.sh "+pass+" "+pubIP+" "+path+"ExecScriptPub.sh "+user+" "+duration.getText() +" 1 0 "+numClients.getSelectedItem();
                    System.out.println("RUN PUB:"+command);
                    try {
                        pr = rt.exec(command);

                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                    }
                }  
        });
        stop.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
            @Override
                public void actionPerformed(ActionEvent e) { 
                    String command = "./remoteExecClients.sh "+pass+" "+subIP+" "+path+"bin/kill-all-java-proc.sh "+user+" "+duration.getText() +" 1 0 "+numClients.getSelectedItem();
                    //System.out.println("COPY SUB:"+command);
                    Runtime rt = Runtime.getRuntime();
                    Process pr;

                    try {
                        pr = rt.exec(command);

                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao executar command", Log.getLineNumber());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fazer sleep entre comandos", Log.getLineNumber());
                    }
                }
        });
    }
}

