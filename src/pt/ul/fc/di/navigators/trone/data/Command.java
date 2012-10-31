/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import pt.ul.fc.di.navigators.trone.utils.Define;
import pt.ul.fc.di.navigators.trone.utils.Define.ReplicaCommand;

/**
 *
 * @author igorantunes
 * Esta classe representa os comandos enviados do controlador das replicas para a replica
 */
public class Command implements Serializable{
    ReplicaCommand rc;
    
    
    public Command(){
        rc = ReplicaCommand.KILL;
    }
    
    public void setCommand(ReplicaCommand r){
        rc = r;
    }
    
    public ReplicaCommand getCommand(){
        return rc;
    }
    
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.rc = (Define.ReplicaCommand) objectInput.readObject();
    }
    
    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(rc);
    }
}
