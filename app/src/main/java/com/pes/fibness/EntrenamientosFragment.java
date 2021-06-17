package com.pes.fibness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EntrenamientosFragment extends Fragment {

    private ListView listViewT;
    private ArrayList<String> trainingList;
    public boolean wait;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entrenamientos, container, false);

        trainingList = User.getInstance().getTrainingList();

        listViewT = (ListView) view.findViewById(R.id.listViewTraining);

        refreshList();

        listViewT.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nameT = trainingList.get(position);
                int idT = User.getInstance().getTrainingID(nameT);
                ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/training/" + idT + "/activities");
                c.getTrainingExercises(nameT);
            }
        } );

        listViewT.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditBox(position);
                return true;
            }
        } );

        FloatingActionButton button = view.findViewById(R.id.fb_new_entrenamiento);

        button.setOnClickListener( new AdapterView.OnClickListener() {
            public void onClick(View v){
                showInputBox();
            }
        });

        return view;
    }

    private void refreshList() {
        listViewT.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.row, trainingList));
    }

    private void showEditBox(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.input_edit_training);
        builder.setTitle(getString(R.string.edit) + " " + trainingList.get(position));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText editNameText = (EditText) dialog.findViewById(R.id.editTitleTraininginput);
        editNameText.setText(trainingList.get(position));
        final EditText editDescText = (EditText) dialog.findViewById(R.id.editDesc);

        editDescText.setText(User.getInstance().getTrainingDesc(trainingList.get(position)));

        Button btDelete = (Button) dialog.findViewById(R.id.btdeleteEdit);
        Button btDone = (Button) dialog.findViewById(R.id.btdoneEdit);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editNameText.getText().toString().trim().length() == 0) {
                    editNameText.setError(getString(R.string.PleaseAddAName));
                }
                else if (trainingList.contains(editNameText.getText().toString()) &&
                        !trainingList.get(position).equals(editNameText.getText().toString())){
                        editNameText.setError(getString(R.string.NameAlreadyUsed));
                }
                else {
                    int idTraining = User.getInstance().getTrainingID(trainingList.get(position));
                    String trainingName = editNameText.getText().toString();
                    String desc = editDescText.getText().toString();

                    ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/training/" + idTraining);
                    c.updateUserTraining(trainingName, desc);

                    User.getInstance().setTrainingName(trainingList.get(position), trainingName);
                    User.getInstance().setTrainingDesc(editNameText.getText().toString(), desc);

                    trainingList.set(position, trainingName);
                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idTraining = User.getInstance().getTrainingID(trainingList.get(position));

                ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/training/" + idTraining);
                c.deleteUserTraining();

                User.getInstance().deleteTraining(trainingList.get(position));

                trainingList.remove(position);
                refreshList();
                dialog.dismiss();
            }
        });

    }


    public void showInputBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.input_new_training);
        builder.setTitle(getString(R.string.Training));
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView txt = (TextView) dialog.findViewById(R.id.inputboxTitleTraining);
        txt.setText(getString(R.string.AddAName));
        final EditText editNameText = (EditText) dialog.findViewById(R.id.TitleTraininginput);
        final EditText editDescText = (EditText) dialog.findViewById(R.id.DescNewTraining);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editNameText.getText().toString().trim().length() == 0) {
                    editNameText.setError(getString(R.string.PleaseAddAName));
                }
                else if (trainingList.contains(editNameText.getText().toString())){
                    editNameText.setError(getString(R.string.NameAlreadyUsed));
                }
                else {
                    Training t = new Training();
                    String titleTraining = editNameText.getText().toString();
                    String desc = editDescText.getText().toString();
                    t.name = titleTraining;
                    t.desc = desc;
                    t.id = -1;

                    User.getInstance().addTraining(t);
                    User.getInstance().setExerciseList(new ArrayList<Exercise>());

                    ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/training");
                    c.postUserTraining(User.getInstance().getId(), titleTraining, desc);

                    trainingList.add(titleTraining);
                    refreshList();
                    dialog.dismiss();

                    Intent TrainingPage = new Intent(getActivity(), CreateTrainingActivity.class);
                    TrainingPage.putExtra("new", true);
                    TrainingPage.putExtra("title", titleTraining);
                    startActivity(TrainingPage);
                }
            }
        });
    }

}
