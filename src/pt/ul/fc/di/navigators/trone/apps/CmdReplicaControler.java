/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
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
       int numberOfReplicas = Integer.parseInt(args[0]); // numero de replicas
       LinkedList<ReplicaControlPanel> repControlPanel = new LinkedList<ReplicaControlPanel>();
       HashMap<String, String> ipPort = new HashMap<String, String>();
       ReplicasControlWindow rcw;
       
       
       
       /*
        * Fazer para ler uma pasta com as especificações das replicas, isto é so teste
        */
       
       
      // BufferedReader in = new BufferedReader(new FileReader("monitor.props"));
       
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
       
       /******************************/
      /* 
       for(int i = 0;i<numberOfReplicas;i++){
           repControlPanel.add(new ReplicaControlPanel(""+i, ""+i,10));
       }*/
       
       
       rcw = new ReplicasControlWindow("TRONE - Replicas", numberOfReplicas);
       
       for(ReplicaControlPanel r: repControlPanel){
           rcw.addFrame(r);
       }
       
       rcw.show();
    }
}
