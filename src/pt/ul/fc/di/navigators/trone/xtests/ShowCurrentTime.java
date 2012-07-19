/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;

/**
 *
 * @author kreutz
 */
public class ShowCurrentTime {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        for (int i = 0; i < 1; i++) {
            CurrentTime.showTimeInMilliseconds();
            CurrentTime.showTimeInNanoseconds();
            CurrentTime.showTimeInNanosecondsWithoutEndingZeros();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ShowCurrentTime.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
