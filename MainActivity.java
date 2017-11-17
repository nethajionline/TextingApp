package com.example.karthikeyan.fingerprintcheck;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Telephony;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private TextView message;
    private static final String KEY_NAME = "SwA";

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;

    private FingerprintManager fingerprintManager;
//insertion

    public String smsSender,smsBody,keyfinal,text;

    // ArrayAdapter arrayAdapter;
    ListView messages;
    Cursor cursor;
    SMSListAdapter smsListAdapter;
    SMSEncListAdapter smsEncListAdapter;
    Context context;
    public static String chatno,chatmsg;
    String  privatekey = "OpIc$-&siLi";
    FloatingActionButton send;
    ImageButton btn,deletecheckbox,donate;
    CheckBox checkbox;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS=1;
    public static int value=0;
//end
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = (TextView) findViewById(R.id.fingerStatus);
        btn = (ImageButton) findViewById(R.id.fingerPrint);
        donate=(ImageButton) findViewById(R.id.donateDollar);
        deletecheckbox = (ImageButton) findViewById(R.id.delete);

        final FingerprintHandler fph = new FingerprintHandler(message);

        if (!checkFinger()) {
            btn.setEnabled(false);
        }
        else {
            // We are ready to set up the cipher and the key
            try {
                generateKey();
                Cipher cipher = generateCipher();
                cryptoObject =
                        new FingerprintManager.CryptoObject(cipher);

            }
            catch(FingerprintException fpe) {
                // Handle exception
                btn.setEnabled(false);
            }
        }
        context=this;
        messages = (ListView) findViewById(R.id.listMessages);
        send = (FloatingActionButton) findViewById(R.id.floatingActionButton);
       // checkbox=(CheckBox) findViewById(R.id.contactcheck);
        checkAndRequestPermissions();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            getPermissionToReadSMS();
        }
        else
        {
            //refreshSmsInbox();
            smsEncListAdapter =new SMSEncListAdapter(this,cursor);
        }


        //secound page Edit text

        cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        smsEncListAdapter = new SMSEncListAdapter(this, cursor);
        messages.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        messages.setAdapter(smsEncListAdapter);
        messages.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                {
                    // TODO Auto-generated method stub
                    TextView textViewSMSSender = (TextView) arg1.findViewById(R.id.textViewSMSSender);
                    TextView textViewSMSBody = (TextView) arg1.findViewById(R.id.textViewMessageBody);
                    smsSender = textViewSMSSender.getText().toString();
                    smsBody = textViewSMSBody.getText().toString();
                    String check=smsBody.substring(smsBody.length() - 3);
                    if(check.equals("000"))
                    {
                        keyfinal = smsSender.substring(smsSender.length() - 8);
                        String duplicate=smsBody.substring(0, smsBody.length() - 3);
                        text = Main2Activity.decrypt(duplicate,  privatekey+keyfinal);
                    }
                    else
                    {
                        text=smsBody;
                    }
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setTitle(smsSender);
                    dialog.setIcon(android.R.drawable.ic_dialog_info);
                    dialog.setMessage(text);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Reply",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    chatmsg =text;
                                    chatno = smsSender;
                                    Intent j = new Intent(MainActivity.this, ReplyActivity.class);
                                    startActivity(j);

                                }
                            });

                    dialog.show();
                   // addNotification();
                    //ref :https://www.tutorialspoint.com/android/android_notifications.htm

                }
            });


            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    message.setText("Use FingerPrint");
                    fph.doAuth(fingerprintManager, cryptoObject);
                    message.setText("click again");
                    if (value == 1) {
                        DecryptedInbox();
                        value=0;
                    }

                }
            });
        donate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.i("MainActivity", "Button Pushed");
                //Utility.setDefaultSmsApp(MainActivity.this);
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
               // intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivity(intent);
            }
        });




    }



    public void DecryptedInbox()
    {

       cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        smsListAdapter  =new SMSListAdapter(this,cursor);
        messages.setAdapter(smsListAdapter);

    }

    public void addNotification()
    {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.image)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private boolean checkFinger() {

        // Keyguard Manager
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        // Fingerprint Manager
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        try {
            // Check if the fingerprint sensor is present
            if (!fingerprintManager.isHardwareDetected()) {
                message.setText("Fingerprint authentication not supported");
                return false;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                message.setText("No fingerprint configured.");
                return false;
            }

            if (!keyguardManager.isKeyguardSecure()) {
                message.setText("Secure lock screen not enabled");
                return false;
            }

        }
        catch(SecurityException se) {
            se.printStackTrace();
        }


        return true;

    }

    private void generateKey() throws FingerprintException {
        try {
            // Get the reference to the key store
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            // Key generator to generate the key
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init( new KeyGenParameterSpec.Builder(KEY_NAME,KeyProperties.PURPOSE_ENCRYPT |KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());

            keyGenerator.generateKey();

        }
        catch(KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }


    }

    private Cipher generateCipher() throws FingerprintException {
        try {
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        }
        catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | UnrecoverableKeyException
                | KeyStoreException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    
    public void OnSendClick(View view)
    {



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            getPermissionToReadSMS();
        }

       Intent i = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(i);


    }

    public boolean checkAndRequestPermissions()
    {
        int permissionSMS = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS);
        int fingerprintPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.USE_FINGERPRINT);
        int phonestatePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (fingerprintPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.USE_FINGERPRINT);
        }
        if (permissionSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (phonestatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
            return false;
        }

        return true;
    }




    //  private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public void getPermissionToReadSMS()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
            {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},READ_SMS_PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults)
    {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
               // refreshSmsInbox();
            }
            else
            {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private class FingerprintException extends Exception {

        public FingerprintException(Exception e) {
            super(e);
        }
    }

    private static MainActivity inst;
    public static MainActivity instance()
    {
        return inst;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        inst = this;
    }
}