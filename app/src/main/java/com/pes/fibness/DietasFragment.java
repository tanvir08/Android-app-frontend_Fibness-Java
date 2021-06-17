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

public class DietasFragment extends Fragment {

    private ListView listViewT;
    private ArrayList<String> dietList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dietas, container, false);

        dietList = User.getInstance().getDietList();

        listViewT = (ListView)view.findViewById(R.id.listViewDiet);

        refreshList();

        listViewT.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent dayPage = new Intent(getActivity(), ChooseDayActivity.class);
                dayPage.putExtra("title", dietList.get(position));
                startActivity(dayPage);
            }
        } );

        listViewT.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditBox(position);
                return true;
            }
        } );

        FloatingActionButton button = view.findViewById(R.id.fb_new_dieta);

        button.setOnClickListener( new AdapterView.OnClickListener() {
            public void onClick(View v){
                showInputBox();
            }
        });

        return view;
    }

    private void refreshList() {
        listViewT.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.row, dietList));
    }

    private void showEditBox(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.input_edit_diet);
        builder.setTitle(getString(R.string.edit) + " " + dietList.get(position));
        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText editNameText = (EditText) dialog.findViewById(R.id.editTitleDietInput);
        editNameText.setText(dietList.get(position));
        final EditText editDescText = (EditText) dialog.findViewById(R.id.editDescDietInput);

        editDescText.setText(User.getInstance().getDietDesc(dietList.get(position)));

        Button btDelete = (Button) dialog.findViewById(R.id.btdeleteEditDiet);
        Button btDone = (Button) dialog.findViewById(R.id.btdoneEditDiet);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editNameText.getText().toString().trim().length() == 0) {
                    editNameText.setError(getString(R.string.PleaseAddAName));
                }
                else if (dietList.contains(editNameText.getText().toString()) &&
                        !dietList.get(position).equals(editNameText.getText().toString())){
                    editNameText.setError(getString(R.string.NameAlreadyUsed));
                }
                else {
                    int idDiet = User.getInstance().getDietID(dietList.get(position));
                    String dietName = editNameText.getText().toString();
                    String desc = editDescText.getText().toString();

                    ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/diet/" + idDiet);
                    c.updateUserDiets(dietName, desc);

                    User.getInstance().setDietName(dietList.get(position), editNameText.getText().toString());
                    User.getInstance().setDietDesc(editNameText.getText().toString(), editDescText.getText().toString());

                    dietList.set(position, editNameText.getText().toString());
                    refreshList();
                    dialog.dismiss();
                }
            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idDiet = User.getInstance().getDietID(dietList.get(position));

                ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/diet/" + idDiet);
                c.deleteUserDiets();

                User.getInstance().deleteDiet(dietList.get(position));

                dietList.remove(position);
                refreshList();
                dialog.dismiss();
            }
        });

    }


    private void showInputBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.input_new_diet);
        builder.setTitle(getString(R.string.Diet));
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView txt = (TextView) dialog.findViewById(R.id.TitleNewDiet);
        txt.setText(getString(R.string.AddAName));
        final EditText editNameText = (EditText) dialog.findViewById(R.id.TitleDietInput);
        final EditText editDescText = (EditText) dialog.findViewById(R.id.DescNewDiet);
        Button bt = (Button) dialog.findViewById(R.id.btdoneDiet);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editNameText.getText().toString().trim().length() == 0) {
                    editNameText.setError(getString(R.string.PleaseAddAName));
                }
                else if (dietList.contains(editNameText.getText().toString())){
                    editNameText.setError(getString(R.string.NameAlreadyUsed));
                }
                else {
                    String titleDiet = editNameText.getText().toString();
                    String desc = editDescText.getText().toString();

                    /**Substituir por conexion con la BD**/
                    Diet d = new Diet();
                    d.name = titleDiet;
                    d.desc = desc;
                    d.id = -1;
                    User.getInstance().addDiet(d);
                    User.getInstance().setMealList(new ArrayList<Meal>());
                    /**End**/

                    ConnetionAPI c = new ConnetionAPI(getContext(), "http://10.4.41.146:3001/diet");
                    c.postUserDiets(User.getInstance().getId(), titleDiet, desc);

                    dietList.add(titleDiet);
                    refreshList();
                    dialog.dismiss();
                    Intent dayPage = new Intent(getActivity(), ChooseDayActivity.class);
                    dayPage.putExtra("title", titleDiet);
                    startActivity(dayPage);
                }
            }
        });
    }
}
