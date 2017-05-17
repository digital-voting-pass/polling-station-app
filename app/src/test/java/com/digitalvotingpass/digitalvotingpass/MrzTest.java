package com.digitalvotingpass.digitalvotingpass;

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

    private String invalid_id_mrz =   "I<NLDCI00383F12999999990<<<<<8\n" +
                                        "7208148F11T8268NLD<<<<<<<<<<<2\n" +
                                        "VAN<DER<STEEN<<MARIANNE<LOUISE";

    private String valid_passport_mrz = "P<NLDBLEH<<JAN<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" +
                                        "GHD777O861NLD9005226M18062657542648264<<<<04";

//    private String valid_passport_mrz2 = "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<\n"+
//                                         "L898902C<3UTO6918061F9406236ZE184226B<<<<<14";

    private String invalid_passport_mrz = "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<\n"+
                                            "L898902C<3UTO6918061F9406236ZE184226B<<<<<14";



    @Test
    public void valid_passport_mrz() throws Exception {
        Mrz valid = new Mrz(valid_passport_mrz);
        assertTrue(valid.valid());
    }

//    @Test
//    public void valid_passport_mrz2() throws Exception {
//        Mrz valid = new Mrz(valid_passport_mrz2);
//        assertTrue(valid.valid());
//    }

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
        assertTrue(valid.getPrettyData().containsKey("Document Number"));
        assertTrue(valid.getPrettyData().containsKey("Date of Birth"));
        assertTrue(valid.getPrettyData().containsKey("Expiration Date"));
    }

    @Test
    public void get_pretty_data_test2() throws Exception {
        Mrz valid = new Mrz(valid_passport_mrz);
        assertNotNull(valid.getPrettyData().get("Document Number"));
        assertNotNull(valid.getPrettyData().get("Date of Birth"));
        assertNotNull(valid.getPrettyData().get("Expiration Date"));
    }
}
