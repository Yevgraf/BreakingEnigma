    package org.example;
    import java.io.BufferedReader;
    import java.io.FileReader;
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.security.MessageDigest;
    import java.security.NoSuchAlgorithmException;
    import java.util.*;

    public class BreakingEnigma {
        private final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private final String saltAlphabet = "!#$%&*+-:;<=>?@";

        public static void main(String[] args) {
            BreakingEnigma enigma = new BreakingEnigma();

            // Manually setting parameters for a single test case
            String input = "A";
            String salt = ".";
            int saltMode = 1;
            int rotation = 23;
            int increment = 23;
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String plugboard = "{'B': 'P', 'K': 'U', 'O': 'J', 'S': 'F', 'I': 'X', 'N': 'R', 'H': 'Y', 'V': 'A', 'E': 'Q', 'T': 'D', 'C': 'Z', 'W': 'M'}";
            String expectedResult = "X"; // The expected decrypted result

            String decryptedWord = enigma.decryptHash(input, salt, saltMode, rotation, increment, alphabet, plugboard);

            // Check if the decryption result matches the expected result
            if (decryptedWord != null && decryptedWord.equals(expectedResult)) {
                System.out.println("Test case passed for input: " + input);
            } else {
                System.out.println("Test case failed for input: " + input);
            }
        }



        private static Map<Character, Character> parsePlugboardMapping(String input) {
            Map<Character, Character> mapping = new HashMap<>();
            input = input.replaceAll("[{}'\\s]", "");

            for (String pair : input.split(",")) {
                String[] keyValue = pair.split(":");
                char key = keyValue[0].charAt(0);
                char value = keyValue[1].charAt(0);
                mapping.put(key, value);
            }

            return mapping;
        }

        public String decryptHash(String input, String salt, int saltMode, int rotation, int increment, String alphabet, String plugboard) {
            try {
                List<String> saltCombinations = generateSaltCombinations();

                for (String saltCombination : saltCombinations) {
                    String saltedFrontWord = saltCombination + input;
                    String saltedBackWord = input + saltCombination;

                    String plugBoardedFrontWord = applyPlugboardMapping(saltedFrontWord, parsePlugboardMapping(plugboard));
                    String plugBoardedBackWord = applyPlugboardMapping(saltedBackWord, parsePlugboardMapping(plugboard));

                    for (int incrementFactor = 0; incrementFactor < 6; incrementFactor++) {
                        int rotationValue = (6 * incrementFactor + rotation) % 26;

                        String translatedFrontWord = translate(plugBoardedFrontWord, incrementFactor, rotationValue, salt, saltMode);
                        String translatedBackWord = translate(plugBoardedBackWord, incrementFactor, rotationValue, salt, saltMode);

                        String hashedFrontWord = calculateSHA512(translatedFrontWord);
                        String hashedBackWord = calculateSHA512(translatedBackWord);

                        if (hashedFrontWord.equalsIgnoreCase(input) || hashedBackWord.equalsIgnoreCase(input)) {
                            return input;
                        }
                    }
                }
                System.out.println("No match found.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }


        private List<String> generateSaltCombinations() {
            List<String> combinations = new ArrayList<>();
            for (char firstSaltChar : saltAlphabet.toCharArray()) {
                for (char secondSaltChar : saltAlphabet.toCharArray()) {
                    combinations.add(String.valueOf(firstSaltChar) + secondSaltChar);
                }
            }
            return combinations;
        }

        private String applyPlugboardMapping(String word, Map<Character, Character> plugboardMapping) {
            StringBuilder mappedWord = new StringBuilder();

            for (int i = 0; i < word.length(); i++) {
                char currentChar = word.charAt(i);
                char mappedChar = plugboardMapping.getOrDefault(currentChar, currentChar);
                mappedWord.append(mappedChar);
            }

            return mappedWord.toString();
        }

        private String calculateSHA512(String wordForHashing) {
            StringBuilder sb = new StringBuilder();
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                byte[] data = md.digest(wordForHashing.getBytes(StandardCharsets.UTF_8));
                for (byte datum : data) {
                    sb.append(String.format("%02x", datum));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error generating hash: " + e.getMessage());
            }
            return sb.toString();
        }

        private String translate(String word, int incrementFactor, int rotation, String salt, int saltMode) {
            StringBuilder translatedWord = new StringBuilder();
            int currentIndex = 0;

            for (char wordChar : word.toCharArray()) {
                int inc = currentIndex * incrementFactor;

                if (saltAlphabet.indexOf(wordChar) != -1) {
                    int saltIndex = saltAlphabet.indexOf(wordChar);
                    int rotatedSaltIndex = (saltIndex + rotation + inc) % saltAlphabet.length();
                    if (rotatedSaltIndex < 0) {
                        rotatedSaltIndex += saltAlphabet.length();
                    }
                    char newSaltChar = saltAlphabet.charAt(rotatedSaltIndex);
                    translatedWord.append(newSaltChar);
                } else {
                    if (!Character.isLetter(wordChar)) {
                        translatedWord.append(wordChar); // Keep non-letter characters as is
                        continue;
                    }

                    int indexOfChar = alphabet.indexOf(Character.toUpperCase(wordChar));

                    if (indexOfChar == -1) continue;

                    int saltIndex = currentIndex % salt.length();
                    char currentSaltChar = salt.charAt(saltIndex);
                    int saltRotation = saltAlphabet.indexOf(currentSaltChar) * saltMode;

                    indexOfChar = (indexOfChar + rotation + inc + saltRotation) % alphabet.length();

                    if (indexOfChar < 0) {
                        indexOfChar += alphabet.length();
                    }

                    char newCharacter = alphabet.charAt(indexOfChar);
                    if (Character.isLowerCase(wordChar)) {
                        newCharacter = Character.toLowerCase(newCharacter); // Preserve lowercase
                    }
                    translatedWord.append(newCharacter);
                    currentIndex++;
                }
            }

            return translatedWord.toString();
        }
    }
