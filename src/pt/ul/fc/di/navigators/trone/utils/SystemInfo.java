/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

//import com.sun.servicetag.SystemEnvironment;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author kreutz
 */
public class SystemInfo {

    public SystemInfo() {
    }

    public static String getProcessNumber(final String fallback) {

        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');

        if (index < 1) {
            return fallback;
        }

        return Long.toString(Long.parseLong(jvmName.substring(0, index)));
    }

    public static String getIpAddress() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();

        return addr.getHostAddress();
    }

    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static String getHardwareInfo() {
        StringBuilder hisb = new StringBuilder();
        
        /*
        SystemEnvironment se = SystemEnvironment.getSystemEnvironment();

        hisb.append(se.getCpuManufacturer());
        hisb.append(se.getHostId());
        hisb.append(se.getHostname());
        hisb.append(se.getOsArchitecture());
        hisb.append(se.getOsName());
        hisb.append(se.getOsVersion());
        hisb.append(se.getSerialNumber());
        hisb.append(se.getSystemModel());
        hisb.append(se.getSystemManufacturer());
        */
        hisb.append(getAvailableProcessors());
        hisb.append(getTotalMemory());
        
        return hisb.toString();
    }

    public static String getUniqueId() throws UnknownHostException {
        StringBuilder sisb = new StringBuilder();
        
        sisb.append(getIpAddress());
        sisb.append(getProcessNumber("<PID>"));
        sisb.append(Integer.toString(getAvailableProcessors()));
        sisb.append(Long.toString(getMaxMemory()));
        //sisb.append(getHardwareInfo());
        
        return sisb.toString();
    }
}
