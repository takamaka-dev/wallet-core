/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Iris Dimni iris.dimni@takamaka.io
 */
@Slf4j
public class StringUtils {

    /**
     * use an equivalent method from
     * org.apache.commons.lang3.StringUtils
     *
     * @param input
     * @return
     * @deprecated 
     */
    public static Boolean isNullOrWhiteSpace(String input) {
        Boolean result = false;

        if (TkmTextUtils.isNullOrBlank(input)) {
            result = true;
        }

        return result;
    }

    /**
     * returns a string of coma separated values of the list
     *
     * @param input
     * @return
     */
    public static String printList(ArrayList<?> input) {
        String result = "";

        for (int i = 0; i < input.size(); i++) {
            if ((input.size() == 1) || (i == input.size() - 1)) {
                result += (input.get(i).toString());
            } else {
                result += (input.get(i).toString() + FixedParameters.STATE_SLOT_SEPARATOR);
            }
        }

        return result;
    }

    /**
     * Deprecated the cast to the defined type doesn't change result objects
     * type, all elements are saved as String
     *
     * @param <T>
     * @param input
     * @param T
     * @param separator
     * @return
     */
    public static <T> ArrayList<T> stringToList(String input, Class T, String separator) {
        ArrayList<T> result = new ArrayList<>();
        String[] temp = input.split(separator);
        for (int i = 0; i < temp.length; i++) {
            result.add((T) temp[i]);
        }
        return result;
    }

    /**
     *
     * @param text
     * @return
     */
    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            log.error("Error parsing text", e);
            return null;
        }
    }

    /**
     * returns the elements of an array as string separated by space
     *
     * @param input
     * @return
     */
    public static String arrayToString(String[] input) {
        String result = null;

        for (int i = 0; i < input.length; i++) {

            result += input[i] + " ";
        }

        return result;
    }

    /**
     * Returns the transaction type from a given string if it's an exact match
     *
     * @param input
     * @return
     */
    public static KeyContexts.TransactionType getTypeFromString(String input) {
        KeyContexts.TransactionType type = KeyContexts.TransactionType.UNDEFINED;
        for (KeyContexts.TransactionType t : KeyContexts.TransactionType.values()) {
            if (t.name().equals(input)) {
                type = t;
            }
        }

        return type;
    }
}
