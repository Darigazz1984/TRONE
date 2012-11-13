/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author igor
 */
public class ClientControlWindow {
    private JFrame window;
    
    public ClientControlWindow(String title){
        window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setBackground(Color.white);
    }
    
    public void addPanel(JPanel p){
        window.add(p);
    }
    
    public void show(){
        window.pack();
        window.setVisible(true);
    }
    
}
