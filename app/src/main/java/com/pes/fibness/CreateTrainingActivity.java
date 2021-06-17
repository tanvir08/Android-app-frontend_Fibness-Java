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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;


public class CreateTrainingActivity extends AppCompatActivity {

    private Boolean isNew;
    private String titleTraining = "";
    private ListView exerciseList;
    private ArrayList<Exercise> exercise = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concrete_training);
        getExtras();

        exercise = User.getInstance().getExerciseList();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarCT);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(titleTraining);
        getSupportActionBar().setSubtitle(User.getInstance().getTrainingDesc(titleTraining));

        exerciseList = (ListView) findViewById(R.id.ExerciseList);

        refreshList();

        Button add_ex = (Button) findViewById(R.id.AddExer);
        add_ex.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showChooseOption();
                isNew = false;
                refreshList();
            }
        });

        exerciseList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int Desc = User.getInstance().getExerciseNamePos(position);
                if(Desc >= 0) showEditExBox(position);
                else showEditExBoxPers(position);
                return true;
            }
        });

    }

    private void showChooseOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateTrainingActivity.this);
        builder.setView(R.layout.choose_type_training);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Button btnSimple = (Button) dialog.findViewById(R.id.Simple);
        Button btnCustom = (Button) dialog.findViewById(R.id.Custom);
        btnSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewExercise();
                dialog.dismiss();
            }
        });
        btnCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewExercisePers();
                dialog.dismiss();
            }
        });
    }

    private void showEditExBox(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateTrainingActivity.this);
        builder.setView(R.layout.input_edit_exercise);
        builder.setTitle(getString(R.string.Exercise));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final Spinner txtNameS = (Spinner) dialog.findViewById(R.id.ExerciseTitle_edit);
        int posEx = User.getInstance().getExerciseNamePos(position);
        txtNameS.setSelection(posEx);
        final EditText numRest = (EditText) dialog.findViewById(R.id.num_Rest_edit);
        numRest.setText(exercise.get(position).NumRest);
        final EditText numSeries = (EditText) dialog.findViewById(R.id.num_Series_edit);
        numSeries.setText(exercise.get(position).NumSerie);
        final EditText numRepet = (EditText) dialog.findViewById(R.id.num_Repet_edit);
        numRepet.setText(exercise.get(position).NumRepet);
        Button btndone = (Button) dialog.findViewById(R.id.btn_done_edit);
        Button btndelete = (Button) dialog.findViewById(R.id.btn_delete_edit);
        btndone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                String txtName = (String) txtNameS.getSelectedItem();
                if (txtName.equals(getString(R.string.SelectExercise))) {
                    correct = false;
                }
                if (numRest.getText().toString().trim().length() == 0) {
                    numRest.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (numRepet.getText().toString().trim().length() == 0) {
                    numRepet.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (numSeries.getText().toString().trim().length() == 0) {
                    numSeries.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (correct) {
                    Exercise t2 = new Exercise();
                    t2.TitleEx = txtName;
                    t2.NumSerie = numSeries.getText().toString();
                    t2.NumRest = numRest.getText().toString();
                    t2.NumRepet = numRepet.getText().toString();
                    t2.Pos = txtNameS.getSelectedItemPosition();
                    int idExercise = User.getInstance().getExerciseID(position);
                    t2.id = idExercise;

                    User.getInstance().updateExercise(position, t2);

                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise/" + idExercise );
                    c.updateTrainingExercises(t2.TitleEx, "", t2.Pos, Integer.parseInt(t2.NumRest), Integer.parseInt(t2.NumSerie), Integer.parseInt(t2.NumRepet));

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int idExercise = User.getInstance().getExerciseID(position);
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise/" + idExercise );
                c.deleteTrainingExercises();

                User.getInstance().deleteExercise(position);

                refreshList();
                dialog.dismiss();
            }
        });

    }

    private void showEditExBoxPers(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateTrainingActivity.this);
        builder.setView(R.layout.input_edit_exercise_pers);
        builder.setTitle(getString(R.string.Exercise));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.ExerciseTitle_edit);
        txtName.setText(exercise.get(position).TitleEx);
        final EditText numRest = (EditText) dialog.findViewById(R.id.num_Rest_edit);
        numRest.setText(exercise.get(position).NumRest);
        final EditText numSeries = (EditText) dialog.findViewById(R.id.num_Series_edit);
        numSeries.setText(exercise.get(position).NumSerie);
        final EditText numRepet = (EditText) dialog.findViewById(R.id.num_Repet_edit);
        numRepet.setText(exercise.get(position).NumRepet);
        final EditText txtDesc = (EditText) dialog.findViewById(R.id.editDescEx);
        txtDesc.setText(exercise.get(position).Desc);
        Button btndone = (Button) dialog.findViewById(R.id.btn_done_edit);
        Button btndelete = (Button) dialog.findViewById(R.id.btn_delete_edit);
        btndone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                if (txtName.getText().toString().trim().length() == 0) {
                    txtName.setError(getString(R.string.PleaseAddAName));
                    correct = false;
                }
                if (numRest.getText().toString().trim().length() == 0) {
                    numRest.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (numRepet.getText().toString().trim().length() == 0) {
                    numRepet.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (numSeries.getText().toString().trim().length() == 0) {
                    numSeries.setError(getString(R.string.AddANumber));
                    correct = false;
                }
                if (correct) {
                    Exercise t2 = new Exercise();
                    t2.TitleEx = txtName.getText().toString();
                    t2.NumSerie = numSeries.getText().toString();
                    t2.NumRest = numRest.getText().toString();
                    t2.NumRepet = numRepet.getText().toString();
                    t2.Desc = txtDesc.getText().toString();
                    t2.Pos = User.getInstance().getExerciseNamePos(position);
                    int idExercise = User.getInstance().getExerciseID(position);
                    t2.id = idExercise;

                    User.getInstance().updateExercise(position, t2);

                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise/" + idExercise );
                    c.updateTrainingExercises(t2.TitleEx, t2.Desc, t2.Pos, Integer.parseInt(t2.NumRest), Integer.parseInt(t2.NumSerie), Integer.parseInt(t2.NumRepet));

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int idExercise = User.getInstance().getExerciseID(position);
                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise/" + idExercise );
                c.deleteTrainingExercises();

                User.getInstance().deleteExercise(position);

                refreshList();
                dialog.dismiss();
            }
        });

    }

    private void refreshList() {
        exerciseList.setAdapter(new Exercise_Adap(this, exercise, isNew));
    }

    private void showNewExercisePers() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateTrainingActivity.this);
        builder.setView(R.layout.input_new_exercise_pers);
        builder.setTitle(getString(R.string.Exercise));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText txtName = (EditText) dialog.findViewById(R.id.ExerciseTitle);
        final EditText numRest = (EditText) dialog.findViewById(R.id.num_Rest);
        final EditText numSeries = (EditText) dialog.findViewById(R.id.num_Series);
        final EditText numRepet = (EditText) dialog.findViewById(R.id.num_Rept);
        final EditText txtDesc = (EditText) dialog.findViewById(R.id.newDescEx);
        Button bt = (Button) dialog.findViewById(R.id.btn_done);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                if (txtName.getText().toString().trim().length() == 0) {
                    txtName.setError(getString((R.string.PleaseAddAName)));
                    correct = false;
                }
                if (numRepet.getText().toString().trim().length() == 0) {
                    numRepet.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (numRest.getText().toString().trim().length() == 0) {
                    numRest.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (numSeries.getText().toString().trim().length() == 0) {
                    numSeries.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (correct) {
                    Exercise t2 = new Exercise();
                    t2.TitleEx = txtName.getText().toString();
                    t2.NumSerie = numSeries.getText().toString();
                    t2.NumRest = numRest.getText().toString();
                    t2.NumRepet = numRepet.getText().toString();
                    t2.Desc = txtDesc.getText().toString();
                    t2.Pos = -1;
                    t2.id = -1;

                    User.getInstance().addExercise(t2);

                    int pos = User.getInstance().sizeExerciseList();

                    int idTraining = User.getInstance().getTrainingID(titleTraining);
                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise");
                    c.postTrainingExercises(idTraining, t2.TitleEx, t2.Desc, t2.Pos, Integer.parseInt(t2.NumRest), Integer.parseInt(t2.NumSerie), Integer.parseInt(t2.NumRepet), pos-1);

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
    }

    private void showNewExercise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateTrainingActivity.this);
        builder.setView(R.layout.input_new_exercise);
        builder.setTitle(getString(R.string.Exercise));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final Spinner txtNameS = (Spinner) dialog.findViewById(R.id.ExerciseTitle);
        final EditText numRest = (EditText) dialog.findViewById(R.id.num_Rest);
        final EditText numSeries = (EditText) dialog.findViewById(R.id.num_Series);
        final EditText numRepet = (EditText) dialog.findViewById(R.id.num_Rept);
        Button bt = (Button) dialog.findViewById(R.id.btn_done);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correct = true;
                String txtName = (String) txtNameS.getSelectedItem();
                if (txtName.equals(getString(R.string.SelectExercise))) {
                    correct = false;
                }
                if (numRepet.getText().toString().trim().length() == 0) {
                    numRepet.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (numRest.getText().toString().trim().length() == 0) {
                    numRest.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (numSeries.getText().toString().trim().length() == 0) {
                    numSeries.setError(getString((R.string.AddANumber)));
                    correct = false;
                }
                if (correct) {
                    Exercise t2 = new Exercise();
                    t2.TitleEx = txtName;
                    t2.NumSerie = numSeries.getText().toString();
                    t2.NumRest = numRest.getText().toString();
                    t2.NumRepet = numRepet.getText().toString();
                    t2.Pos = txtNameS.getSelectedItemPosition();
                    t2.id = -1;

                    User.getInstance().addExercise(t2);

                    int pos = User.getInstance().sizeExerciseList();

                    int idTraining = User.getInstance().getTrainingID(titleTraining);
                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/exercise");
                    c.postTrainingExercises(idTraining, t2.TitleEx, "", t2.Pos, Integer.parseInt(t2.NumRest), Integer.parseInt(t2.NumSerie), Integer.parseInt(t2.NumRepet), pos-1);

                    refreshList();
                    dialog.dismiss();
                }
            }
        });
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        isNew = extras.getBoolean("new");
        titleTraining = extras.getString("title");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}


