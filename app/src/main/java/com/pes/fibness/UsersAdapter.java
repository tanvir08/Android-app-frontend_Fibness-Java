package com.pes.fibness;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersAdapterVh> implements Filterable {

    private List<UserModel> userModelList;
    private Context context;
    private SelectedUser selectedUser;
    private List<UserModel> userModelListFiltered;

    public UsersAdapter(List<UserModel> userModelList, SelectedUser selectedUser) {
        this.userModelList = userModelList;
        this.selectedUser = selectedUser;
        this.userModelListFiltered = userModelList;
    }


    @NonNull
    @Override
    public UsersAdapter.UsersAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new UsersAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_users, null));
    }
    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersAdapterVh holder, int position) {
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


    /*Search filter*/
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



    public class UsersAdapterVh extends RecyclerView.ViewHolder {
        TextView tvPrefix;
        TextView tvUsername;
        ImageView imgIcon;
        public UsersAdapterVh(@NonNull View itemView) {
            super(itemView);
            tvPrefix = itemView.findViewById(R.id.prefix);
            tvUsername = itemView.findViewById(R.id.username);
            imgIcon = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedUser.selectedUser(userModelList.get(getAdapterPosition()));
                }
            });



        }
    }
}
