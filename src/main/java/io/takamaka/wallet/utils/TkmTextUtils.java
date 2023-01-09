/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author giovanni
 */
public class TkmTextUtils {

    /**
     *
     * @param text
     * @return true if text is null or blank
     */
    public static final boolean isNullOrBlank(String text) {
        return (text == null
                || ("".equals(text.trim()))
                || (StringUtils.isBlank(text))
                || (text.isBlank())
                || (text.isEmpty()));
    }
}
