package org.reactnative.camera.utils;


public class ObjectUtils {

    /*
    * Replacement for Objects.equals that is only available after Android API 19
    */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null) return false;
        return o1.equals(o2);
    }

}
