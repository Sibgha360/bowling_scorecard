package de.practice;

import java.util.HashMap;
import java.util.Map;

public class FirstUniqueLetter {
    public static void main(String[] args) {
        System.out.println(FirstUniqueLetter.getFirstUniqueIndex(""));
        System.out.println(FirstUniqueLetter.getFirstUniqueIndex("anna"));
        System.out.println(FirstUniqueLetter.getFirstUniqueIndex("togetther"));
    }

    public static int getFirstUniqueIndex(String text) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();

        for (int n = 0; n < text.length(); n++) {
            Character letter = text.charAt(n);

            Integer mapLetterCount = map.get(letter);

            if (mapLetterCount == null) {
                map.put(letter, 1);
            } else {
                map.put(letter, mapLetterCount + 1);
            }
        }

        for (int n = 0; n < text.length(); n++) {
            Character letter = text.charAt(n);

            if (map.get(letter) == 1) {
                return n + 1;
            }
        }

        return -1;
    }

}


