package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakingEnigma {

    private static String hash;
    private static Map<String, String> plugboard = new HashMap<>();
    private static String password;
    private static List<String> wordlist;
    private static int attempts = 0;
    private static final String enigmaAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) {
        // Hardcoded plugboard configuration and hash
        hash = "6e8c4b3a71543af80890dd501a70e030f0a1f867631175f7440a4599041d52e3";
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

        // Load wordlist from file - you need to provide your wordlist data here
        // For now, I'll just initialize an empty list
        wordlist = new ArrayList<>();

        int rot = -1;

        long startTime = System.currentTimeMillis(); // Start time measurement

        while (!passwordFound()) {
            rot++;
            System.out.println("Rot " + rot);

            for (String word : wordlist) {
                String plugWord = convertWordToPlugboard(word);

                if (passwordFound()) break;

                for (char firstSaltChar : "!#$%&*+-:;<=>?@".toCharArray()) {
                    for (char secondSaltChar : "!#$%&*+-:;<=>?@".toCharArray()) {
                        String salt = convertWordToPlugboard(String.valueOf(firstSaltChar) + secondSaltChar);

                        for (int f = 0; f < plugWord.length() + 2; f++) {
                            String passwordHash = enhancedCaesar(plugWord, salt, rot, f);

                            if (passwordHash != null) {
                                password = word;
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

        long endTime = System.currentTimeMillis(); // End time measurement

        System.out.println(passwordFound()
                ? "The password is " + password
                : "Password not found");

        System.out.println(attempts + " attempts in " + (endTime - startTime) + " milliseconds");
    }

    private static void readWordlist(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            List<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
            reader.close();
            wordlist = words;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void parsePlugboard(String config) {
        // Parse the plugboard configuration provided as a string argument
        // Assuming the format is like {'N':'Q','S':'B',...}
        config = config.substring(1, config.length() - 1).replace("'", "").replace(" ", "");
        String[] pairs = config.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            plugboard.put(keyValue[0], keyValue[1]);
        }
    }

        private static String convertWordToPlugboard(String word) {
            StringBuilder plugWord = new StringBuilder();
            for (char w : word.toCharArray()) {
                String plugboardChar = plugboard.getOrDefault(String.valueOf(w), String.valueOf(w));
                plugWord.append(plugboardChar);
            }
            return plugWord.toString();
        }

        private static String enhancedCaesar(String plugWord, String salt, int rot, int f) {
            String calculatedWord, hash = "";

            calculatedWord = enhancedCaesarCalculator(salt + plugWord, rot, f);
            calculatedWord = convertWordToPlugboard(calculatedWord);
            hash = convertWordToSha512(calculatedWord);

            if (hash.equals(BreakingEnigma.hash)) {
                return hash;
            }

            calculatedWord = enhancedCaesarCalculator(plugWord + salt, rot, f);
            calculatedWord = convertWordToPlugboard(calculatedWord);
            hash = convertWordToSha512(calculatedWord);

            if (hash.equals(BreakingEnigma.hash)) {
                return hash;
            }

            return null;
        }

    private static String enhancedCaesarCalculator(String word, int rot, int f) {
        StringBuilder calculatedWord = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            attempts++;
            char currChar = word.charAt(i);
            int inc = i * f;

            // Calculate the new character position without using enigmaLongAlphabet
            int charPosition = enigmaAlphabet.indexOf(currChar);
            int newPosition = (charPosition + rot + inc) % enigmaAlphabet.length();

            char newChar = enigmaAlphabet.charAt(newPosition);
            calculatedWord.append(newChar);
        }

        return calculatedWord.toString();
    }


    private static String convertWordToSha512(String word) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                byte[] hashBytes = digest.digest(word.getBytes());
                StringBuilder hexString = new StringBuilder();

                for (byte hashByte : hashBytes) {
                    String hex = Integer.toHexString(0xff & hashByte);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                return hexString.toString().toLowerCase();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }

        private static boolean passwordFound() {
            return password != null && !password.isEmpty();
        }
    }
