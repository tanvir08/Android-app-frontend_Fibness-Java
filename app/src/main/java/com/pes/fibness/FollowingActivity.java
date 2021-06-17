package com.pes.fibness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowingActivity extends AppCompatActivity implements FollowingAdapter.SelectedUser{


    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private List<UserModel> userModelList = new ArrayList<>();
    private ArrayList<Pair<Integer, String>> names = new ArrayList<>();
    private FollowingAdapter followingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerview);

        ArrayList<Pair<Integer,String>> users = User.getInstance().getUserFollowing();
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

        followingAdapter = new FollowingAdapter(userModelList, this);
        recyclerView.setAdapter(followingAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        names = User.getInstance().getUserFollowing();
        followingAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(followingAdapter);

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
                Intent i = new Intent().setClass(FollowingActivity.this, SelectedUserActivity.class).putExtra("data", userModel);
                i.putExtra("name", "FollowingActivity");
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
                followingAdapter.getFilter().filter(newText);
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


class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.FollowingAdapterVh> implements Filterable {

    private List<UserModel> userModelList;
    private Context context;
    private FollowingAdapter.SelectedUser selectedUser;
    private List<UserModel> userModelListFiltered;
    private Boolean first = false;

    public FollowingAdapter(List<UserModel> userModelList, FollowingAdapter.SelectedUser selectedUser) {
        this.userModelList = userModelList;
        this.selectedUser = selectedUser;
        this.userModelListFiltered = userModelList;
    }


    @NonNull
    @Override
    public FollowingAdapter.FollowingAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new FollowingAdapter.FollowingAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_following, null));
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingAdapter.FollowingAdapterVh holder, int position) {
        UserModel userModel = userModelList.get(position);
        String username = userModel.getUsername();
        String prefix = userModel.getUsername().substring(0,1);

        holder.tvUsername.setText(username);
        holder.tvPrefix.setText(prefix);

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if(charSequence == null | charSequence.length() == 0){
                    filterResults.count = userModelListFiltered.size();
                    filterResults.values = userModelListFiltered;
                }
                else{
                    String searchChar = charSequence.toString().toLowerCase();
                    List<UserModel> resultData = new ArrayList<>();

                    for(UserModel userModel: userModelListFiltered){
                        if(userModel.getUsername().toLowerCase().contains(searchChar)){
                            resultData.add(userModel);
                        }
                    }

                    filterResults.count = resultData.size();
                    filterResults.values = resultData;


                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                userModelList = (List<UserModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }



    public interface SelectedUser{
        public void selectedUser(UserModel userModel);
    }



    public class FollowingAdapterVh extends RecyclerView.ViewHolder {
        TextView tvPrefix;
        TextView tvUsername;
        Button btn_following;
        public FollowingAdapterVh(@NonNull View itemView) {
            super(itemView);
            tvPrefix = itemView.findViewById(R.id.prefix);
            tvUsername = itemView.findViewById(R.id.username);
            btn_following = itemView.findViewById(R.id.btn_following);

            tvPrefix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedUser.selectedUser(userModelList.get(getAdapterPosition()));
                }
            });
            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedUser.selectedUser(userModelList.get(getAdapterPosition()));
                }
            });

            btn_following.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("my color: " + view.getBackgroundTintList());
                    if(view.getBackgroundTintList() == null || first){
                        first = false;
                        System.out.println("elimiar id: " + userModelList.get(getAdapterPosition()).getId());
                        ConnetionAPI connetionAPI = new ConnetionAPI(context, "http://10.4.41.146:3001/user/follow/" + User.getInstance().getId() + "/" + userModelList.get(getAdapterPosition()).getId());
                        connetionAPI.deleteFollowing();

                        view.setBackgroundTintList(ColorStateList.valueOf(-16021062));
                        btn_following.setText(context.getResources().getText(R.string.Follow));
                        btn_following.setTextColor(context.getResources().getColor(R.color.white));
                        System.out.println("my color2222: " + view.getBackgroundTintList());
                    }
                    else{
                        ConnetionAPI connetionAPI = new ConnetionAPI(context, "http://10.4.41.146:3001/user/follow");
                        connetionAPI.followUser(User.getInstance().getId(), userModelList.get(getAdapterPosition()).getId());

                        view.setBackgroundTintList(ColorStateList.valueOf(0));
                        btn_following.setText(context.getResources().getText(R.string.Following));
                        btn_following.setTextColor(context.getResources().getColor(R.color.app_bkg_color));
                        first = true;
                    }
                }
            });


        }
    }
}