package com.pes.fibness;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.HistoryRoomListener;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;

import androidx.annotation.Nullable;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;
import com.scaledrone.lib.SubscribeOptions;

import java.util.Random;

public class ChatActivity extends AppCompatActivity implements RoomListener  {

    private String channelID;
    private String roomName = "observable-room";
    private EditText editText;
    private Scaledrone scaledrone;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private String User_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        editText =  (EditText) findViewById(R.id.editText);

        getExtras();
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messageView);
        messagesView.setAdapter(messageAdapter);

        MemberData data = new MemberData(User_name, getRandomColor());

        scaledrone = new Scaledrone(channelID, data);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("Scaledrone connection open");
                Room room = scaledrone.subscribe(roomName, ChatActivity.this, new SubscribeOptions(100));
                room.listenToHistoryEvents(new HistoryRoomListener() {
                    @Override
                    public void onHistoryMessage(Room room, com.scaledrone.lib.Message message) {
                        System.out.println("Received a message from the past " + message.getData().asText());
                        final ObjectMapper mapper = new ObjectMapper();
                        MemberData data = new MemberData(getString(R.string.Previously), "red");
                        final Message mes = new Message(message.getData().asText(), data, false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageAdapter.add(mes);
                                messagesView.setSelection(messagesView.getCount() - 1);
                            }
                        });
                    }
                });
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println("On open failure " + ex);
            }

            @Override
            public void onFailure(Exception ex) {
                System.err.println("onFailure " + ex);
            }

            @Override
            public void onClosed(String reason) {
                System.err.println("OnClosed " + reason);
            }
        });
    }

    public void sendMessage(View view) {
        String message = editText.getText().toString();
        if (message.length() > 0) {
            scaledrone.publish(roomName, message);
            editText.getText().clear();
        }
    }

    @Override
    public void onOpen(Room room) {
        System.out.println("Conneted to room");
    }

    @Override
    public void onOpenFailure(Room room, Exception ex) {
        System.err.println(ex);
    }

    @Override
    public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MemberData data = mapper.treeToValue(receivedMessage.getMember().getClientData(), MemberData.class);
            boolean belongsToCurrentUser = receivedMessage.getClientID().equals(scaledrone.getClientID());
            final Message message = new Message(receivedMessage.getData().asText(), data, belongsToCurrentUser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

        private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    private void getExtras(){
        Bundle extras = getIntent().getExtras();
        User_name = extras.getString("name");
        channelID = extras.getString("channelID");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}