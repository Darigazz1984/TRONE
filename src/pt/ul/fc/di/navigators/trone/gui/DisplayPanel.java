/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author igor
 */
public class DisplayPanel {
    JFrame frame;
    JLabel label;
    
    public DisplayPanel(String title){
        frame = new JFrame(title);
        label = new JLabel("",SwingConstants.CENTER);
    }
    
    
    public void build(){
        frame.add(label);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setSize(300, 100);
        frame.setLocationRelativeTo(null);
        frame.requestFocus();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        label.setVisible(true);
    }
    
    public void changeText(String text){
        label.setText(text);
        frame.repaint();
    }
}
