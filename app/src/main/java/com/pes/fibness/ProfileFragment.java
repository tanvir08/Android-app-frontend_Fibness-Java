package com.pes.fibness;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import static com.pes.fibness.R.id.iv_user;

public class ProfileFragment extends Fragment {

    private TextView username, chat, users;
    private User u = User.getInstance();
    ImageView ivUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_perfil, container, false);
        Context thiscontext = container.getContext();

        username = root.findViewById(R.id.username);
        chat = root.findViewById(R.id.chat);
        users = root.findViewById(R.id.userModels);
        ivUser = root.findViewById(iv_user);


        showUserInfo();


        ImageView imgSettings = (ImageView) root.findViewById(R.id.setting);
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);

            }
        });

        ImageView editButton = (ImageView) root.findViewById(R.id.editProfile);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);

            }
        });

        ImageView imgViewProfile = (ImageView) root.findViewById(R.id.viewProfile);
        imgViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User u = User.getInstance();
                ConnetionAPI connetionAPI = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/user/"+ u.getId() + "/statistics");
                connetionAPI.getStatistics();

                @SuppressLint("HandlerLeak") Handler h = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {

                        Intent i = new Intent(getActivity(), ViewProfileActivity.class);
                        startActivity(i);
                    }
                };
                h.sendEmptyMessageDelayed(0, 100);


            }
        });


        Dialog dialog = new Dialog(thiscontext);
        ImageView imgAchievements = (ImageView) root.findViewById(R.id.achievements);
        imgAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User u = User.getInstance();
                ConnetionAPI connetionAPI = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/user/"+ u.getId() + "/globaldst");
                connetionAPI.getTotalDst();

                @SuppressLint("HandlerLeak") Handler h = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {

                        Intent i = new Intent(getActivity(), AchievementsActivity.class);
                        startActivity(i);
                    }
                };
                h.sendEmptyMessageDelayed(0, 100);





            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatList = new Intent(getActivity(), ChooseChatActivity.class);
                startActivity(chatList);
            }
        });

        /*load user info (id,username)*/
        users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User u = User.getInstance();
                ConnetionAPI connetionAPI = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/user/shortInfo/"+ u.getId());
                connetionAPI.getShortUserInfo(u.getId());

                @SuppressLint("HandlerLeak") Handler h = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {

                        Intent i = new Intent().setClass(getActivity(), SearchUsersActivity.class);
                        startActivity(i);
                    }
                };
                h.sendEmptyMessageDelayed(0, 200);

            }
        });



        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        showUserInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivUser.setImageBitmap(null); ivUser.destroyDrawingCache();
    }

    private void showUserInfo() {
        boolean validImage = false;
        byte[] userImage = null;
        if (u.getImage() != null) {
            validImage = true;
            userImage = u.getImage();
        }
        username.setText(u.getName());
        if (validImage) {
            Glide.with(ProfileFragment.this)
                    .load(userImage)
                    .centerCrop()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .into(ivUser);
        }
    }

}