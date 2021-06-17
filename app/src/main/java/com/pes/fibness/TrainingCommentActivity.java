package com.pes.fibness;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Quota;

import static com.facebook.FacebookSdk.getApplicationContext;

public class TrainingCommentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView, recyclerview_comment;
    private Button importTraining;
    private ImageView like, comment, send ;
    private TextView nlike, ncomment;
    private ConstraintLayout CL;
    private EditText msgText;

    private List<ExerciseModel> exerciseModelList = new ArrayList<>();
    private ArrayList<ExerciseExtra> names = new ArrayList<>();
    private ShowExerciseAdapter showExerciseAdapter;

    private List<CommentModel> commentModelList = new ArrayList<>();
    private ArrayList<Comment> names2 = new ArrayList<>();
    private CommentAdapter commentAdapter;

    private TrainingModel trainingModel;
    private int userId;
    private Boolean liked = User.getInstance().getElementLike();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_comment);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            trainingModel = (TrainingModel) intent.getSerializableExtra("data");
            userId = (int) intent.getSerializableExtra("userId");
            System.out.println("Training id: " + trainingModel.getId());
            System.out.println("User selected id: " + userId);
        }

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerview_comment = findViewById(R.id.recyclerview_comment);
        importTraining = findViewById(R.id.importTraining);
        like = findViewById(R.id.like);
        nlike = findViewById(R.id.nlike);
        comment = findViewById(R.id.comment);
        ncomment = findViewById(R.id.ncomment);
        CL = findViewById(R.id.CL);
        nlike.setText("" + trainingModel.getnLikes());
        ncomment.setText("" + trainingModel.getnComment());
        send = findViewById(R.id.send);
        msgText = findViewById(R.id.msgText);

        ArrayList<ExerciseExtra> tn = User.getInstance().getExerciseExtras();
        names = tn;

        ArrayList<Comment> tc = User.getInstance().getComments();
        names2 = tc;

        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerview_comment.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_comment.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        for(int i=0; i < names.size(); ++i){
            ExerciseModel exerciseModel = new ExerciseModel(names.get(i).id, names.get(i).title, names.get(i).desc, names.get(i).numRep, names.get(i).numSerie, names.get(i).numRest);
            exerciseModelList.add(exerciseModel);
        }

        for(int i=0; i < names2.size(); ++i){
            CommentModel commentModel = new CommentModel(names2.get(i).id_comment, names2.get(i).id_user, names2.get(i).user_name, names2.get(i).date, names2.get(i).text);
            commentModelList.add(commentModel);
        }



        showExerciseAdapter = new ShowExerciseAdapter(exerciseModelList);
        recyclerView.setAdapter(showExerciseAdapter);

        commentAdapter = new CommentAdapter(commentModelList);
        recyclerview_comment.setAdapter(commentAdapter);



        importTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/import");
                connetionAPI.importElement("training",trainingModel.getId(), User.getInstance().getId() );
            }
        });



        if(liked)
            like.setColorFilter(getApplicationContext().getResources().getColor(R.color.red));
        else
            like.setColorFilter(getApplicationContext().getResources().getColor(R.color.c_icon_like));


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(liked){ //delete like
                    ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/like/" + User.getInstance().getId() + "/" + trainingModel.getId() + "/element" );
                    connetionAPI.deleteElementLike();
                    int n = trainingModel.getnLikes() -1;
                    trainingModel.setnLikes(n);
                    nlike.setText("" + n);
                    like.setColorFilter(getApplicationContext().getResources().getColor(R.color.c_icon_like));
                    liked = false;
                }
                else { //post like
                    ConnetionAPI connetionAPI = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/like");
                    connetionAPI.likeElement(User.getInstance().getId(), trainingModel.getId(), "element");
                    int n = trainingModel.getnLikes() + 1;
                    trainingModel.setnLikes(n);
                    nlike.setText("" + n);

                    like.setColorFilter(getApplicationContext().getResources().getColor(R.color.red));
                    liked = true;
                }

            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msgText.requestFocus();
                msgText.setFocusableInTouchMode(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(msgText, InputMethodManager.SHOW_FORCED);

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String text = msgText.getText().toString();
                if(text.equals("")){
                    System.out.println("commentAdapter size: " + commentAdapter.getItemCount());
                    Toast.makeText(getApplicationContext(), "The comment cannot be empty", Toast.LENGTH_LONG).show();
                }
                else {
                    ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/comment/");
                    c.postComment(User.getInstance().getId(), trainingModel.getId(),text);

                    int n = trainingModel.getnComment() + 1;
                    trainingModel.setnComment(n);
                    ncomment.setText("" + n);

                    CommentModel commentModel = new CommentModel(1, User.getInstance().getId(), User.getInstance().getName(), LocalDate.now().toString(), text);
                    commentModelList.add(commentModel);

                    commentAdapter = new CommentAdapter(commentModelList);
                    recyclerview_comment.setAdapter(commentAdapter);
                    finishActivity(1);



                }


            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(1);
    }
}


