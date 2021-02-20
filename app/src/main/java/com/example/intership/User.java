package com.example.intership;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class User extends Fragment implements FragmentLifecycle {
    private RecyclerView recyclerView;;
    private List<UserModel> userList;
    private ProgressDialog pDialog;
    private UserRecyclerViewAdapter recyclerViewAdapter;


    public User() {
        // required empty public constructor.
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userList=new ArrayList<>();
        getUsers();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user, container, false);
        recyclerView=(RecyclerView) view.findViewById(R.id.recyclerViewID);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAdapter=new UserRecyclerViewAdapter(getContext(),userList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
        return view;
    }


    private void getUsers(){
        displayLoader();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS)
                .orderBy(Constant.KEY_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    pDialog.dismiss();
                    if (task.isSuccessful() && task.getResult()!=null){
                        userList.clear();
                        for(QueryDocumentSnapshot documentSnapshots:task.getResult()){
                            UserModel user=new UserModel();
                            user.setFirst_name(documentSnapshots.getString(Constant.KEY_FIRST_NAME));
                            user.setLast_name(documentSnapshots.getString(Constant.KEY_LAST_NAME));
                            user.setDob(documentSnapshots.getString(Constant.KEY_DOB));
                            user.setGender(documentSnapshots.getString(Constant.KEY_GENDER));
                            user.setCountry(documentSnapshots.getString(Constant.KEY_COUNTRY));
                            user.setState(documentSnapshots.getString(Constant.KEY_STATE));
                            user.setHometown(documentSnapshots.getString(Constant.KEY_HOMETOWN));
                            user.setPhone(documentSnapshots.getString(Constant.KEY_PHONE));
                            user.setTelephone(documentSnapshots.getString(Constant.KEY_TELEPHONE));
                            user.setImage(documentSnapshots.getString(Constant.KEY_IMAGE));
                            user.setTime(documentSnapshots.getString(Constant.KEY_TIME));
                            userList.add(user);
                        }
                        if (userList.size()>0){
                            recyclerViewAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getContext(),"No User Found",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getContext(),"No User Found",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        getUsers();

    }

    private void displayLoader() {
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }
}
