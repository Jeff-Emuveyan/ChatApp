package com.chatapp_source.jeffemuveyan.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button joinMeButton = (Button)findViewById(R.id.join_me);
        Button createNewChatButton = (Button)findViewById(R.id.create_new);

        setTitle("ChatApp by Jeff Emuveyan");


        joinMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlert("Join me", "To join my chat, enter my userID in the field below", "Connect");

            }
        });


        createNewChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlert("Create a new chat", "To create a new chat group, simply enter your userID below", "Create");

            }
        });


    }


    private void showAlert(String title, String message, String positiveButtonMessage){

        LayoutInflater layout = LayoutInflater.from(WelcomeActivity.this);

        View view = layout.inflate(R.layout.alert,null);

        AlertDialog.Builder alert = new AlertDialog.Builder(WelcomeActivity.this);

        alert.setView(view);

        final EditText editTextUserId = (EditText)view.findViewById(R.id.userid);
        TextView titleTextView = (TextView)view.findViewById(R.id.title_textview);
        TextView messageTextView = (TextView)view.findViewById(R.id.message_textview);

        titleTextView.setText(title);
        messageTextView.setText(message);

        alert.setPositiveButton(positiveButtonMessage, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub

                String userId = editTextUserId.getText().toString();

                if(userId == null || TextUtils.isEmpty(userId)){
                    Toast.makeText(WelcomeActivity.this, "Field cannot be empty", Toast.LENGTH_LONG).show();
                }else{
                    //send the userID to the next activity to register it as a chat topic
                    Intent i = new Intent(WelcomeActivity.this,MainActivity.class);
                    i.putExtra("userID", userId );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.cancel();
            }
        });

        //create an alert
        AlertDialog a = alert.create();
        a.show();

    }



}
