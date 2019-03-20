package com.example.jeffemuveyan.HelperClasses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jeffemuveyan.Classes.Message;
import com.example.jeffemuveyan.chatapp.MainActivity;
import com.example.jeffemuveyan.chatapp.R;
import com.kinda.postui.PostMessageView;

import java.util.ArrayList;

public class CustomAdapter  extends BaseAdapter {

    Context c;
    ArrayList<Message> arrayOfListViewElement;

    LayoutInflater inflater;


    public CustomAdapter(Context c, ArrayList<Message> arrayOfListViewElement) {
        super();
        this.c = c;
        this.arrayOfListViewElement = arrayOfListViewElement;

        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return arrayOfListViewElement.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub

        return arrayOfListViewElement.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.single_message_layout, parent, false);
        }

        PostMessageView postMessageView = (PostMessageView)convertView.findViewById(R.id.holder);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.message);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.time);
        TextView usernameTextView = (TextView) convertView.findViewById(R.id.username);


        Message message = arrayOfListViewElement.get(position);

        if(message.getUserName().equals("You")){//if this is our own message:
            //change the color of the holder
            postMessageView.setBackgroundColor(Color.parseColor("#80DEEA"), Color.parseColor("#E0F7FA"));
        }

        //relativeLayout.setId(listViewElement.getId());
        messageTextView.setText(message.getMessage());
        timeTextView.setText(message.getTime());
        usernameTextView.setText(message.getUserName());

        postMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(c);
                alert.setTitle("Delete Message?");
                alert.setMessage("This message will be removed from your chat list");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        MainActivity.deleteMessage(position);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                    }
                });

                alert.show();
            }
        });

        return convertView;
    }

}
