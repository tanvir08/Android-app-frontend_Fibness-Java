package com.pes.fibness;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class MealActivity extends AppCompatActivity {

    private String titleDiet = "";
    private String day = "";
    private String diaTitle = "";
    private ArrayList<Meal> meals;
    private ListView mealList;
    private boolean isNew = false;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);
        getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMeal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(titleDiet + "-" + diaTitle);
        getSupportActionBar().setSubtitle(User.getInstance().getDietDesc(titleDiet));

        meals = User.getInstance().getMealList();

        mealList = (ListView) findViewById(R.id.MealList);

        if(meals.size()==0)isNew = true;

        refreshList();

        Button add_meal = (Button) findViewById(R.id.AddMeal);
        add_meal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showNewMeal();
                isNew = false;
                refreshList();
            }
        });

        mealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nameM = meals.get(position).name;
                int idMeal = User.getInstance().getMealID(nameM);
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/meal/" + idMeal + "/aliments");
                c.getMealAliment(titleDiet, diaTitle, nameM, false);
            }
        });

        mealList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditMealBox(position);
                return true;
            }
        });


    }

    private void refreshList(){
        mealList.setAdapter(new Meal_Adap(this, meals, isNew));
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        titleDiet = extras.getString("title");
        day = extras.getString("dia");
        diaTitle = extras.getString("diaTitle");
    }

    private void showEditMealBox(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MealActivity.this);
        builder.setView(R.layout.input_edit_meal);
        builder.setTitle(getString(R.string.Meal));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.EditNameMeal);
        txtName.setText(meals.get(position).name);
        final EditText txtHour = (EditText) dialog.findViewById(R.id.EditSetHour);
        final EditText txtMin = (EditText) dialog.findViewById(R.id.EditSetMin);
        String time = meals.get(position).time;
        String[] time2 = time.split(":");
        txtHour.setText(time2[0]);
        txtMin.setText(time2[1]);
        Button btndone = (Button) dialog.findViewById(R.id.btEditDone);
        Button btndelete = (Button) dialog.findViewById(R.id.btEditDelete);
        btndone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                if (txtName.getText().toString().trim().length() == 0) {
                    txtName.setError(getString(R.string.PleaseAddAName));
                    correct = false;
                }
                if (txtHour.getText().toString().trim().length() == 0) {
                    txtHour.setError(getString(R.string.AddAnHour));
                    correct = false;
                }
                else if (Integer.valueOf(txtHour.getText().toString()) > 23 || txtHour.getText().toString().length() > 2) {
                    txtHour.setError(getString(R.string.AddAValidHour));
                    correct = false;
                }
                if (txtMin.getText().toString().trim().length() == 0) {
                    txtMin.setError(getString(R.string.PleaseAddMinutes));
                    correct = false;
                }
                else if (Integer.valueOf(txtMin.getText().toString()) > 59 || txtMin.getText().toString().length() > 2) {
                    txtMin.setError(getString(R.string.AddValidMinutes));
                    correct = false;
                }
                else if (meals.contains(txtName.getText().toString()) &&
                        !meals.get(position).equals(txtName.getText().toString())){
                    txtName.setError(getString(R.string.NameAlreadyUsed));
                }
                if (correct) {
                    Meal m2 = new Meal();
                    m2.name = txtName.getText().toString();
                    String Hour = txtHour.getText().toString();
                    if(Hour.length() < 2) Hour = "0" + Hour;
                    String Min = txtMin.getText().toString();
                    if(Min.length() < 2) Min = "0" + Min;
                    String time = Hour + ":" + Min + ":00";
                    m2.time = time;
                    String nameM = meals.get(position).name;
                    int idMeal = User.getInstance().getMealID(nameM);
                    m2.id = idMeal;

                    User.getInstance().updateMeal(position, m2);

                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/meal/" + idMeal );
                    c.updateDietMeal(m2.name, m2.time);

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameM = meals.get(position).name;
                int idMeal = User.getInstance().getMealID(nameM);
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/meal/" + idMeal );
                c.deleteDietMeal();

                User.getInstance().deleteMeal(position);

                refreshList();
                dialog.dismiss();
            }
        });

    }

    private void showNewMeal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MealActivity.this);
        builder.setView(R.layout.input_new_meal);
        builder.setTitle(getString(R.string.Meal));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.NewNameMeal);
        final EditText txtHour = (EditText) dialog.findViewById(R.id.NewSetHour);
        final EditText txtMin = (EditText) dialog.findViewById(R.id.setNewMinutes);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                if (txtName.getText().toString().trim().length() == 0) {
                    txtName.setError(getString(R.string.PleaseAddAName));
                    correct = false;
                }
                if (txtHour.getText().toString().trim().length() == 0) {
                    txtHour.setError(getString(R.string.AddAnHour));
                    correct = false;
                }
                else if (Integer.valueOf(txtHour.getText().toString()) > 23 || txtHour.getText().toString().length() > 2) {
                    txtHour.setError(getString(R.string.AddAValidHour));
                    correct = false;
                }
                if (txtMin.getText().toString().trim().length() == 0) {
                    txtMin.setError(getString(R.string.PleaseAddMinutes));
                    correct = false;
                }
                else if (Integer.valueOf(txtMin.getText().toString()) > 59 || txtMin.getText().toString().length() > 2) {
                    txtMin.setError(getString(R.string.AddValidMinutes));
                    correct = false;
                }
                if (correct) {
                    Meal m2 = new Meal();
                    m2.name = txtName.getText().toString();
                    String Hour = txtHour.getText().toString();
                    if(Hour.length() < 2) Hour = "0" + Hour;
                    String Min = txtMin.getText().toString();
                    if(Min.length() < 2) Min = "0" + Min;
                    String time = Hour + ":" + Min + ":00";
                    m2.time = time;
                    m2.id = -1;

                    User.getInstance().addMeal(m2);

                    int idMeal = User.getInstance().getDietID(titleDiet);
                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/meal");
                    c.postDietMeal(idMeal, m2.name, m2.time, day);

                    refreshList();
                    dialog.dismiss();

                    Intent AlimentPage = new Intent(getApplicationContext(), CreateDietActivity.class);
                    AlimentPage.putExtra("new", true);
                    AlimentPage.putExtra("title", titleDiet);
                    AlimentPage.putExtra("dia", diaTitle);
                    AlimentPage.putExtra("comida", m2.name);
                    User.getInstance().setAlimentList(new ArrayList<Aliment>());
                    startActivity(AlimentPage);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

class Meal_Adap extends BaseAdapter {

    private static LayoutInflater inflater = null;

    private Context context;
    private ArrayList<Meal> dades;
    private boolean New;

    public Meal_Adap(Context c, ArrayList<Meal> m, boolean IsNew){
        context = c;
        dades = m;
        New = IsNew;
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dades.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View vista = inflater.inflate(R.layout.meal_element, null);
        if(!New) {
            TextView titulo = (TextView) vista.findViewById(R.id.txtNameMeal);
            TextView hora = (TextView) vista.findViewById(R.id.txtSetHour);
            titulo.setText(dades.get(position).name);
            String time = dades.get(position).time;
            String[] time2 = time.split(":");
            hora.setText(time2[0] + ":" + time2[1]);
        }

        return vista;
    }
}

