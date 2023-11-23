package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakingEnigma {

    private static String hash;
    private static Map<String, String> plugboard = new HashMap<>();
    private static String decryptionResult;
    private static List<String> wordlist;
    private static final String enigmaAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) throws IOException {
        // Hardcoded plugboard configuration and hash
        hash = "6e8c4b3a71543af80890dd501a70e030f0a1f867631175f7440a4599041d52e3";
        // Add plugboard configurations
        plugboard.put("N", "Q");
        plugboard.put("S", "B");
        plugboard.put("X", "W");
        plugboard.put("T", "G");
        plugboard.put("R", "D");
        plugboard.put("J", "C");
        plugboard.put("F", "O");
        plugboard.put("V", "I");
        plugboard.put("L", "P");
        plugboard.put("H", "Y");

        String wordlistFilePath = "/home/yeev/IdeaProjects/BreakingEnigma/src/main/resources/wordlist.txt";
        wordlist = readWordlist(wordlistFilePath);

        int rot = -1;
        while (!passwordFound()) {
            rot++;

            for (String word : wordlist) {
                String plugWord = convertWordToPlugboard(word);
                System.out.println("Word: " + word + "     Converted: " + plugWord);

                if (passwordFound()) break;

                for (char firstSaltChar : "!#%&*+-:;=?@".toCharArray()) {
                    for (char secondSaltChar : "!#%&*+-:;=?@".toCharArray()) {
                        String salt = convertWordToPlugboard(String.valueOf(firstSaltChar) + secondSaltChar);
                        System.out.println("Salt: " + salt + "    Rotation: " + rot + "    Word: " + word);

                        for (int f = 0; f < plugWord.length() + 2; f++) {
                            String passwordHash = enhancedCaesar(plugWord, salt, rot, f);

                            if (passwordFound(passwordHash)) {
                                decryptionResult = word;
                                System.out.println("Password Found: " + word + "    Salt: " + salt + "    Rotation: " + rot);
                                break;
                            }
                        }

                        if (passwordFound()) break;
                    }

                    if (passwordFound()) break;
                }

                if (passwordFound()) break;
            }
        }

        System.out.println(passwordFound()
                ? "Found: " + decryptionResult
                : "Not found");
    }

    private static List<String> readWordlist(String filePath) throws IOException {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        }
        return words;
    }

    private static boolean passwordFound() {
        return decryptionResult != null && !decryptionResult.isEmpty();
    }

    private static boolean passwordFound(String calculatedHash) {
        return calculatedHash != null && calculatedHash.equals(hash);
    }

    private static String convertWordToSha256(String word) {
        try {
            // SHA-256 hashing algorithm implemented without using java.security.MessageDigest
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(word.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String convertWordToPlugboard(String word) {
        StringBuilder plugWord = new StringBuilder();
        for (char w : word.toCharArray()) {
            String plugboardChar = plugboard.getOrDefault(String.valueOf(w), String.valueOf(w));
            plugWord.append(plugboardChar);
        }
        return plugWord.toString();
    }

    private static String enhancedCaesar(String word, String salt, int rot, int f) {
        String calculatedWord;

        // Attempt 1: Salt in front of the word
        calculatedWord = enhancedCaesarCalculator(salt + word, rot, f);
        calculatedWord = convertWordToPlugboard(calculatedWord);

        String hash = convertWordToSha256(calculatedWord);
        if (hash.equals(BreakingEnigma.hash)) {
            return hash;
        }

        // Attempt 2: Salt at the end of the word
        calculatedWord = enhancedCaesarCalculator(word + salt, rot, f);
        calculatedWord = convertWordToPlugboard(calculatedWord);

        hash = convertWordToSha256(calculatedWord);
        if (hash.equals(BreakingEnigma.hash)) {
            return hash;
        }

        return null;
    }

    private static String enhancedCaesarCalculator(String word, int rot, int f) {
        StringBuilder calculatedWord = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char currChar = word.charAt(i);
            int currIndex = enigmaAlphabet.indexOf(currChar);
            
            if (currIndex == -1) {
                calculatedWord.append(currChar);
                continue;
            }

            int newIndex = (currIndex + rot + (i * f)) % enigmaAlphabet.length();
            if (newIndex < 0) {
                newIndex += enigmaAlphabet.length();
            }

            char newChar = enigmaAlphabet.charAt(newIndex);
            calculatedWord.append(newChar);
        }

        return calculatedWord.toString();
    }

}

