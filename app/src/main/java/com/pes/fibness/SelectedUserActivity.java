package com.pes.fibness;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class SelectedUserActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private ImageView backImgButton, blockImgButton, ivUser;
    private TextView nFollowers, nFollowing, username,coma, age, country, description;
    private FloatingActionButton follow;
    private UserModel userModel;
    private UsersInfo ui = User.getInstance().getSelectedUser();
    private Boolean ImFolloing = ui.follow; /* ui.follow necesito por si el usuario en la misma pagina quiere seguir y dejar de seguir*/
    private int n = ui.nFollower;
    private Boolean bkUser= ui.blocked;

    private Button btn_training, btn_diets, btn_routes;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_user);

        backImgButton = findViewById(R.id.backImgButton);
        blockImgButton = findViewById(R.id.blockImgButton);
        ivUser = findViewById(R.id.iv_user);
        nFollowers = findViewById(R.id.nFollowers);
        nFollowing = findViewById(R.id.nFollowing);
        username = findViewById(R.id.username);
        coma = findViewById(R.id.coma);
        age = findViewById(R.id.age);
        country = findViewById(R.id.country);
        description = findViewById(R.id.description);
        follow = findViewById(R.id.follow);

        btn_training = findViewById(R.id.btn_training);
        btn_diets = findViewById(R.id.btn_diets);
        btn_routes = findViewById(R.id.btn_routes);


        showUserInfo();


        Intent intent = getIntent();
        if(intent.getExtras() != null){
            userModel = (UserModel) intent.getSerializableExtra("data");
        }



        backImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(getIntent().getStringExtra("name").equals("SearchUserActivity")){
                    System.out.println("onBackPressed----1");
                    intent = new Intent(SelectedUserActivity.this, SearchUsersActivity.class);
                }
                else if(getIntent().getStringExtra("name").equals("FollowersActivity")) {
                    System.out.println("onBackPressed----2");
                    intent = new Intent(SelectedUserActivity.this, FollowersActivity.class);
                }
                else{
                    /*hay que cargar otravez*/
                    intent = new Intent(SelectedUserActivity.this, FollowingActivity.class);
                }
                startActivity(intent);

            }
        });





        /*follow button*/
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("My backgroud: " + view.getBackgroundTintList());
                if(!ImFolloing){
                    //follow user
                    if(!bkUser){
                        ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/follow");
                        connetionAPI.followUser(User.getInstance().getId(), userModel.getId());
                        follow.setBackgroundTintList(ColorStateList.valueOf(-2818048)); //-2818048 = red color
                        ++n;
                        nFollowers.setText(""+n);
                        ImFolloing = true;
                    }
                    else Toast.makeText(getApplicationContext(), "You cannot follow a blocked user", Toast.LENGTH_LONG).show();

                }
                else{
                    //delete follow
                    if(!bkUser){
                        ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/follow/" + User.getInstance().getId() + "/" + userModel.getId());
                        connetionAPI.deleteFollowing();

                        view.setBackgroundTintList(ColorStateList.valueOf(-16021062)); //-16021062 = @color/c_icon_bkg_unsel
                        --n;
                        if(n < 0) n=0;
                        nFollowers.setText(""+n);
                        ImFolloing = false;
                    }
                    else Toast.makeText(getApplicationContext(), "You cannot follow a blocked user", Toast.LENGTH_LONG).show();

                }

            }
        });



        btn_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**cargo entrenamientos*/
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/" + userModel.getId() + "/trainings");
                c.getUserTrainings();

                @SuppressLint("HandlerLeak") Handler h = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Intent i = new Intent().setClass(SelectedUserActivity.this, ShowTrainingActivity.class).putExtra("data", userModel);
                        i.putExtra("name", "SelectedUserActivity");
                        startActivity(i);
                    }
                };
                h.sendEmptyMessageDelayed(0, 200);



            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showUserInfo() {

        if(ImFolloing){
            follow.setBackgroundTintList(ColorStateList.valueOf(-2818048)); //-2818048 = red color
        }
        else{
            System.out.println("NADA");;
            follow.setBackgroundTintList(ColorStateList.valueOf(-16021062)); //-16021062 = @color/c_icon_bkg_unsel
        }


        if(ui.nFollower < 0)
            nFollowers.setText("0");
        else nFollowers.setText(""+ ui.nFollower);
        if(ui.nFollowing < 0)
            nFollowing.setText("0");
        else nFollowing.setText(""+ui.nFollowing);
        /*image*/
        boolean validImage = false;
        byte[] userImage = null;
        if (ui.image != null) {
            validImage = true;
            userImage = ui.image;
        }
        if (validImage) {
            Glide.with(SelectedUserActivity.this)
                    .load(userImage)
                    .centerCrop()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivUser);
        }

        username.setText(ui.username);
        if(ui.birthDate.equals("null"))
            coma.setText("");
        else {
            if(ui.sAge)
                age.setText(howManyYears(ui.birthDate));
            else coma.setText("");
        }
        String[] planets = getResources().getStringArray(R.array.countries);
        if(ui.country.equals("null"))
            country.setText("");
        else country.setText(planets[Integer.parseInt(ui.country)]);
        if(ui.description.equals("null"))
            description.setText("");
        else description.setText(ui.description);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String howManyYears(String birthDate){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaNac = LocalDate.parse(birthDate, fmt);
        LocalDate ahora = LocalDate.now();

        Period periodo = Period.between(fechaNac, ahora);
        System.out.printf("Tu edad es: %s años, %s meses y %s días",
                periodo.getYears(), periodo.getMonths(), periodo.getDays());
        return String.valueOf(periodo.getYears());
    }




    /*blockImgButton onClick*/
    public  void showPopup(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_block_menu);
        System.out.println("my usermodel block: " + userModel.getBlocked());
        if(bkUser)
            popupMenu.getMenu().getItem(0).setTitle(getResources().getString(R.string.unlockUser));
        else popupMenu.getMenu().getItem(0).setTitle(getResources().getString(R.string.blockUser));
        popupMenu.show();
    }



    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.bk_item){
            String s = (String) menuItem.getTitle();
            if(s.equals("Block user") || s.equals("Bloquear usuario")){
                showBlockMessage();
                if(bkUser) menuItem.setTitle(getResources().getString(R.string.unlockUser));
            }
            else{
                showUnlockMessage();
                if(!bkUser) menuItem.setTitle(getResources().getString(R.string.blockUser));
            }

            return true;
        }
        return false;
    }


    private void showBlockMessage() {
        AlertDialog.Builder message = new AlertDialog.Builder(this);
        message.setTitle(getResources().getString(R.string.bkUser));
        message.setMessage(getResources().getString(R.string.bkMsg))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/block");
                        connetionAPI.blockUser(User.getInstance().getId(), userModel.getId());
                        if(ImFolloing){
                            follow.setBackgroundTintList(ColorStateList.valueOf(-16021062));
                            --n;
                            if(n < 0) n=0;
                            nFollowers.setText(""+n);
                            ImFolloing = false;
                        }

                        bkUser = true;
                        System.out.println("my usermodel block true: " + userModel.getBlocked());
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = message.create();
        alertDialog.show();

    }

    private void showUnlockMessage() {
        AlertDialog.Builder message = new AlertDialog.Builder(this);
        message.setTitle(getResources().getString(R.string.unlockUser));
        message.setMessage(getResources().getString(R.string.unlockMsg))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/block/"+ User.getInstance().getId() +"/"+userModel.getId());
                        connetionAPI.unlockkUser();
                        bkUser = false;
                        System.out.println("my usermodel block false: " + userModel.getBlocked());
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = message.create();
        alertDialog.show();

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }








}
