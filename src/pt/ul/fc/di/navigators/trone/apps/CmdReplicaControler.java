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
import java.util.HashMap;
import java.util.LinkedList;
import pt.ul.fc.di.navigators.trone.gui.DisplayPanel;
import pt.ul.fc.di.navigators.trone.gui.ReplicaControlPanel;
import pt.ul.fc.di.navigators.trone.gui.ReplicasControlWindow;

/**
 *
 * @author igor
 */
public class CmdReplicaControler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
       
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
       while ((strLine = br.readLine()) != null)   {
           //Queremos saltar os comentarios
            if(!strLine.startsWith("#") && !strLine.isEmpty()){
                repControlPanel.add(new ReplicaControlPanel(""+number, strLine.split("=")[0],Integer.parseInt(strLine.split("=")[1])));
                number++;
            }
                
       }
       
       
       rcw = new ReplicasControlWindow("TRONE - Replicas", number);
       
       for(ReplicaControlPanel r: repControlPanel){
           rcw.addFrame(r);
       }
       
       rcw.show();
       
       DisplayPanel dp = new DisplayPanel("Averege time per request");
       dp.changeText("PRIMEIRO TESTE");
       dp.build();
    }
}
