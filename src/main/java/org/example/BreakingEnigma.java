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
    private static final Map<String, String> plugboard = new HashMap<>();
    private static String decryptionResult;
    private static final String enigmaAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java -jar BreakingEnigma.jar <hash> <plugboard_config> <wordlist_filepath>");
            System.out.println("Example: java -jar BreakingEnigma.jar 6e8c4b3a71543af80890dd501a70e030f0a1f867631175f7440a4599041d52e3 \"{'N': 'Q', 'S': 'B', 'X': 'W', 'T': 'G', 'R': 'D', 'J': 'C', 'F': 'O', 'V': 'I', 'L': 'P', 'H': 'Y'}\" ./wordlist.txt");
            return;
        }

        hash = args[0];
        parsePlugboardConfig(args[1]);
        String wordlistFilePath = args[2];
        List<String> wordlist = readWordlist(wordlistFilePath);


        int rot = -1;
        while (!passwordFound()) {
            rot++;
            for (String word : wordlist) {
                String plugWord = convertWordToPlugboard(word);

                if (passwordFound()) break;

                for (char firstSaltChar : "!#%&*+-:;=?@".toCharArray()) {
                    for (char secondSaltChar : "!#%&*+-:;=?@".toCharArray()) {
                        String salt = convertWordToPlugboard(String.valueOf(firstSaltChar) + secondSaltChar);
                        System.out.println("Salt: " + salt + "    Rotation: " + rot + "    Word: " + word);

                        for (int f = 0; f < plugWord.length() ; f++) {
                            String passwordHash = enhancedCaesar(plugWord, salt, rot, f);

                            if (passwordFound(passwordHash)) {
                                decryptionResult = word;
                                System.out.println("Password Found: " + word + "    Salt: " + salt + "    Rotation: " + rot + "    Hash: " + passwordHash);
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

        System.out.println("Found: " + decryptionResult);
    }

    private static void parsePlugboardConfig(String plugboardConfig) {
        plugboardConfig = plugboardConfig.substring(1, plugboardConfig.length() - 1);
        String[] pairs = plugboardConfig.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split(": ");
            String key = keyValue[0].replaceAll("'", "");
            String value = keyValue[1].replaceAll("'", "");
            plugboard.put(key, value);
        }
    }

  /*  private static void printPlugboard() {
        System.out.println("Plugboard Configuration:");
        for (Map.Entry<String, String> entry : plugboard.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.exit(0);
    } */


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

        // Attempt with salt in front of the word
        calculatedWord = enhancedCaesarCalculator(salt + word, rot, f);
        calculatedWord = convertWordToPlugboard(calculatedWord);

        String hash = convertWordToSha256(calculatedWord);
        if (hash.equals(BreakingEnigma.hash)) {
            return hash;
        }

        // Attempt with salt at the end of the word
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

