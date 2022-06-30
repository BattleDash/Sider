package net.battledash.sider.utils;

public class HashUtil {

    public static int hash(String str) {
        int hash = 7;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * 31 + str.charAt(i);
        }
        return hash;
    }

}