class Exercise_Adap extends BaseAdapter {

    private static LayoutInflater inflater = null;

    private Context context;
    private ArrayList<Exercise> dades;
    private boolean New;

    public Exercise_Adap(Context c, ArrayList<Exercise> d, boolean IsNew){
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
        final View vista = inflater.inflate(R.layout.exerciselist_element, null);
        if(!New) {
            TextView titulo = (TextView) vista.findViewById(R.id.ExerciseTitle);
            TextView repet = (TextView) vista.findViewById(R.id.txtRepet);
            TextView rest = (TextView) vista.findViewById(R.id.txtRest);
            TextView series = (TextView) vista.findViewById(R.id.txtSeries);
            ImageView image = (ImageView) vista.findViewById(R.id.photo_ex);
            TextView Desc = (TextView) vista.findViewById(R.id.txtDesc);
            titulo.setText(dades.get(position).TitleEx);
            int id = getImageID (position);
            if(id != -1) image.setImageResource(id);
            else{
                image.setImageResource(R.drawable.logoazulflojo);
                Desc.setText(dades.get(position).Desc);
            }
            series.setText(dades.get(position).NumSerie);
            rest.setText(dades.get(position).NumRest + " s");
            repet.setText(dades.get(position).NumRepet);
        }

        return vista;
    }

    private int getImageID(int position){
        int id = -1;
        int pos = User.getInstance().getExerciseNamePos(position);
        switch (pos){
            case 1:
                id = R.drawable.squat;
                break;
            case 2:
                id = R.drawable.hollow_hold;
                break;
            case 3:
                id = R.drawable.lunges;
                break;
            case 4:
                id = R.drawable.back_extension_hold;
                break;
            case 5:
                id = R.drawable.plank;
                break;
            case 6:
                id = R.drawable.sit_ups;
                break;
            case 7:
                id = R.drawable.jumping_jacks;
                break;
            case 8:
                id = R.drawable.push_up;
                break;
            case 9:
                id = R.drawable.dips;
                break;
            case 10:
                id = R.drawable.burpees;
                break;
            case 11:
                id = R.drawable.ab_infer;
                break;
            case 12:
                id = R.drawable.bicycle_crunches;
                break;
        }
        return id;
    }

}