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
    long sleepTime;
    
    public Command(){
        rc = ReplicaCommand.KILL;
        sleepTime = 50;
    }
    
    public void setCommand(ReplicaCommand r){
        rc = r;
    }
    
    public ReplicaCommand getCommand(){
        return rc;
    }
    
    public void setSleeplTime(long st){
        sleepTime = st;
    }
    
    public long getSleepTime(){
        return sleepTime;
    }
    
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.rc = (Define.ReplicaCommand) objectInput.readObject();
        this.sleepTime = objectInput.readLong();
    }
    
    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(rc);
        objectOutput.writeLong(sleepTime);
    }
}
