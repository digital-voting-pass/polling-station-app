package com.digitalvotingpass.utilities;

import com.digitalvotingpass.utilities.MultiChainAddressGenerator;
import com.digitalvotingpass.utilities.Util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultiChainAddressGeneratorTest {
    @Test
    public void testGetAddress() throws Exception {
        String[] version = new String[]{"00", "AF", "EA", "21"};
        String addresschecksum = "953ABC69";

        MultiChainAddressGenerator generator = new MultiChainAddressGenerator();
        String pubKey = "0284E5235E299AF81EBE1653AC5F06B60E13A3A81F918018CBD10CE695095B3E24";
        byte[] keybytes = Util.hexStringToByteArray(pubKey);
        String address = generator.getPublicAddress(version, addresschecksum, keybytes);
        assertEquals(address, "1Yu2BuptuZSiBWfr2Qy4aic6qEVnwPWrdkHPEc");
    }

    @Test
    public void testGetAddress2() throws Exception {
        String[] version = new String[]{ "00", "8c", "b5", "d6"};
        String addresschecksum = "5afce7b2";

        MultiChainAddressGenerator generator = new MultiChainAddressGenerator();
        String pubKey = "6c284c42d95e4933513d663a8a2e4cf0e41bcb7f8397bd065e6e3f2d4176e95f66c317bf612bec0207f3954783fd361a71118eea680655607953a1aceddf045c7133e99f35a9391c5c28d5d82c12b148";
        byte[] keybytes = Util.hexStringToByteArray(pubKey);
        String address = generator.getPublicAddress(version, addresschecksum, keybytes);
        assertEquals(address, "1BNgsh92p1wFmKiQVPs9fNyriG72wygs9esHV8");
    }
}
