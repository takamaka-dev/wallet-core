/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.beans.FeeBean;
import java.math.BigInteger;
import java.util.Date;

/**
 * In the takamaka blockchain, values are represented in nanoTK, which means
 * that to transfer 1 TK (a red token or a green token) you need to multiply
 * this value by 10^9.
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class TkmTK {

    public static BigInteger unitTK(int unit) {
        return new BigInteger("" + unit).multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECIMAL);
    }

    public static BigInteger unitTK(BigInteger unit) {
        return unit.multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECIMAL);
    }

    public static BigInteger unitTK(String unit) {
        return new BigInteger(unit).multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECIMAL);
    }

    /**
     *
     * @param unit
     * @return
     */
    public static BigInteger deciTK(int unit) {
        return new BigInteger("" + unit).multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECI);
    }

    public static BigInteger deciTK(BigInteger unit) {
        return unit.multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECI);
    }

    public static BigInteger deciTK(String unit) {
        return new BigInteger(unit).multiply(KeyContexts.NUMBER_OF_ZEROS_SHIFT_DECI);
    }

    public static String getFeeViewTK(FeeBean fb) {
        if (fb != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ")
                    .append(tkView(fb.getCpu())).append(" | ")
                    .append(tkView(fb.getMemory())).append(" | ")
                    .append(tkView(fb.getDisk())).append(" ]");
            return sb.toString();
        } else {
            return null;
        }
        //return "";
    }

    private static String tkView(BigInteger val) {
        BigInteger toUnit = BigInteger.TEN.pow(DefaultInitParameters.NUMBER_OF_ZEROS);
        if (val == null) {
            return null;
        } else {
            BigInteger[] integerAndDecimal = val.divideAndRemainder(toUnit);
            //BigInteger decimalPart = val.d(toUnit);
            //String reminder = StringUtils.leftPad(integerAndDecimal[1].toString(), DefaultInitParameters.NUMBER_OF_ZEROS);
            String reminder = String.format("%09d", integerAndDecimal[1]);
            return integerAndDecimal[0].toString() + "," + reminder;
        }
    }

    /**
     * In the takamaka blockchain each transaction has a time limit beyond which
     * it cannot be included in a block. The transaction timer must be formatted
     * as unix timestamp (with milliseconds) and set to between "current time"
     * and "current time + 10 minutes". This enables the transaction to be
     * included in the first available block for the next 10 minutes. Creating
     * transactions with timer set to current time might work well for local
     * testing but is unlikely to function when interacting with remote servers.
     * To give a "sensible" inclusion window to the transaction and to account
     * for possible clock errors of the nodes, the device doing the sending, and
     * network transport times, it is recommended to place the transaction in
     * the middle of the interval. To do this simply add 60000L * 5 (5 minutes)
     * to the NOW(), current time.
     *
     * @return
     */
    public static final Date getTransactionTime() {
        return new Date((new Date()).getTime() + 60000L * 5);
    }

}
