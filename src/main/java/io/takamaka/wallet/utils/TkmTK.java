/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.beans.FeeBean;
import java.math.BigInteger;

/**
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

}
