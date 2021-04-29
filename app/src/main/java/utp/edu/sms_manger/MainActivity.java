package utp.edu.sms_manger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ContactRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "CHANNEL_1";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth;

    private SmsManager smsManager;
    private BroadcastReceiver broadcastReceiver;

    private EditText phoneNumberEditText;
    private EditText smsEditText;
    private Button sendButton;
    private TextView azimuthTextView;
    private TextView smsReceivedTextView;

    private static final int REQUEST_CODE = 1;
    private String receivedMessages = "SMS:\n";

    private ContactRecyclerViewAdapter adapter;
    private static boolean initContactListFlag = false;
    private static List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        smsEditText = findViewById(R.id.sms_text_edit_text);
        smsReceivedTextView = findViewById(R.id.sms_received_text_view);
        sendButton = findViewById(R.id.send_button);
        azimuthTextView = findViewById(R.id.azimuth_text_view);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        smsManager = SmsManager.getDefault();
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "No permission to send or receive SMS");
            String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS};
            requestPermissions(permissions, REQUEST_CODE);
        } else {
            initAfterPermissionsGranted();
        }

        smsReceivedTextView.setText(receivedMessages);
        sendButton.setEnabled(false);
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled((!phoneNumberEditText.getText().toString().trim().isEmpty())
                        && (!smsEditText.getText().toString().trim().isEmpty()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        smsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled((!phoneNumberEditText.getText().toString().trim().isEmpty())
                        && (!smsEditText.getText().toString().trim().isEmpty()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RecyclerView recyclerView = findViewById(R.id.contacts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new ContactRecyclerViewAdapter(this, contacts);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        createChannel();
    }

    private void displaySMS(String number, String message) {
        String date = getDate();

        String text = number + "\n" + date + "\n" + message;
        receivedMessages += text + "\n\n";
        smsReceivedTextView.setText(receivedMessages);

        displayNotification("New message", text);
    }

    private String getDate() {
        DateFormat formatter = DateFormat.getDateTimeInstance();

        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }

    public void sendSMS(View view) {
        String destinationAddress = phoneNumberEditText.getText().toString();
        String text = smsEditText.getText().toString();
        text += "\nAzimuth: " + azimuthToDegrees(azimuth);

        smsManager.sendTextMessage(destinationAddress, null, text, null, null);
        Toast.makeText(MainActivity.this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        smsEditText.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver, filter);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];
                azimuthTextView.setText(String.format("Azimuth: %s", azimuthToDegrees(azimuth)));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Accuracy changed");
    }


    public float azimuthToDegrees(float azimuthInRadians) {
        float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
        if (azimuthInDegrees < 0.0f) {
            azimuthInDegrees += 360.0f;
        }
        return azimuthInDegrees;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("messages", receivedMessages);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Optional<String> messages = Optional.ofNullable(savedInstanceState.getString("messages"));
        messages.ifPresent(s -> {
            receivedMessages = s;
            smsReceivedTextView.setText(receivedMessages);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAfterPermissionsGranted();
            } else {
                Log.e(TAG, "Permissions denied");
            }
        }
    }

    private void initAfterPermissionsGranted() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        broadcastReceiver = new BroadcastReceiver() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceive(Context context, Intent intent) {
                final String pdu_type = "pdus";

                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String format = bundle.getString("format");
                Object[] pdus = (Object[]) bundle.get(pdu_type);
                if (pdus != null) {
                    boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        if (isVersionM) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                        Toast.makeText(context, "New SMS", Toast.LENGTH_LONG).show();
                        displaySMS(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);

        if (!initContactListFlag) {
            contacts = getContactList();
            initContactListFlag = true;
        }

    }

    private List<Contact> getContactList() {
        List<Contact> contacts = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Contact contact = new Contact(Long.parseLong(id), name);

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        contact.setNumber(phoneNo);
                    }
                    pCur.close();
                }
                contacts.add(contact);
            }
        }
        if (cur != null) {
            cur.close();
        }
        contacts.sort(Comparator.comparing(Contact::getName));
        return contacts;
    }

    @Override
    public void onItemClick(View view, int position) {
        Contact contact = adapter.getItem(position);
        Toast.makeText(this, "You selected " + contact.getName() + " with number: " + contact.getNumber(), Toast.LENGTH_SHORT).show();
        phoneNumberEditText.setText(contact.getNumber());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void displayNotification(String title, String contentText) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

}
