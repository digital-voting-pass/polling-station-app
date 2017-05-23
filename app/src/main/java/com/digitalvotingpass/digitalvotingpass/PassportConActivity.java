package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.jmrtd.PassportService;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.PublicKey;
import java.security.Security;
import java.util.HashMap;

public class PassportConActivity extends AppCompatActivity {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    // Adapter for NFC connection
    private NfcAdapter mNfcAdapter;
    private HashMap<String, String> documentData;

    /**
     * This activity usually be loaded from the starting screen of the app.
     * This method handles the start-up of the activity, it does not need to call any other methods
     * since the activity onNewIntent() calls the intentHandler when a NFC chip is detected.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        documentData = (HashMap<String, String>) extras.get("docData");

        setContentView(R.layout.activity_passport_con);
        TextView notice = (TextView) findViewById(R.id.notice);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.nfc_not_supported_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            notice.setText(R.string.nfc_disabled_error);
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
        // It's important, that the activity is in the foreground (resumed). Otherwise an IllegalStateException is thrown.
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        // Call this before super.onPause, otherwise an IllegalArgumentException is thrown as well.
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    /**
     * This method gets called, when a new Intent gets associated with the current activity instance.
     * Instead of creating a new activity, onNewIntent will be called. For more information have a look
     * at the documentation.
     *
     * In our case this method gets called, when the user attaches a Tag to the device.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Setup the recognition of nfc tags when the activity is opened (foreground)
     *
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
    private void handleIntent(Intent intent) {
        TextView textSignedData = (TextView) findViewById(R.id.signedData);

        // if nfc tag holds no data, return
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        // Open a connection with the ID, return a PassportService object which holds the open connection
        PassportConnection pcon= new PassportConnection();
        PassportService ps = pcon.openConnection(tag, documentData);
        try {
            // Get the BSN from datagroup1 to confirm the ID was scanned correctly.
            // This is for testing purposes
            Toast.makeText(this, pcon.getBSN(ps), Toast.LENGTH_LONG).show();

            // display data from dg15
            PublicKey pubk = pcon.getAAPublicKey(ps);

            // sign 8 bytes of data and display the signed data + length
            byte[] signedData = pcon.signData(ps);
            textSignedData.setText(Util.byteArrayToHexString(signedData) + " (size: " + signedData.length + ")");
            System.out.println(pcon.getDG15Contents(ps));
            System.out.println("Pubkey: ");
            System.out.println(Util.byteArrayToHexString(pubk.getEncoded()));


        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
