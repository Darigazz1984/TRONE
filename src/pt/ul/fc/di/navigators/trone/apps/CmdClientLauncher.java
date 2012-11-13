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
import pt.ul.fc.di.navigators.trone.gui.ClientControlPanel;
import pt.ul.fc.di.navigators.trone.gui.ClientControlWindow;

/**
 *
 * @author igor
 */
public class CmdClientLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        /***LER FICHEIRO COM IP'S E LOCALIZAÇÃO DO SCRIPT A EXECUTAR ***/
        FileInputStream fstream = new FileInputStream("clientLauncher.props");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine ="", pass="", path="", pubIP="", subIP="", user="";
        
        while ((strLine = br.readLine()) != null)   {
             if(!strLine.startsWith("#") && !strLine.isEmpty()){
                 String line[] = strLine.split("=");
                 if(line[0].equals("pubIP")){
                     pubIP = line[1];
                 }
                 
                 if(line[0].equals("subIP")){
                     subIP = line[1];
                 }
                 
                 if(line[0].equals("pass")){
                     pass = line[1];
                 }
                 
                 if(line[0].equals("path")){
                     path = line[1];
                 }
                 
                 if(line[0].equals("user")){
                     user = line[1];
                 }
             }
        }
        
        ClientControlWindow ccw = new ClientControlWindow("Client launcher");
        ClientControlPanel ccp = new ClientControlPanel(pubIP,subIP,pass,path, user);
        ccw.addPanel(ccp.getPanel());
        ccw.show();
    }
}
