package pt.ul.fc.di.navigators.trone.apps;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;

/**
 *
 * @author kreutz
 */
public class ConfigsReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList listOfFiles = new ArrayList<String>();
        listOfFiles.add("netConfig.props");
        listOfFiles.add("serverConfig.props");
        listOfFiles.add("clientConfig.props");

        try {
            Iterator it = listOfFiles.iterator();
            while (it.hasNext()) {
                String fileName = (String) it.next();

                ConfigHandler confHandler = new ConfigHandler(fileName);

                confHandler.readConfig();

                System.out.println("\nThe content of file " + fileName + " is: ");
                System.out.println("First element key: " + confHandler.getFirstElement() + " Value: " + confHandler.getFirstElementValue());

                confHandler.printElements();

            }
        } catch (Exception ex) {
            Logger.getLogger(ConfigsReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
