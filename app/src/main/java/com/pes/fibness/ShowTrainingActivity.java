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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ShowTrainingActivity extends AppCompatActivity implements ShowTrainingAdapter.SelectedTraining{

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private List<TrainingModel> trainingModelList = new ArrayList<>();
    private ArrayList<TrainingExtra> names = new ArrayList<>();
    private ShowTrainingAdapter showTrainingAdapter;

    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_training);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            userModel = (UserModel) intent.getSerializableExtra("data");
            System.out.println("user id: " + userModel.getId());
            System.out.println("username: " + userModel.getUsername());
        }

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerview);

        ArrayList<TrainingExtra> tn = User.getInstance().getTrainingExtra();
        names = tn;

        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        for(int i=0; i < names.size(); ++i){
            TrainingModel trainingModel = new TrainingModel(names.get(i).id, names.get(i).name,names.get(i).desc, names.get(i).nLikes, names.get(i).nComment);
            trainingModelList.add(trainingModel);
        }

        showTrainingAdapter = new ShowTrainingAdapter(trainingModelList, this);
        recyclerView.setAdapter(showTrainingAdapter);



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void selectedTraining(final TrainingModel trainingModel) {
        /*tengo que obtener los ejercicios y comentarios de este training*/
        ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/training/" + trainingModel.getId() + "/activities");
        c.getTrainingExercises("chivato");


        ConnetionAPI c2 = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/likeelemento/" + trainingModel.getId()+ "/" + User.getInstance().getId());
        c2.getElementLike();


        /*cargo comments*/
        ConnetionAPI c3 = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/comment/" + trainingModel.getId() + "/comments");
        c3.getTrainingComments();


        @SuppressLint("HandlerLeak") Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent i = new Intent().setClass(ShowTrainingActivity.this, TrainingCommentActivity.class).putExtra("data", trainingModel);
                i.putExtra("userId", userModel.getId());
                i.putExtra("name", "ShowTrainingActivity");
                startActivity(i);
            }
        };
        h.sendEmptyMessageDelayed(0, 300);





    }
}


class ShowTrainingAdapter extends RecyclerView.Adapter<ShowTrainingAdapter.ShowTrainingAdapterVh> {

    private List<TrainingModel> trainingModelList;
    private Context context;
    private ShowTrainingAdapter.SelectedTraining selectedTraining;
    private Boolean first = false;

    public ShowTrainingAdapter(List<TrainingModel> trainingModelList, ShowTrainingAdapter.SelectedTraining selectedTraining) {
        this.trainingModelList = trainingModelList;
        this.selectedTraining = selectedTraining;
    }


    @NonNull
    @Override
    public ShowTrainingAdapter.ShowTrainingAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ShowTrainingAdapter.ShowTrainingAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_training, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ShowTrainingAdapter.ShowTrainingAdapterVh holder, int position) {

        TrainingModel trainingModel = trainingModelList.get(position);
        String name = trainingModel.getName();
        String description = trainingModel.getDesc();
        holder.tvName.setText(name);
        holder.tvDesc.setText(description);

    }

    @Override
    public int getItemCount() {
        return trainingModelList.size();
    }





    public interface SelectedTraining{
        public void selectedTraining(TrainingModel trainingModel);
    }

    public class ShowTrainingAdapterVh extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;

        public ShowTrainingAdapterVh(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.name);
            tvDesc = itemView.findViewById(R.id.description);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedTraining.selectedTraining(trainingModelList.get(getAdapterPosition()));
                }
            });

        }
    }




}