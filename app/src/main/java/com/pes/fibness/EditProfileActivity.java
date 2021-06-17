package com.pes.fibness;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.pes.fibness.R.id.backImgButton;
import static com.pes.fibness.R.id.confirm;
import static com.pes.fibness.R.id.et_Description;
import static com.pes.fibness.R.id.et_Name;
import static com.pes.fibness.R.id.et_mostrar_fecha_picker;
import static com.pes.fibness.R.id.ib_obtener_fecha;
import static com.pes.fibness.R.id.iv_user;
import static com.pes.fibness.R.id.s_Country;
import static com.pes.fibness.R.id.tB_fem;
import static com.pes.fibness.R.id.tb_male;

public class EditProfileActivity extends AppCompatActivity {
    User u = User.getInstance();

    /* Calendar */
    private static final String zero = "0";
    private static final String slash = "/";

    public final Calendar c = Calendar.getInstance();

    final int month = c.get(Calendar.MONTH);
    final int day = c.get(Calendar.DAY_OF_MONTH);
    final int year = c.get(Calendar.YEAR);

    /* Profile picture */
    public static final int REQUEST_CODE_CAMERA = 0012;
    public static final int REQUEST_CODE_GALLERY = 0013;
    ImageView ivUser;
    private String [] items = {"Camera","Gallery"};


    /* Widgets */
    Spinner sCountry;
    EditText etDate, etName, etDescription;
    ImageButton ibDateGetter;
    ToggleButton tbFem;
    ToggleButton tbMale;
    private EasyImage easyImage;

    //Object to take pictures

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(et_Name);
        etDescription = findViewById(et_Description);
        sCountry = findViewById(s_Country);
        etDate = findViewById(et_mostrar_fecha_picker);
        ibDateGetter = findViewById(ib_obtener_fecha);
        tbFem = findViewById(tB_fem);
        tbMale = findViewById(tb_male);
        ivUser = findViewById(iv_user);


        showUserInfo();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[] {Manifest.permission.CAMERA}, 0);


        ImageView backButton = findViewById(backImgButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });


        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(false);

        ivUser.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                EasyImage.openChooserWithGallery(EditProfileActivity.this, "Select", 0);
            }
        });

        TextView doneButton = findViewById(confirm);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                saveEditData();
                onBackPressed();
                finish();
            }
        });

        ImageView calendarButton = findViewById(ib_obtener_fecha);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                getDate();

            }
        });

        tbFem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tbMale.setChecked(false);
                } else {
                    tbMale.setChecked(true);
                }
            }
        });

        tbMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tbFem.setChecked(false);
                } else {
                    tbFem.setChecked(true);
                }
            }

        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivUser.setImageBitmap(null); ivUser.destroyDrawingCache();
    }

    protected void saveProfilePicture() {
        // Reading a Image file from file system
        // mirar que el bitmap no sea nulo
            Bitmap bitmap = ((BitmapDrawable) ivUser.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageInByte = baos.toByteArray();
            u.setImage(imageInByte);
            String response = baos.toString();
            //System.out.println("Respuesta Imagen: "+ response);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                Glide.with(EditProfileActivity.this)
                        .load(imageFiles.get(0))
                        .centerCrop()
                        .circleCrop()
                        .skipMemoryCache(true)
                        .into(ivUser);
            }
        });
        @SuppressLint("HandlerLeak") Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                saveProfilePicture();
            }
        };
        h.sendEmptyMessageDelayed(0, 1000);
    }

    private void getDate(){
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String formatDay = (dayOfMonth < 10)? zero + dayOfMonth :String.valueOf(dayOfMonth);
                String formatMonth = (mesActual < 10)? zero + mesActual :String.valueOf(mesActual);
                etDate.setText(formatDay + slash + formatMonth + slash + year);
            }
        }, year, month, day);
        datePicker.show();

    }

    private void showUserInfo() {
        etName.setText(u.getName());
        boolean validImage = false;
        byte[] userImage = null;
        if (u.getImage() != null) {
            validImage = true;
            userImage = u.getImage();
        }
        if(!(u.getDescription().equals("null")|| u.getDescription().equals(""))) etDescription.setText(u.getDescription());
        if(!(u.getBirthDate().equals("null") || u.getBirthDate().equals(""))) etDate.setText(u.getBirthDate());
        if (!(u.getGender().equals("null") || u.getGender().equals(""))) {
            if (u.getGender().equals("1")) tbFem.setChecked(true);
            else tbMale.setChecked(true);
        }
        if(!(u.getCountry().equals("null") || u.getCountry().equals(""))) sCountry.setSelection(Integer.parseInt(u.getCountry()));
        if (validImage) {
            Glide.with(EditProfileActivity.this)
                    .load(userImage)
                    .centerCrop()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .into(ivUser);
        }

    }

    private void saveEditData() {

        if (etName.getText().toString().trim().length() != 0) u.setName(etName.getText().toString());
        if(tbFem.isChecked())
            u.setGender("1");
        else u.setGender("0");
        if (etDescription.getText().toString().trim().length() != 0) u.setDescription(etDescription.getText().toString());
        if (etDate.getText().toString().trim().length() != 0) u.setBirthDate(etDate.getText().toString());
        u.setCountry(String.valueOf(sCountry.getSelectedItemPosition()));



        String route = "http://10.4.41.146:3001/user/"+u.getId()+"/info";
        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), route);
        connection.postUserInfo();

        if (u.getImage() != null) {
            route = "http://10.4.41.146:3001/user/" + u.getId() + "/profile";
            connection = new ConnetionAPI(getApplicationContext(), route);
            connection.setUserProfilePicture();
        }
    }
}
