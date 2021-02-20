package com.example.intership;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class Enroll extends Fragment implements FragmentLifecycle {
    private Button addUser;
    private EditText firstName;
    private EditText lastName;
    private EditText dateofBirth;
    private EditText gender;
    private EditText country;
    private EditText state;
    private EditText hometown;
    private EditText phone;
    private EditText telephone;
    private ImageView selectImage;
    private String Firstname,Lastname,DOB,Gender,Country,State,Hometown,Phone,Telephone;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    private Context applicationContext;
    FirebaseFirestore database;
    FirebaseStorage storage;
    StorageReference storageReference;

    public Enroll() {
        // required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database=FirebaseFirestore.getInstance();
        applicationContext = MainActivity.getContextOfApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_enroll, container, false);
        addUser=(Button)view.findViewById(R.id.btnAddUser);
        selectImage=(ImageView)view.findViewById(R.id.selectImage);
        firstName=(EditText)view.findViewById(R.id.etFirstName);
        lastName=(EditText)view.findViewById(R.id.etLastName);
        dateofBirth=(EditText)view.findViewById(R.id.etDateOfBirth);
        gender=(EditText)view.findViewById(R.id.etGender);
        country=(EditText)view.findViewById(R.id.etCountry);
        state=(EditText)view.findViewById(R.id.etState);
        hometown=(EditText)view.findViewById(R.id.etHomeTown);
        phone=(EditText)view.findViewById(R.id.etPhoneNumber);
        telephone=(EditText)view.findViewById(R.id.etTelephoneNumber);

        selectImage.setOnClickListener(view1 -> SelectImage());

        addUser.setOnClickListener(view12 -> {
            Firstname=firstName.getText().toString().trim();
            Lastname=lastName.getText().toString().trim();
            DOB=dateofBirth.getText().toString().trim();
            Gender=gender.getText().toString().trim();
            Country=country.getText().toString().trim();
            State=state.getText().toString().trim();
            Hometown=hometown.getText().toString().trim();
            Phone=phone.getText().toString().trim();
            Telephone=telephone.getText().toString().trim();
            if(validateInputs()){
                checkUser();
            }
        });
        return view;
    }

    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(applicationContext.getContentResolver(), filePath);
                selectImage.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = applicationContext.getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    private void checkUser(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS).whereEqualTo(Constant.KEY_PHONE,Phone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().getDocuments().size()>0){
                                Toast.makeText(applicationContext, "Phone Number Already Exists", Toast.LENGTH_SHORT).show();
                            }else {
                                uploadImage();
                            }
                        }
                    }
                });

    }
    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.setMessage("Adding... User");
            progressDialog.show();
            final String time=String.valueOf(System.currentTimeMillis());


            StorageReference ref = storageReference.child(
                    Constant.KEY_IMAGE_STORAGE + time + "." + GetFileExtension(filePath));

            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri location=uri;

                            FirebaseFirestore database=FirebaseFirestore.getInstance();
                            HashMap<String,Object> user=new HashMap<>();
                            user.put(Constant.KEY_FIRST_NAME,Firstname);
                            user.put(Constant.KEY_LAST_NAME,Lastname);
                            user.put(Constant.KEY_DOB,DOB);
                            user.put(Constant.KEY_GENDER,Gender);
                            user.put(Constant.KEY_COUNTRY,Country);
                            user.put(Constant.KEY_STATE,State);
                            user.put(Constant.KEY_HOMETOWN,Hometown);
                            user.put(Constant.KEY_PHONE,Phone);
                            user.put(Constant.KEY_TELEPHONE,Telephone);
                            user.put(Constant.KEY_IMAGE,location.toString());
                            user.put(Constant.KEY_TIME,time);
                            database.collection(Constant.KEY_COLLECTION_USERS).document(time)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        emptyFields();
                                        Toast.makeText(applicationContext, "User Added Successfully", Toast.LENGTH_SHORT).show();

                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(applicationContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }else{
            Toast.makeText(applicationContext, "Image Not Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean validateInputs() {
        if (Firstname.isEmpty()) {
            firstName.setError("First Name cannot be empty");
            firstName.requestFocus();
            return false;

        }
        if (Lastname.isEmpty()) {
            lastName.setError("Lastname cannot be empty");
            lastName.requestFocus();
            return false;
        }
        if (DOB.isEmpty()) {
            dateofBirth.setError("Date of Birth cannot be empty");
            dateofBirth.requestFocus();
            return false;
        }

        if (Gender.isEmpty()) {
            gender.setError("Gender cannot be empty");
            gender.requestFocus();
            return false;
        }

        if (Country.isEmpty()) {
            country.setError("Country cannot be empty");
            country.requestFocus();
            return false;
        }
        if (State.isEmpty()) {
            state.setError("State cannot be empty");
            state.requestFocus();
            return false;
        }

        if (Hometown.isEmpty()){
            hometown.setError("Hometown cannot be empty");
            hometown.requestFocus();
            return false;
        }
        if (Phone.isEmpty()){
            phone.setError("Phone Number cannot be empty");
            phone.requestFocus();
            return false;
        }
        if (Telephone.isEmpty()){
            telephone.setError("Telephone Number cannot be empty");
            telephone.requestFocus();
            return false;
        }

        if(!isValidFormat("dd/MM/yyyy", DOB)){
            dateofBirth.setError("Enter Valid Date oF Birth DD/MM/YYYY");
            dateofBirth.requestFocus();
            return false;
        }
        if (Gender.equals("Male")||Gender.equals("male")||Gender.equals("Female")||Gender.equals("female")){

        }else{
            gender.setError("Gender Should be Male or Female");
            gender.requestFocus();
            return false;
        }
        return true;
    }

    private void emptyFields(){
        firstName.setText("");
        lastName.setText("");
        dateofBirth.setText("");
        gender.setText("");
        country.setText("");
        state.setText("");
        hometown.setText("");
        phone.setText("");
        telephone.setText("");
        selectImage.setImageResource(R.drawable.ic_image);
    }


    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        emptyFields();

    }
}

