package com.pes.fibness;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowersActivity extends AppCompatActivity implements UsersAdapter.SelectedUser{

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private List<UserModel> userModelList = new ArrayList<>();
    private ArrayList<Pair<Integer, String>> names = new ArrayList<>();
    private UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerview);

        ArrayList<Pair<Integer,String>> users = User.getInstance().getUserFollowers();
        names = users;

        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));



        for(int i=0; i < names.size(); ++i){
            UserModel userModel = new UserModel(names.get(i).first, names.get(i).second, false);
            userModelList.add(userModel);
        }
        Collections.sort(userModelList);

        usersAdapter = new UsersAdapter(userModelList, this);
        recyclerView.setAdapter(usersAdapter);

    }


    @Override
    public void selectedUser(final UserModel userModel) {
        /**hay que cargar los datos del usuario seleccionado*/
        String route = "http://10.4.41.146:3001/user/"+userModel.getId()+"/info/" + User.getInstance().getId();
        ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), route);
        connetionAPI.getSelectedUserInfo();

        @SuppressLint("HandlerLeak") Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent i = new Intent().setClass(FollowersActivity.this, SelectedUserActivity.class).putExtra("data", userModel);
                i.putExtra("name", "FollowersActivity");
                startActivity(i);
            }
        };
        h.sendEmptyMessageDelayed(0, 100);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_view);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                usersAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }


}



