package com.example.jeffemuveyan.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeffemuveyan.Classes.Message;
import com.example.jeffemuveyan.HelperClasses.CustomAdapter;
import com.iitr.kaishu.nsidedprogressbar.NSidedProgressBar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private Button sendButton;
    private EditText messageEditText;
    private TextView connectionTextView;
    private ListView listView;
    private NSidedProgressBar nSidedProgressBar;

    private static ArrayList<Message> arrayOfListViewElement = new ArrayList<Message>();
    private static CustomAdapter adapter;
    private final String userSignature = String.valueOf(Math.random());// We will use this to mark each message this user sends so that we can identify who sent which message.

    private AlertDialog.Builder alert;

    private static Context c;
    private String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button)findViewById(R.id.button);
        messageEditText = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.listview);
        connectionTextView = (TextView)findViewById(R.id.connection);
        nSidedProgressBar = (NSidedProgressBar)findViewById(R.id.progress);

        setTitle("ChatApp by Jeff Emuveyan");

        //Get userID from previous activity (this ID will be used as the topic)
        Intent i = getIntent();
        topic =  i.getStringExtra("userID");//we use the user's ID as the topic.


        //disable the views until there is a connection
        sendButton.setEnabled(false);
        messageEditText.setEnabled(false);

        alert = new AlertDialog.Builder(this);

        c = MainActivity.this;

        //connect to HiveMQ
        connect();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        disconnect();
    }

    public void connect(){
        connectionTextView.setText("Connecting...");
        Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_LONG).show();

        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();

                    //Now subscribe to a topic
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    onErrorEvent("Connection failed", "Failed to connect");

                }
            });
        } catch (MqttException e) {
            onErrorEvent("Connection failed", "Failed to connect");
            e.printStackTrace();
        }
    }


    public void subscribe(){
        //Toast.makeText(MainActivity.this, "Subscribing...", Toast.LENGTH_LONG).show();

        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);// we are subscribing to the topic which is the user's ID.
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText(MainActivity.this, "Connection successful! Topic: "+topic, Toast.LENGTH_LONG).show();
                    //now that we know the user has subscribed, we can let him send messages
                    connectionTextView.setText("Connected");
                    connectionTextView.setTextColor(Color.parseColor("#00EB00"));//green color
                    sendButton.setEnabled(true);
                    messageEditText.setEnabled(true);
                    nSidedProgressBar.setVisibility(View.INVISIBLE);

                    //and also allow him listen for messages
                    listenForMessage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    onErrorEvent("Connection failed", "The subscription could not be performed...");
                }
            });
        } catch (MqttException e) {
            onErrorEvent("Connection failed", "The subscription could not be performed...");
            e.printStackTrace();
        }
    }


    public void listenForMessage() {

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                onErrorEvent("Connection lost", "Sorry, we have lost connection");
                sendButton.setEnabled(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Once the message comes:
                nSidedProgressBar.setVisibility(View.INVISIBLE);
                sendButton.setEnabled(true);
                messageEditText.setText("");

                //for sound
                try {
                    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                    r.play();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Notification sound won't play on this type of android device...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                //get the date/time
                Date date = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("E hh.mm.ss");
                String theDate = String.valueOf(ft.format(date));

                //lets check if its our own message or our friends message:
                Message textMessage;

                if(message.toString().trim().contains(userSignature)){//if this message contains our signature:

                    textMessage = new Message(seperateMessageFromSignature(message.toString()), "You", theDate);
                }else{
                    textMessage = new Message(seperateMessageFromSignature(message.toString()), "Friend", theDate);
                }

                //add it to the listview
                arrayOfListViewElement.add(textMessage);

                adapter = new CustomAdapter(MainActivity.this, arrayOfListViewElement);

                listView.setAdapter(adapter);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //this method is called first before 'messageArrived()' method
                Toast.makeText(MainActivity.this, "Message has been delivered", Toast.LENGTH_LONG).show();

            }
        });
    }


    public void sendMessage(){

        nSidedProgressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        String userMessage = messageEditText.getText().toString().trim();
        String payload = "<"+userMessage+">"+userSignature;//get the message, enclose it in <> so that we can easily seprate it from the signature later (attach the signature to it.

        if(userMessage == null || TextUtils.isEmpty(userMessage)){
            Toast.makeText(MainActivity.this, "Message cannot be empty...", Toast.LENGTH_SHORT).show();
            sendButton.setEnabled(true);
        }else{
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                Toast.makeText(MainActivity.this, "Message not sent", Toast.LENGTH_LONG).show();
                sendButton.setEnabled(true);
                e.printStackTrace();
            }
        }

    }


    private String seperateMessageFromSignature(String receivedMessage){
        //our aim here is to remove the signature from the raw message:

        Pattern pattern = Pattern.compile("\\<(.*?)\\>");
        //That code will simply search the HTML, extract anything in between '<' and '>'

        Matcher matcher = pattern.matcher(receivedMessage);

        String message = null;

        while(matcher.find()){
            message = matcher.group(1).trim(); //remove any leading or trailing space
        }

        return message;
    }

    private void onErrorEvent(String errorTitle, String errorMessage){
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        connectionTextView.setText(errorTitle);
        connectionTextView.setTextColor(Color.parseColor("#F40000"));//red color
        nSidedProgressBar.setVisibility(View.INVISIBLE);

    }


    private void disconnect(){
        //disconnect anytime the user closes the app:

        //first unsubscribe
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        //disconnect
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMessage(int position){
        arrayOfListViewElement.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(c, "Deleted", Toast.LENGTH_LONG).show();
    }

}
