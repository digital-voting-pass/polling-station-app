package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.tlv.TLVOutputStream;

import org.jmrtd.BACKeySpec;
import org.jmrtd.ChipAuthenticationResult;
import org.jmrtd.DESedeSecureMessagingWrapper;
import org.jmrtd.PassportService;
import org.jmrtd.TerminalAuthenticationResult;
import org.jmrtd.Util;
import org.jmrtd.cert.CVCAuthorizationTemplate;
import org.jmrtd.cert.CVCPrincipal;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.jmrtd.lds.CVCAFile;
import org.jmrtd.lds.DG15File;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.MRZInfo;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;

public class jMRTDActivity extends AppCompatActivity {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    // Adapter for NFC connection
    private NfcAdapter mNfcAdapter;


    /**
     * This activity usually be loaded from the starting screen of the app.
     * This method handles the start-up of the activity, it does not need to call any other methods
     * since the activity onNewIntent() calls the intentHandler when a NFC chip is detected.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        setContentView(R.layout.activity_j_mrtd);
        TextView textView = (TextView) findViewById(R.id.textView);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            textView.setText(R.string.nfc_not_supported_error);
            Toast.makeText(this, R.string.nfc_not_supported_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            textView.setText(R.string.nfc_disabled_error);
        }
    }

    /**
     * Some methods to ensure that when the activity is opened the ID is read.
     * When the activity is opened any nfc device held against the phone will cause, handleIntent to
     * be called.
     */
    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Filter for nfc tag discovery
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /**
     * Handle the intent following from a NFC detection.
     *
     */
    private void handleIntent(Intent intent){
        TextView textView = (TextView) findViewById(R.id.textView);
        TextView textSignedData = (TextView) findViewById(R.id.signedData);

        // if nfc tag holds no data, return
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        // Open a connection with the ID, return a PassportService object which holds the open connection
        PassportService ps = openConnection(tag);
        InputStream is = null;
        InputStream is15 = null;
        try {
            // Get the BSN from datagroup1 to confirm the ID was scanned correctly.
            // This is for testing purposes
            is = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
            Toast.makeText(this, dg1.getMRZInfo().getPersonalNumber(), Toast.LENGTH_LONG).show();

            // display data from dg15
            is15 = ps.getInputStream(PassportService.EF_DG15);

            PublicKey pubk = getAAPublicKey(ps,is15);

            // sign 8 bytes of data and display the signed data
            textSignedData.setText(byteArrayToHexString(signData(ps,is15,pubk)));

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        } finally {
            try {
                ps.close();
                is.close();
                is15.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Opens a connection with the ID by doing BAC
     * Uses hardcoded parameters for now
     * @param tag - NFC tag that started this activity (ID NFC tag)
     * @return PassportService - passportservice that has an open connection with the ID
     */
    private PassportService openConnection(Tag tag) {
        PassportService ps = null;
        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            ps = new PassportService(cs);
            ps.open();

            // Get the information needed for BAC, hardcoded for now
            // TODO: link this with OCR functionality
            ps.sendSelectApplet(false);
            BACKeySpec bacKey = new BACKeySpec() {
                @Override
                public String getDocumentNumber() {
                    return "NP0811B03";
                }
                @Override
                public String getDateOfBirth() {
                    return "940610";
                }
                @Override
                public String getDateOfExpiry() {
                    return "180624";
                }
            };

            ps.doBAC(bacKey);
            return ps;
        } catch (Exception ex) {
            ex.printStackTrace();
            ps.close();
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
        return null;
    }


    /**
     * Retrieves the public key used for Active Authentication from datagroup 15.
     * @return Publickey - returns the publickey used for AA
     */
    private PublicKey getAAPublicKey(PassportService ps, InputStream is15) {
        try {
            DG15File dg15 = new DG15File(is15);
            return dg15.getPublicKey();
        } catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    /**
     * Signs 8 bytes by the passport using the AA functionality.
     * @return byte[] - signed byte array
     */
    private byte[] signData(PassportService ps, InputStream is15, PublicKey pubk) {
        try {
            // test 8 byte string for testing purposes
            byte[] data = hexStringToByteArray("0a1b3c4d5e6faabb");
            return ps.doAA(pubk, null, null, data);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    /**
     * Method for converting a hexString to a byte array
     * This method is used for signing transaction hashes (which are in hex)
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Method for converting a byte array to a hexString
     * This method is used for converting a signed 8-byte array back to a hashString in order to
     * display it readable
     */
    public static String byteArrayToHexString(byte[] b) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[b.length * 2];
        for ( int j = 0; j < b.length; j++ ) {
            int v = b[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