class ShowExerciseAdapter extends RecyclerView.Adapter<ShowExerciseAdapter.ShowExerciseAdapterVh> {

    private List<ExerciseModel> exerciseModelList;
    private Context context;
    private Boolean first = false;

    public ShowExerciseAdapter(List<ExerciseModel> exerciseModelList) {
        this.exerciseModelList = exerciseModelList;
    }


    @NonNull
    @Override
    public ShowExerciseAdapter.ShowExerciseAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ShowExerciseAdapter.ShowExerciseAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_exercise, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ShowExerciseAdapter.ShowExerciseAdapterVh holder, int position) {
        ExerciseModel exerciseModel = exerciseModelList.get(position);
        String title = exerciseModel.getTitle();
        String description = exerciseModel.getDesc();
        int numRep = exerciseModel.getNumRep();
        int numSerie = exerciseModel.getNumSerie();
        int numRest = exerciseModel.getNumRest();

        switch (title){
            case "Squat":
                holder.imageView.setImageResource(R.drawable.squat);
                break;
            case "Hollow Hold":
                holder.imageView.setImageResource( R.drawable.hollow_hold);
                break;
            case "Lunges":
                holder.imageView.setImageResource(R.drawable.lunges);
                break;
            case "Back Extensión Hold":
                holder.imageView.setImageResource(R.drawable.back_extension_hold);
                break;
            case "Plank":
                holder.imageView.setImageResource(R.drawable.plank);
                break;
            case "Sit ups":
                holder.imageView.setImageResource(R.drawable.sit_ups);
                break;
            case "Jumping Jacks":
                holder.imageView.setImageResource(R.drawable.jumping_jacks);
                break;
            case "Push Up":
                holder.imageView.setImageResource(R.drawable.push_up);
                break;
            case "Dips":
                holder.imageView.setImageResource(R.drawable.dips);
                break;
            case "Burpees":
                holder.imageView.setImageResource(R.drawable.burpees);
                break;
            case "Leg Raises":
                holder.imageView.setImageResource(R.drawable.ab_infer);
                break;
            case "Bicycle Crunches":
                holder.imageView.setImageResource(R.drawable.bicycle_crunches);
                break;
        }

        holder.tvTitle.setText(title);
        String s1 = (String) holder.tvRep.getText();
        holder.tvRep.setText(s1 + " " + numRep);
        String s2 = (String) holder.tvSerie.getText();
        holder.tvSerie.setText(s2 + " " + numSerie);
        String s3 = (String) holder.tvRest.getText();
        holder.tvRest.setText(s3 + " " + numRest + "s");
        holder.tvDesc.setText(description);
    }

    @Override
    public int getItemCount() {
        return exerciseModelList.size();
    }



    public class ShowExerciseAdapterVh extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRep, tvSerie, tvRest, tvDesc;
        ImageView imageView;

        public ShowExerciseAdapterVh(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvDesc = itemView.findViewById(R.id.desc);
            tvRep = itemView.findViewById(R.id.reps);
            tvSerie = itemView.findViewById(R.id.serie);
            tvRest = itemView.findViewById(R.id.rest);
            imageView = itemView.findViewById(R.id.imageView);

        }
    }




}










class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentAdapterVh> {

    private List<CommentModel> commentModelList;
    private Context context;

    public CommentAdapter(List<CommentModel> commentModelList) {
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public CommentAdapter.CommentAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CommentAdapter.CommentAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_comments, null));
    }


    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentAdapterVh holder, int position) {

        CommentModel commentModel = commentModelList.get(position);
        System.out.println("myname: " + commentModel.getUsername());
        System.out.println("mytext: " + commentModel.getText());
        System.out.println("mydate: " + commentModel.getDate());

        holder.prefix.setText(commentModel.getUsername().substring(0,1));
        holder.username.setText(commentModel.getUsername());
        holder.comment.setText(commentModel.getText());
        holder.date.setText(commentModel.getDate().substring(0,10));


    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }


    public class CommentAdapterVh extends RecyclerView.ViewHolder {

        TextView prefix, username, comment, date;
        public CommentAdapterVh(View itemView){
            super(itemView);
            prefix = itemView.findViewById(R.id.prefix);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            date = itemView.findViewById(R.id.dateC);



        }

    }


}






