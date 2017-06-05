package com.digitalvotingpass.ocrscanner;

import com.digitalvotingpass.digitalvotingpass.DocumentData;
import com.digitalvotingpass.ocrscanner.Mrz;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by jonathan on 5/16/17.
 */

public class MrzTest {

    private String valid_id_mrz =   "I<NLDCI00383F12999999990<<<<<8\n" +
                                    "7208148F1108268NLD<<<<<<<<<<<2\n" +
                                    "VAN<DER<STEEN<<MARIANNE<LOUISE";

    private String valid_id_doc_num = "CI00383F1";
    private String valid_id_data_of_birth = "720814";
    private String valid_id_expiry_data = "110826";

    private String invalid_id_mrz =   "I<NLDCI00383F12999999990<<<<<8\n" +
                                        "7208148F11T8268NLD<<<<<<<<<<<2\n" +
                                        "VAN<DER<STEEN<<MARIANNE<LOUISE";

    private String valid_passport_mrz = "P<NLDBLEH<<JAN<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" +
                                        "GHD777O861NLD9005226M18062657542648264<<<<04";

    private String valid_pass_doc_num = "GHD777O86";
    private String valid_pass_data_of_birth = "900522";
    private String valid_pass_expiry_data = "180626";

    private String invalid_passport_mrz = "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<\n"+
                                            "L898902C<3UTO6918061F9406236ZE184226B<<<<<14";



    @Test
    public void valid_passport_mrz() throws Exception {
        Mrz valid = new Mrz(valid_passport_mrz);
        assertTrue(valid.valid());
    }

    @Test
    public void valid_id_mrz() throws Exception {
        Mrz valid = new Mrz(valid_id_mrz);
        assertTrue(valid.valid());
    }

    @Test
    public void invalid_passport_mrz() throws Exception {
        Mrz invalid = new Mrz(invalid_passport_mrz);
        assertFalse(invalid.valid());
    }

    @Test
    public void invalid_id_mrz() throws Exception {
        Mrz invalid = new Mrz(invalid_id_mrz);
        assertFalse(invalid.valid());
    }

    @Test
    public void null_check() throws Exception {
        Mrz invalid = new Mrz(null);
        assertFalse(invalid.valid());
    }

    @Test
    public void empty_string_is_invalid() throws Exception {
        Mrz invalid = new Mrz("");
        assertFalse(invalid.valid());
    }

    @Test
    public void get_pretty_data_test() throws Exception {
        Mrz valid = new Mrz(valid_id_mrz);
        assertEquals(valid_id_doc_num, valid.getPrettyData().getDocumentNumber());
        assertEquals(valid_id_data_of_birth, valid.getPrettyData().getDateOfBirth());
        assertEquals(valid_id_expiry_data, valid.getPrettyData().getExpiryDate());
        assertTrue(valid.getPrettyData().isValid());
    }

    @Test
    public void get_pretty_data_test2() throws Exception {
        Mrz valid = new Mrz(valid_passport_mrz);
        DocumentData data = valid.getPrettyData();
        assertEquals(valid_pass_doc_num, data.getDocumentNumber());
        assertEquals(valid_pass_data_of_birth, data.getDateOfBirth());
        assertEquals(valid_pass_expiry_data, data.getExpiryDate());
        assertTrue(data.isValid());
    }
}
