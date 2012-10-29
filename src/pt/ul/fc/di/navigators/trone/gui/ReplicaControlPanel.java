/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
   
   
   private String myIP;//ip da replica que deve ser controlada
   
   private JButton killButton;//matar a replica
   private JButton startButton;//iniciar a replica
   private JButton lieButton;//meter a replica a mentir
   private JButton slowButton;//atrasar a replica
   
   
   public ReplicaControlPanel(String number, String ip){
       //ip da replica a que se vai ligar
       myIP = ip;
       int repNumber = Integer.parseInt(number)+1;
       //representacao do estado da replica
       
       
       
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
   
   
   private void addListeners(){
        killButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
        public void actionPerformed(ActionEvent e) { killButton.setSelected(true); startButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(false); dot.setIcon(redDot); }
        });
        
        startButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
        public void actionPerformed(ActionEvent e) { startButton.setSelected(true); killButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(false); dot.setIcon(greenDot); }
        });
        
        lieButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
        public void actionPerformed(ActionEvent e) { startButton.setSelected(false); killButton.setSelected(false); lieButton.setSelected(true); slowButton.setSelected(false); dot.setIcon(yellowDot); }
        });
        
        slowButton.addActionListener(new ActionListener() {  // Note: inner class
      // This method is called when the Yes button is clicked.
        public void actionPerformed(ActionEvent e) { startButton.setSelected(false); killButton.setSelected(false); lieButton.setSelected(false); slowButton.setSelected(true); dot.setIcon(yellowDot); }
        });    
   }
   
}
