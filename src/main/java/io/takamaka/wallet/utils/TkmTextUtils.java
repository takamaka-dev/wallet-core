/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.takamaka.wallet.beans.EncKeyBean;
import io.takamaka.wallet.beans.InternalBlockBean;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.beans.PublicKeyBean;
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashCompositionException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.InclusionHashCreationException;
import io.takamaka.wallet.exceptions.NullInternalTransactionBeanException;
import org.apache.commons.lang3.RandomStringUtils;
//import com.fasterxml.jackson.databind.ObjectWriter;
//import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class TkmTextUtils {

    private static final SimpleModule getSM() {
        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(StorageReference.class, new ISStorageReferenceSerializer());
//        simpleModule.addSerializer(TransactionReference.class, new ISTransactionReferenceSerializer());
//        simpleModule.addSerializer(StorageValue.class, new ISStorageValueSerializer());
//        simpleModule.addDeserializer(TransactionReference.class, new ISTransactionRefernceDeserializer());
//        simpleModule.addDeserializer(StorageReference.class, new ISStorageRefernceDeserializer());
//        simpleModule.addDeserializer(StorageValue.class, new ISStorageValueDeserializer());
//        simpleModule.addSerializer(StorageType.class, new ISStorageTypeSerializer());
//        simpleModule.addDeserializer(StorageType.class, new ISStorageTypeDeserializer());
//        simpleModule.addSerializer(CodeSignature.class, new ISCodeSignatureSerializer());
//        simpleModule.addDeserializer(CodeSignature.class, new ISCodeSignatureDeserializer());
//        simpleModule.addSerializer(TransactionResponse.class, new ISTransactionResponseSerializer());
//        simpleModule.addDeserializer(TransactionResponse.class, new ISTransactionResponseDeserializer());
//        simpleModule.addSerializer(TransactionRequest.class, new ISTransactionRequestSerializer());
//        simpleModule.addDeserializer(TransactionRequest.class, new ISTransactionRequestDeserializer());
//        simpleModule.addSerializer(Update.class, new ISUpdateSerializer());
//        simpleModule.addDeserializer(Update.class, new ISUpdateDeserializer());
//        simpleModule.addSerializer(FieldSignature.class, new ISFieldSignatureSerializer());
//        simpleModule.addDeserializer(FieldSignature.class, new ISFieldSignatureDeserializer());
//
//        simpleModule.addSerializer(WordsBean.class, new ISWordsBeanSerializer());
//        simpleModule.addDeserializer(WordsBean.class, new ISWordsBeanDeserializer());
//
//        simpleModule.addSerializer(EncWordsBean.class, new ISEncWordsBeanSerializer());
//        simpleModule.addDeserializer(EncWordsBean.class, new ISEncWordsBeanDeserializer());
//        //
//        simpleModule.addDeserializer(ElasticObjectBean.class, new ISElasticObjectDeserializer());
        return simpleModule;
    }

    public static final TypeReference<ArrayList<TransactionBean>> type_ArrayList_TransactionBean = new TypeReference<ArrayList<TransactionBean>>() {
    };

    public static final ObjectMapper getJacksonMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(getSM());
        return mapper;
    }

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

    public static final String toJson(KeyBean key) {
        try {
            return getJacksonMapper().writeValueAsString(key);
        } catch (JsonProcessingException ex) {
            log.error("KeyBean serialization error", ex);
            return null;
        }
    }

    public static final KeyBean keyBeanFromJson(String keyBeanJson) {
        try {
            return getJacksonMapper().readValue(keyBeanJson, KeyBean.class);
        } catch (JsonProcessingException ex) {
            log.error("KeyBean deserialization error", ex);
            return null;
        }
    }

    public static final String toJson(EncKeyBean key) {
        try {
            return getJacksonMapper().writeValueAsString(key);
        } catch (JsonProcessingException ex) {
            log.error("EncKeyBean serialization error", ex);
            return null;
        }
    }

    public static final EncKeyBean enckeyBeanFromJson(String encKeyBeanJson) {
        try {
            return getJacksonMapper().readValue(encKeyBeanJson, EncKeyBean.class);
        } catch (JsonProcessingException ex) {
            log.error("EncKeyBean deserialization error", ex);
            return null;
        }
    }

    public static final String toJson(PublicKeyBean key) {
        try {
            return getJacksonMapper().writeValueAsString(key);
        } catch (JsonProcessingException ex) {
            log.error("PublicKeyBean serialization error", ex);
            return null;
        }
    }

    public static final PublicKeyBean publicKeyBeanFromJson(String publickeyBeanJson) {
        try {
            return getJacksonMapper().readValue(publickeyBeanJson, PublicKeyBean.class);
        } catch (JsonProcessingException ex) {
            log.error("PublicKeyBean deserialization error", ex);
            return null;
        }
    }

    public static final String toJson(InternalTransactionBean itb) {
        try {
            return getJacksonMapper().writeValueAsString(itb);
        } catch (JsonProcessingException ex) {
            log.error("InternalTransactionBean serialization error", ex);
            return null;
        }
    }

    public static final InternalTransactionBean internalTransactionBeanFromJson(String jsonString) {
        try {
            return getJacksonMapper().readValue(jsonString, InternalTransactionBean.class);
        } catch (JsonProcessingException ex) {
            log.error("InternalTransactionBean deserialization error", ex);
            return null;
        }
    }

    public static final String toJson(InternalBlockBean itb) {
        try {
            return getJacksonMapper().writeValueAsString(itb);
        } catch (JsonProcessingException ex) {
            log.error("InternalTransactionBean serialization error", ex);
            return null;
        }
    }

    public static final InternalBlockBean internalBlockBeanFromJson(String jsonString) {
        try {
            return getJacksonMapper().readValue(jsonString, InternalBlockBean.class);
        } catch (JsonProcessingException ex) {
            log.error("InternalTransactionBean deserialization error", ex);
            return null;
        }
    }

    public static final String toJson(ArrayList<TransactionBean> listTB) {
        try {
            return getJacksonMapper().writeValueAsString(listTB);
        } catch (JsonProcessingException ex) {
            log.error("TransactionBean list serialization error.");
            return null;
        }
    }

    public static final ArrayList<TransactionBean> getTransactionBeanListFromJson(String jsonString) {
        try {
            return getJacksonMapper().readValue(jsonString, type_ArrayList_TransactionBean);
        } catch (JsonProcessingException ex) {
            log.error("TransactionBean list deserialization error.");
            return null;
        }
    }
    
    public static final String toJson(TransactionBean tb) {
        try {
            return getJacksonMapper().writeValueAsString(tb);
        } catch (JsonProcessingException ex) {
            log.error("TransactionBean serialization error.");
            return null;
        }
    }

    public static final TransactionBean transactionBeanFromJson(String transactionBeanJson) {
        try {
            return getJacksonMapper().readValue(transactionBeanJson, TransactionBean.class);
        } catch (JsonProcessingException ex) {
            log.error("TransactionBean deserialization error.");
            return null;
        }
    }

    /**
     * function for sorting hash
     *
     * @param input
     * @return
     */
    public static final String getSortingString(String input) {
        return new BigInteger(1, input.getBytes(FixedParameters.CHARSET)).toString();
    }

    /**
     * ITB Hash. It puts all the itb fields together, than it generates the hash
     * code
     * <br>
     * StringBuilder sb = new StringBuilder();
     * <br>
     * sb.append(itb.getFrom());<br>
     *
     * sb.append(itb.getTo());<br>
     *
     * sb.append(itb.getMessage());<br>
     *
     * sb.append(itb.getNotBefore().getTime());<br>
     *
     * sb.append(itb.getRedValue());<br>
     *
     * sb.append(itb.getGreenValue());<br>
     *
     * sb.append(itb.getTransactionType().name());<br>
     *
     * sb.append(itb.getEpoch());<br>
     *
     * sb.append(itb.getSlot());<br>
     *
     * String hash = TkmSignUtils.Hash256(sb.toString());<br>
     *
     * @param itb
     * @return String the hash encoded
     * @throws NullInternalTransactionBeanException
     * @throws HashCompositionException
     */
    public static final String internalTransactionBeanHash(InternalTransactionBean itb) throws NullInternalTransactionBeanException, HashCompositionException {
        try {
            if (itb == null) {
                throw new NullInternalTransactionBeanException("null itb");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(itb.getFrom());
            sb.append(itb.getTo());
            sb.append(itb.getMessage());
            sb.append(itb.getNotBefore().getTime());
            sb.append(itb.getRedValue());
            sb.append(itb.getGreenValue());
            sb.append(itb.getTransactionType().name());
            sb.append(itb.getEpoch());
            sb.append(itb.getSlot());
//            F.y(sb.toString());
            String hash = TkmSignUtils.Hash256(sb.toString());
            return hash;
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("internal transaction bean hash generation exception", ex);
            throw new HashCompositionException(ex);
        }
    }

    /**
     * Transaction Bean Random string generator
     *
     * @return a 4 digit alphanumeric random string
     */
    public static final String generateWalletRandomString() {
        return RandomStringUtils.randomAlphanumeric(4);
    }
    
    /**
     * Hash used inside block for transaction identification
     *
     * @param itbHash
     * @param addr
     * @param sig
     * @param random
     * @param walletCypher
     * @return
     * @throws InclusionHashCreationException
     */
    public static final String singleTransactionInclusionHash(String itbHash, String addr, String sig, String random, String walletCypher) throws InclusionHashCreationException {
        try {
            return TkmSignUtils.Hash256(itbHash + addr + sig + random + walletCypher);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("Error creating SITH", ex);
            throw new InclusionHashCreationException(ex);
        }
    }
}
