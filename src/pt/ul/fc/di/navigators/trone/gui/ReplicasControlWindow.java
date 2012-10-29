/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 *
 * @author igor
 */
public class ReplicasControlWindow {
    private JFrame window;
    private JLabel imageLabel;
    private JScrollPane jsp;
    
    
    public ReplicasControlWindow(String name, int numberOfReplicas){
        window = new JFrame(name);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setLayout(new GridLayout(numberOfReplicas+1, 0));
        window.setSize(250*(numberOfReplicas+1), 200*numberOfReplicas);
        window.setBackground(Color.white);
        imageLabel = new JLabel();
        imageLabel.setIcon(new ImageIcon("img/trone_logo.jpg"));
        imageLabel.setVisible(true);
        
        jsp = new JScrollPane(imageLabel);
        jsp.setVisible(true);
        
        window.getContentPane().add(jsp);
        
    }
    
    
    public void addFrame(ReplicaControlPanel cp){
        window.add(cp.getPanel());
    }
    
    public void show(){
        window.pack();
        window.setVisible(true);
    }
}
