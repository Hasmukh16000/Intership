package com.example.intership;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<UserModel> userList;
    private User USER;
    private ProgressDialog pDialog;

    public UserRecyclerViewAdapter(Context context,List<UserModel> userModelList){
        this.context=context;
        this.userList=userModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_row, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setUserData(userList.get(position));
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                USER.deleteUser(position,userList.get(position).getTime(),userList.get(position));
                displayLoader();
                FirebaseFirestore db=FirebaseFirestore.getInstance();
                db.collection(Constant.KEY_COLLECTION_USERS).document(userList.get(position).getTime())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pDialog.dismiss();
                                userList.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context,"User Deleted Successfully ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pDialog.dismiss();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView delete;
        TextView fullname;
        TextView details;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            USER=new User();
            image=(ImageView)itemView.findViewById(R.id.listImageID);
            delete=(ImageView)itemView.findViewById(R.id.listDeleteID);
            fullname=(TextView)itemView.findViewById(R.id.listnameID);
            details=(TextView)itemView.findViewById(R.id.listDetailID);

        }

        void setUserData(UserModel userData){
            StringBuilder Fullname=new StringBuilder(userData.getFirst_name());
            Fullname.append(" ");
            Fullname.append(userData.getLast_name());
            fullname.setText(Fullname);

            StringBuilder Details=new StringBuilder(userData.getGender());
            Details.append(" | ");
            Details.append(getAge(userData.getDob()));
            Details.append(" | ");
            Details.append(userData.getHometown());
            details.setText(Details);
            if(!userData.getImage().isEmpty()){
                image.setBackgroundResource(0);
                Glide.with(context).load(userData.getImage()).into(image);
            }
        }


    }

    public String getAge(String dob) {
        String age = null;
        String DOB = dob;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = formatter.parse(DOB);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Instant instant = date.toInstant();
        ZonedDateTime zone = instant.atZone(ZoneId.systemDefault());
        LocalDate givenDate = zone.toLocalDate();
        Period period = Period.between(givenDate, LocalDate.now());
        age=String.valueOf(period.getYears());
        return age;
    }

    private void displayLoader() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Deleting Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }
}

