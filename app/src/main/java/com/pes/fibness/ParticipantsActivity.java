package com.pes.fibness;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ParticipantsActivity extends AppCompatActivity {

    AdapterUsers adapter;
    RecyclerView recycler;
    ArrayList<UserShortInfo> participants = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_participants);

        participants = User.getInstance().getParticipantsList();

        recycler = findViewById(R.id.recyclerview);
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new AdapterUsers(participants);
        recycler.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.ViewHolderUsers> {

    private ArrayList<UserShortInfo> listaUsers;

    public AdapterUsers(ArrayList<UserShortInfo> listaUsers) {
        this.listaUsers = listaUsers;
    }

    @NotNull
    @Override
    public ViewHolderUsers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_participants, null, false);
        return new ViewHolderUsers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderUsers holder, int position) {
        holder.assignUsers(listaUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return listaUsers.size();
    }


    static class ViewHolderUsers extends RecyclerView.ViewHolder {

        TextView username;
        TextView prefix;

        public ViewHolderUsers(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.usernameP);
            prefix = itemView.findViewById(R.id.prefixP);
        }


        public void assignUsers(UserShortInfo u) {
            username.setText(u.username);
            prefix.setText(u.username.substring(0, 1));
        }
    }
}