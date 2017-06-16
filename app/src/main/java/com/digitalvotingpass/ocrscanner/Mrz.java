package com.digitalvotingpass.ocrscanner;

import com.digitalvotingpass.digitalvotingpass.DocumentData;

public class Mrz {

    private static final int[] PASSPORT_DOCNO_INDICES = new int[]{0, 9};
    private static final int[] PASSPORT_DOB_INDICES = new int[]{13, 19};
    private static final int[] PASSPORT_EXP_INDICES = new int[]{21, 27};

    private static final int[] ID_DOCNO_INDICES = new int[]{5, 14};
    private static final int[] ID_DOB_INDICES = new int[]{0, 6};
    private static final int[] ID_EXP_INDICES = new int[]{8, 14};
    private static final int[] PASSPORT_PERSONAL_NUMBER_INDICES = new int[]{28, 42};

    private String mrz;

    public Mrz(String mrz) {
        this.mrz = mrz;
        cleanMRZString();
    }

    /**
     * Does some basic cleaning on the MRZ string of this object
     * Done before checksum verification so may throw errors, these are ignored
     */
    private void cleanMRZString() {
        try {
            String[] spl = mrz.replace(" ", "").replace("\n\n", "\n").split("\n"); // Delete any space characters and replace double newline with a single newline
            mrz = spl[0] + "\n" + spl[1]; // Extract only first 2 lines, sometimes random errorous data is detected beyond.
        }catch (Exception ignored) {
        }
    }

    /**
     * Performs checksum calculation of the given string's chars from start til end.
     * Uses value at index {@code checkIndex} in {@code string} as check value.
     * @param string String to be checked
     * @param ranges indices of substrings to be checked
     * @param checkIndex index of char to check against
     * @return boolean whether check was successful
     */
    private static boolean checkSum (String string, int[][] ranges, int checkIndex) {
        int[] code = { 7, 3, 1};
        int checkValue = Character.getNumericValue(string.charAt(checkIndex));
        int count = 0;
        float checkSum = 0;
        for (int[] range : ranges) {
            char[] line = string.substring(range[0], range[1]).toCharArray();
            for (char c : line) {
                int num;
                if (Character.toString(c).matches("[A-Z]")) {
                    num = ((int) c - 55);
                } else if (Character.toString(c).matches("\\d")) {
                    num = Character.getNumericValue(c);
                } else if (Character.toString(c).matches("<")) {
                    num = 0;
                } else {
                    return false; //illegal character
                }
                checkSum += num * code[count%3];
                count++;
            }
        }
        int rem = (int) checkSum % 10;
        return rem == checkValue;
    }

    /**
     * Returns relevant data from the MRZ in a DocumentData object.
     * @return DocumentData object
     */
    public DocumentData getPrettyData() {
        DocumentData data = new DocumentData();
        if (mrz.startsWith("P")) {
            data.setDocumentNumber(mrz.split("\n")[1].substring(PASSPORT_DOCNO_INDICES[0], PASSPORT_DOCNO_INDICES[1]));
            data.setDateOfBirth(mrz.split("\n")[1].substring(PASSPORT_DOB_INDICES[0], PASSPORT_DOB_INDICES[1]));
            data.setExpiryDate(mrz.split("\n")[1].substring(PASSPORT_EXP_INDICES[0],PASSPORT_EXP_INDICES[1]));
        } else if (mrz.startsWith("I")) {
            data.setDocumentNumber(mrz.split("\n")[0].substring(ID_DOCNO_INDICES[0],ID_DOCNO_INDICES[1]));
            data.setDateOfBirth(mrz.split("\n")[1].substring(ID_DOB_INDICES[0],ID_DOB_INDICES[1]));
            data.setExpiryDate(mrz.split("\n")[1].substring(ID_EXP_INDICES[0],ID_EXP_INDICES[1]));
        }
        return data;
    }

    /**
     * Checks if this MRZ data is valid
     * @return boolean whether the given input is a correct MRZ.
     */
    public boolean valid() {
        try {
            if (mrz.startsWith("P")) {
                return checkPassportMRZ(mrz);
            } else if (mrz.startsWith("I")){
                return checkIDMRZ(mrz);
            }
        } catch (Exception ignored) {
            // Probably an outOfBounds indicating the format was incorrect
        }
        return false;
    }

    private boolean checkIDMRZ(String mrz) {
        boolean firstCheck = checkSum(mrz.split("\n")[0], new int[][]{ID_DOCNO_INDICES}, 14); //Checks document number
        boolean secondCheck = checkSum(mrz.split("\n")[1], new int[][]{ID_DOB_INDICES}, 6); //Checks DoB
        boolean thirdCheck = checkSum(mrz.split("\n")[1], new int[][]{ID_EXP_INDICES}, 14); //Checks expiration date
        boolean fourthCheck = checkSum(mrz.replace("\n", ""), new int[][]{{5, 30}, {30, 37}, {38, 45}, {49, 59}}, 59); //Checks upper line from 6th digit + middle line
        return firstCheck && secondCheck && thirdCheck && fourthCheck;
    }

    private boolean checkPassportMRZ(String mrz) {
        boolean firstCheck = checkSum(mrz.split("\n")[1], new int[][]{PASSPORT_DOCNO_INDICES}, 9); // Checks document number
        boolean secondCheck = checkSum(mrz.split("\n")[1], new int[][]{PASSPORT_DOB_INDICES}, 19);
        boolean thirdCheck = checkSum(mrz.split("\n")[1], new int[][]{PASSPORT_EXP_INDICES}, 27);
        boolean fourthCheck = checkSum(mrz.split("\n")[1], new int[][]{PASSPORT_PERSONAL_NUMBER_INDICES}, 42);
        boolean fifthCheck = checkSum(mrz.split("\n")[1], new int[][]{{0, 10}, {13, 20}, {21, 43}}, 43);
        return firstCheck && secondCheck && thirdCheck && fourthCheck && fifthCheck;
    }

    public String getText() {
        return mrz;
    }
}
