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
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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

import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;

public class ProfileFragment extends Fragment {
    public View fragmentView;
    private ImageView imageView;
    private EditText username;
    private EditText email;
    private EditText name;
    private DatabaseReference myRef;
    private FirebaseUser currentUser;

    private FirebaseAuth mAuth;

    private ImageButton changeEmail;
    private ImageButton changeUsername;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        imageView = fragmentView.findViewById(R.id.profileImage);

        SharedPreferences pref = new ObscuredSharedPreferences(fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", 0));
        String userId = pref.getString("UserKey", "");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        MainPage.setFragment(ProfileFragment.this);
        ArrayList<Object> values = new ArrayList<>();
        myRef = database.getReference().child(userId);

        email = fragmentView.findViewById(R.id.profileEmail);
        username = fragmentView.findViewById(R.id.username);
        changeEmail = fragmentView.findViewById(R.id.changeEmail);
        changeUsername = fragmentView.findViewById(R.id.changeUsername);

        email.setEnabled(false);
        username.setEnabled(false);

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                alert.setMessage("Enter Your Message");
                alert.setTitle("Change Username");

                alert.setView(edittext);

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String usernameString = edittext.getText().toString();
                        database.getReference().child(userId+ "/username").setValue(usernameString);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }

        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                alert.setMessage("Enter Your Message");
                alert.setTitle("Change Email");

                alert.setView(edittext);

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String emailString = edittext.getText().toString();
                        database.getReference().child(userId+ "/email").setValue(emailString);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        });



        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);



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
