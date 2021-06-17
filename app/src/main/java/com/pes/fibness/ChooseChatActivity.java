package com.pes.fibness;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class ChooseChatActivity extends AppCompatActivity {

    private ListView chatList;
    private ArrayList<String> listaChat;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        listaChat = User.getInstance().getChatsName();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatList = (ListView) findViewById(R.id.ListChat);

        refreshList();

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @SuppressLint("ResourceType")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String channelID = User.getInstance().getChannelID(position);
                String userName = User.getInstance().getName();
                Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                chat.putExtra("name", userName);
                chat.putExtra("channelID", channelID);
                startActivity(chat);

            }
        } );

    }


    private void refreshList() {
        chatList.setAdapter(new ArrayAdapter<String>(this, R.layout.row, listaChat));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
