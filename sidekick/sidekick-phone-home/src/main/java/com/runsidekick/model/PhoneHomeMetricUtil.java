package com.runsidekick.model;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author yasin.kalafat
 */
public final class PhoneHomeMetricUtil {

    private static final String NOT_AVAILABLE = "N/A";

    private PhoneHomeMetricUtil() {

    }

    public static String generateUUIDFromString(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes()).toString();
    }

    public static String getMacAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            byte[] hardwareAddress = ni.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            String macAddress = String.join("-", hexadecimal);
            return generateUUIDFromString(macAddress);
        } catch (Exception e) {

        }
        return NOT_AVAILABLE;
    }

    public static void setOsInfo(PhoneHomeMetric phoneHomeMetric) {
        try {
            OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
            phoneHomeMetric.setOsName(osMxBean.getName());
            phoneHomeMetric.setOsArch(osMxBean.getArch());
            phoneHomeMetric.setOsVersion(osMxBean.getVersion());
        } catch (SecurityException e) {
            phoneHomeMetric.setOsName(NOT_AVAILABLE);
            phoneHomeMetric.setOsArch(NOT_AVAILABLE);
            phoneHomeMetric.setOsVersion(NOT_AVAILABLE);
        }
    }

    public static void setHostName(PhoneHomeMetric phoneHomeMetric) {
        try {
            phoneHomeMetric.setHostName(generateUUIDFromString(InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException e) {
            phoneHomeMetric.setHostName(NOT_AVAILABLE);
        }
    }
}
