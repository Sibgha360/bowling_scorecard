package de.genios.helper;

import java.util.Arrays;

public class Util {

    public static boolean validateInput(String pins) {
        if (!Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10").contains(pins)) {
            return false;
        }
        return true;
    }
}
