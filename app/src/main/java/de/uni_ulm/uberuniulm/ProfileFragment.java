package de.uni_ulm.uberuniulm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    public View fragmentView;
    private ImageView imageView;
    private EditText username;
    private EditText email;
    private EditText name;
    private DatabaseReference myRef;
    private FirebaseUser currentUser;

    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        imageView = fragmentView.findViewById(R.id.profileImage);

        SharedPreferences pref = getContext().getSharedPreferences("UserKey", 0);
        String userId = pref.getString("UserKey", "");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        MainPage.setFragment(ProfileFragment.this);
        ArrayList<Object> values = new ArrayList<>();
        myRef = database.getReference().child(userId);

        email = fragmentView.findViewById(R.id.profileEmail);
        username = fragmentView.findViewById(R.id.username);

        /*email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child(userId+"/email").setValue(editable.toString());
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child(userId+"/username").setValue(editable.toString());
            }
        });
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        */

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                    values.add(childSnapshot.getValue());
                }
                if(values.size() == 5) {
                    username.setText(values.get(4).toString());
                } else if (values.size() == 6) {
                    username.setText(values.get(5).toString());
                } else {
                    username.setText(values.get(6).toString());
                }
                email.setText(values.get(0).toString());

            }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+currentUser.getUid()+".jpg");
        profileImageRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "can not load image", Toast.LENGTH_SHORT).show();
            }
        });


        return fragmentView;
    }

    public void setImage(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    public void setImagePath (String path) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
    }


}
