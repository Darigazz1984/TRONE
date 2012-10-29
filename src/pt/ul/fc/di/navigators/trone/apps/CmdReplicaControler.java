/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    public static void main(String[] args) throws FileNotFoundException {
       int numberOfReplicas = Integer.parseInt(args[0]); // numero de replicas
       LinkedList<ReplicaControlPanel> repControlPanel = new LinkedList<ReplicaControlPanel>();
       HashMap<String, String> ipPort = new HashMap<String, String>();
       ReplicasControlWindow rcw;
       
       
       
       /*
        * Fazer para ler uma pasta com as especificações das replicas, isto é so teste
        */
       
       
      // BufferedReader in = new BufferedReader(new FileReader("monitor.props"));
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       /******************************/
       
       for(int i = 0;i<numberOfReplicas;i++){
           repControlPanel.add(new ReplicaControlPanel(""+i, ""+i));
       }
       
       
       rcw = new ReplicasControlWindow("TRONE - Replicas", numberOfReplicas);
       
       for(ReplicaControlPanel r: repControlPanel){
           rcw.addFrame(r);
       }
       
       rcw.show();
    }
}
