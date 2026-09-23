/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.InvalidRecoveryWordsException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author francesco.pasetto@takamaka.io
 */
@Slf4j
public class SeedGenerator {

    public static String[] words = new String[FixedParameters.WALLET_DICTIONARY_LENGTH];

    public static boolean inizialized = false;

    public static void init() {
        BufferedReader reader = null;
        try {
            String fileName = FixedParameters.WALLET_DICTIONARY_FILE;
            ClassLoader classLoader = new SeedGenerator().getClass().getClassLoader();
            reader = new BufferedReader(new StringReader(IOUtils.toString(classLoader.getResource(fileName), FixedParameters.CHARSET)));
            String mLine;
            int i = 0;
            while ((!TkmTextUtils.isNullOrBlank((mLine = reader.readLine()))) && (i < FixedParameters.WALLET_DICTIONARY_LENGTH)) {
                words[i] = mLine;
                i++;
            }
        } catch (IOException e) {
            log.error("io error in seed generation", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("error closing reader in exception handling", e);
                }
            }
        }
    }

    /**
     * It generates a random 25 list of words used for wallet creation
     *
     * @return 
     * @throws NoSuchAlgorithmException
     */
    public static List<String> generateWords() throws NoSuchAlgorithmException {
        try {
            List<String> rndWords = new ArrayList<String>();

            if (!inizialized) {
                init();
                inizialized = true;
            }

            String concat = "";
            Random rand = new SecureRandom();
            for (int i = 0; i < FixedParameters.WALLET_WORDS_NUMBER - 2; i++) {
                int rnd = rand.nextInt(2048);
                rndWords.add(words[rnd]);
                concat += words[rnd];
            }

            int index;
            index = (new BigInteger(TkmSignUtils.PWHash(concat, "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(FixedParameters.WALLET_DICTIONARY_LENGTH))).intValue();
            String secondToLastWord = words[index];
            concat += words[index];
            index = (new BigInteger(TkmSignUtils.PWHash(concat, "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(FixedParameters.WALLET_DICTIONARY_LENGTH))).intValue();
            String lastWord = words[index];
            rndWords.add(secondToLastWord);
            rndWords.add(lastWord);
            return rndWords;
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException ex) {
            log.error("error generating words", ex);
            return null;
        }
    }

    /**
     * True when {@code wordsList} is a valid recovery phrase: exactly 25
     * words, every word in the dictionary (exact, case-sensitive), and words
     * 24-25 equal to the checksum of the words before them. The same three
     * checks as wallet-core-flutter {@code SeedGenerator.verifySeedWords} and
     * takamaka-sdk-wrap {@code TkmMnemonic.requireValid} (F64).
     */
    public static boolean verifySeedWords(List<String> wordsList) {
        try {
            if (wordsList == null || wordsList.size() != FixedParameters.WALLET_WORDS_NUMBER) {
                return false;
            }

            if (!inizialized) {
                init();
                inizialized = true;
            }

            if (!wordsNotInDictionary(wordsList).isEmpty()) {
                return false;
            }

            String concat = "";
            for (int i = 0; i < wordsList.size() - 2; i++) {
                concat += wordsList.get(i);
            }

            int index;
            index = (new BigInteger(TkmSignUtils.PWHash(concat, "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(FixedParameters.WALLET_DICTIONARY_LENGTH))).intValue();
            String secondToLastWord = words[index];
            concat += words[index];
            index = (new BigInteger(TkmSignUtils.PWHash(concat, "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(FixedParameters.WALLET_DICTIONARY_LENGTH))).intValue();
            String lastWord = words[index];

            return (lastWord.equals(wordsList.get(wordsList.size() - 1)) && secondToLastWord.equals(wordsList.get(wordsList.size() - 2)));

        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            log.error("seed verification failure", ex);
            return false;
        }
    }

    /**
     * 1-based positions of the words that are not in the dictionary (exact,
     * case-sensitive match); empty when every word is a dictionary word.
     */
    public static List<Integer> wordsNotInDictionary(List<String> wordsList) {
        if (!inizialized) {
            init();
            inizialized = true;
        }
        Set<String> dictionary = new HashSet<>(Arrays.asList(words));
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < wordsList.size(); i++) {
            String w = wordsList.get(i);
            if (w == null || !dictionary.contains(w)) {
                positions.add(i + 1);
            }
        }
        return positions;
    }

    /**
     * F64 — throws {@link InvalidRecoveryWordsException} unless
     * {@code wordsList} is a valid phrase (count, dictionary, checksum). The
     * message carries counts and positions only, never a word.
     *
     * @param wordsList the recovery phrase
     * @throws InvalidRecoveryWordsException when the phrase is not valid
     */
    public static void requireValidSeedWords(List<String> wordsList) throws InvalidRecoveryWordsException {
        if (wordsList == null || wordsList.size() != FixedParameters.WALLET_WORDS_NUMBER) {
            throw new InvalidRecoveryWordsException("expected " + FixedParameters.WALLET_WORDS_NUMBER
                    + " recovery words, found " + (wordsList == null ? 0 : wordsList.size()));
        }
        List<Integer> unknown = wordsNotInDictionary(wordsList);
        if (!unknown.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Integer p : unknown) {
                sb.append(sb.length() == 0 ? "" : ", ").append(p);
            }
            throw new InvalidRecoveryWordsException("word(s) at position " + sb + " are not dictionary words");
        }
        if (!verifySeedWords(wordsList)) {
            throw new InvalidRecoveryWordsException("checksum mismatch: a word is wrong, misspelt or out of order");
        }
    }

    /**
     * DR-011 progress seam — a plain callback fired after each of the 25 words
     * is derived (≈25 ticks, matching the Flutter {@code generateSeedPwhWithProgress}
     * granularity). Deliberately NOT a Reactor type: wallet-core stays
     * dependency-clean (Java 11, no reactor-core); upstream (rsclient /
     * chat-web-gui) bridges this into a {@code Flux<Progress>}. The callback is
     * invoked AFTER the per-word salt update and never touches the hash
     * computation, so the derived seed is byte-identical with or without it
     * (parity-safe).
     */
    @FunctionalInterface
    public interface SeedProgressListener {

        void onWordDerived(int completedWords, int totalWords);
    }

    /**
     * @param rndWords the generated list of words
     * @return String is the generated seed
     * @throws NoSuchAlgorithmException
     * @throws HashEncodeException
     * @throws InvalidKeySpecException
     * @throws HashAlgorithmNotFoundException
     * @throws HashProviderNotFoundException
     */
    public static String generateSeedPWH(List<String> rndWords) throws NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return generateSeedPWH(rndWords, null);
    }

    /**
     * Progress-reporting overload of {@link #generateSeedPWH(java.util.List)}.
     * Output-identical to the no-arg form; {@code progress} (nullable) is fired
     * once per derived word.
     *
     * @param rndWords the generated list of words
     * @param progress per-word progress callback, or {@code null} for none
     * @return String is the generated seed
     * @throws NoSuchAlgorithmException
     * @throws HashEncodeException
     * @throws InvalidKeySpecException
     * @throws HashAlgorithmNotFoundException
     * @throws HashProviderNotFoundException
     */
    public static String generateSeedPWH(List<String> rndWords, SeedProgressListener progress) throws NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        int saltIndex = 0;
        init();
        for (int i = 0; i < words.length; i++) {
            if (rndWords.get(0).equals(words[i])) {
                saltIndex = i;
                break;
            }
        }

        List<String> hashTable;
        String salt = String.valueOf(saltIndex);
        String tempWord = "";
        int completedWords = 0;
        final int totalWords = rndWords.size();
        for (String word : rndWords) {
            hashTable = new ArrayList<>();
            tempWord = word;
            for (int i = 0; i < FixedParameters.WALLET_DICTIONARY_LENGTH; i++) {

                tempWord = TkmSignUtils.PWHashB64(tempWord, salt, 1, 768);

                hashTable.add(tempWord);
            }
            int modIndex = new BigInteger(salt.getBytes()).abs().mod(new BigInteger("2048")).intValue();
            salt += hashTable.get(modIndex);
            completedWords++;
            if (progress != null) {
                progress.onWordDerived(completedWords, totalWords);
            }
        }

        return tempWord;
    }

    /**
     * convert the seed to seed string
     */
    public static synchronized String toStringSeed(List<String> rndWords) {
        return String.join(" ", rndWords);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException {

        List<String> rndWords = generateWords();
    }

}
