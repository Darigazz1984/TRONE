/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.util.*;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;
import pt.ul.fc.di.navigators.trone.utils.RandomNumbers;
import sun.misc.Queue;

/**
 *
 * @author kreutz
 */
public class ListsAndQueueTests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        ArrayList al = new ArrayList<Event>();
        LinkedList ll = new LinkedList<Event>();
        Queue qu = new Queue();
        List syncList = Collections.synchronizedList(new ArrayList());
        long addRandomNumberOfEvents;
        long removeRandomNumberOfEvents;
        long doItForRandomNumberOfTimes;
        long timeForArrayList = 0;
        long timeForLinkedList = 0;
        long timeForQueue = 0;
        long timeForSyncList = 0;
        
        addRandomNumberOfEvents = RandomNumbers.getUniqueLongPositiveRandomNumber();
        if (addRandomNumberOfEvents > 1000000) {
            addRandomNumberOfEvents = (addRandomNumberOfEvents % 1000000);
        }
        
        removeRandomNumberOfEvents = RandomNumbers.getUniqueLongPositiveRandomNumber();
        if (removeRandomNumberOfEvents > addRandomNumberOfEvents) {
            removeRandomNumberOfEvents = (removeRandomNumberOfEvents % (addRandomNumberOfEvents / 10));
        }
        
        doItForRandomNumberOfTimes = RandomNumbers.getUniqueLongPositiveRandomNumber();
        doItForRandomNumberOfTimes = (doItForRandomNumberOfTimes % 40) + 30;
        
        System.out.println("addRandomNumberOfEvents: " + addRandomNumberOfEvents);
        System.out.println("removeRandomNumberOfEvents: " + removeRandomNumberOfEvents);
        System.out.println("doItForRandomNumberOfTimes: " + doItForRandomNumberOfTimes);
        
        for (long i = 0; i < doItForRandomNumberOfTimes; i++) {
            
            long t = CurrentTime.getTimeInMilliseconds(); 
            for (long j = 0; j < addRandomNumberOfEvents; j++) {
                al.add(new Event());
            }
            for (long j = 0; j < removeRandomNumberOfEvents && al.size() > 0; j++) {
                int pos = (int)(RandomNumbers.getUniqueLongPositiveRandomNumber() % al.size());
                al.remove(pos);
            }
            for (int x = (al.size() - 1); x >= 0; x--) {
                al.remove(x);
            }
            t = CurrentTime.getTimeInMilliseconds() - t;
            timeForArrayList += t;
            
            t = CurrentTime.getTimeInMilliseconds(); 
            for (long j = 0; j < addRandomNumberOfEvents; j++) {
                ll.add(new Event());
            }
            for (long j = 0; j < removeRandomNumberOfEvents && ll.size() > 0; j++) {
                int pos = (int)(RandomNumbers.getUniqueLongPositiveRandomNumber() % ll.size());
                ll.remove(pos);
            }
            for (int x = (ll.size() - 1); x >= 0 && ll.size() > 0; x--) {
                ll.remove(x);
            }
            t = CurrentTime.getTimeInMilliseconds() - t;
            timeForLinkedList += t;
            
            t = CurrentTime.getTimeInMilliseconds(); 
            for (long j = 0; j < addRandomNumberOfEvents; j++) {
                qu.enqueue(new Event());
            }
            long y=0;
            Enumeration e = qu.elements();
            while (e.hasMoreElements()) {
                y++;
                e.nextElement();
            }
            y--;
            for (long j = 0; j < removeRandomNumberOfEvents && y > 0; j++) {
                long pos = (RandomNumbers.getUniqueLongPositiveRandomNumber() % y);
                qu.dequeue(pos);
                y--;
            }
            for (y = y; y >= 0; y--) {
                qu.dequeue(y);
            }
            t = CurrentTime.getTimeInMilliseconds() - t;
            timeForQueue += t;
            
            t = CurrentTime.getTimeInMilliseconds(); 
            for (long j = 0; j < addRandomNumberOfEvents; j++) {
                syncList.add(new Event());
            }
            for (long j = 0; j < removeRandomNumberOfEvents && syncList.size() > 0; j++) {
                int pos = (int)(RandomNumbers.getUniqueLongPositiveRandomNumber() % syncList.size());
                syncList.remove(pos);
            }
            for (int x = (syncList.size() - 1); x >= 0; x--) {
                syncList.remove(x);
            }
            t = CurrentTime.getTimeInMilliseconds() - t;
            timeForSyncList += t;
        }
        
        System.out.println("ARRAY LIST TIME: " + timeForArrayList);
        System.out.println("LINKED LIST TIME: " + timeForLinkedList);
        System.out.println("QUEUE TIME: " + timeForQueue);
        System.out.println("SYNC LIST TIME: " + timeForSyncList);
        
    }
}
