package com.pes.fibness;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;

public class CreateDietActivity extends AppCompatActivity {

    private Boolean isNew;
    private String titleDiet = "";
    private ListView foodList;
    private ArrayList<Aliment> food = new ArrayList<>();
    private String day = "";
    private String meal = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concrete_diet);
        getExtras();

        food = User.getInstance().getAlimentList();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarCD);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(titleDiet + "-" + day + "-" + meal);
        getSupportActionBar().setSubtitle(User.getInstance().getDietDesc(titleDiet));

        foodList = (ListView) findViewById(R.id.FoodList);

        refreshList();

        Button add_ex = (Button) findViewById(R.id.AddFood);
        add_ex.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showNewFood();
                isNew = false;
                refreshList();
            }
        });
        
        foodList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditExBox(position);
                return true;
            }
        });

    }

    private void showEditExBox(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateDietActivity.this);
        builder.setView(R.layout.input_edit_food);
        builder.setTitle(getString(R.string.Food));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.EditNameAliment);
        txtName.setText(food.get(position).name);
        final EditText numCalories = (EditText) dialog.findViewById(R.id.EditSetCalories);
        numCalories.setText(food.get(position).calories);
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
                if (numCalories.getText().toString().trim().length() == 0) {
                    numCalories.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (correct) {
                    Aliment a2 = new Aliment();
                    a2.name = txtName.getText().toString();
                    a2.calories = numCalories.getText().toString();
                    int idFood = User.getInstance().getAlimentID(position);
                    a2.id = idFood;
                    food.set(position, a2);
                    User.getInstance().setAlimentList(food);

                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/aliment/" + idFood);
                    c.updateMealAliment(a2.name, a2.calories);

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idFood = User.getInstance().getAlimentID(position);
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/aliment/" + idFood );
                c.deleteMealAliment();

                User.getInstance().deleteAliment(position);

                refreshList();
                dialog.dismiss();
            }
        });

    }

    private void refreshList() {
        foodList.setAdapter(new Food_Adap(this, food, isNew));
    }

    private void showNewFood() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateDietActivity.this);
        builder.setView(R.layout.input_new_food);
        builder.setTitle(getString(R.string.Food));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.NewNameAliment);
        final EditText numCalories = (EditText) dialog.findViewById(R.id.NewSetCalories);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                if (txtName.getText().toString().trim().length() == 0) {
                    txtName.setError(getString(R.string.PleaseAddAName));
                    correct = false;
                }
                if (numCalories.getText().toString().trim().length() == 0) {
                    numCalories.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (correct) {
                    Aliment a2 = new Aliment();
                    a2.name = txtName.getText().toString();
                    a2.calories = numCalories.getText().toString();
                    a2.id = -1;

                    User.getInstance().addAliment(a2);

                    int pos = User.getInstance().getSizeAlimentList();

                    int idMeal = User.getInstance().getMealID(meal);
                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/aliment");
                    c.postMealAliment(idMeal, a2.name, a2.calories, pos-1);

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        isNew = extras.getBoolean("new");
        titleDiet = extras.getString("title");
        day = extras.getString("dia");
        meal = extras.getString("comida");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

class Food_Adap extends BaseAdapter {

    private static LayoutInflater inflater = null;

    private Context context;
    private ArrayList<Aliment> dades;
    private boolean New;

    public Food_Adap(Context c, ArrayList<Aliment> d, boolean IsNew){
        context = c;
        dades = d;
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
        final View vista = inflater.inflate(R.layout.aliment_element, null);
        if(!New) {
            TextView titulo = (TextView) vista.findViewById(R.id.txtNameAliment);
            TextView calorias = (TextView) vista.findViewById(R.id.txtSetCalories);
            titulo.setText(dades.get(position).name);
            calorias.setText(dades.get(position).calories);
        }

        return vista;
    }
}
