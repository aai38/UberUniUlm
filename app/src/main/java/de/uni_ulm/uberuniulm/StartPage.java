package de.uni_ulm.uberuniulm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import de.uni_ulm.uberuniulm.model.notifications.NotificationsManager;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.User;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import java.io.ByteArrayOutputStream;

import static android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS;

public class StartPage  extends AppCompatActivity {
    LinearLayout loginDialog;
    Gender gender= Gender.FEMALE;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private Boolean isValid = false, newProfilePhotoSet=false;
    private Boolean logInSuccessful = false;
    private FirebaseUser currentUser;
    private ImageButton maleIcon;
    private ImageButton femaleIcon;
    private ImageButton transIcon;
    private CheckBox loggedInCheckBox;
    private int REQUEST_CODE = 1;
    private Uri uri;
    private ImageView profilePhoto;

    Uri outPutfileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_page);

        loginDialog= (LinearLayout) findViewById(R.id.startActivityLoginContentContainer);
        mAuth = FirebaseAuth.getInstance();
        profilePhoto = (ImageView) findViewById(R.id.startActivityRegisterProfileImage);

        maleIcon= (ImageButton) findViewById(R.id.startActivityRegisterMaleGenderBttn);
        femaleIcon=(ImageButton) findViewById(R.id.startActivityRegisterFemaleGenderBttn);
        transIcon= (ImageButton) findViewById(R.id.startActivityRegisterTransGenderBttn);

        loggedInCheckBox=(CheckBox) findViewById(R.id.startActivityLoggedInCheckbox);

        pref = new ObscuredSharedPreferences(
                StartPage.this, StartPage.this.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        editor = pref.edit();

        if(pref.getBoolean("StayLoggedIn", false)){
            loggedInCheckBox.setChecked(true);
            String username=pref.getString("UserName", null);
            String password=pref.getString("UserPassword", null);


            if(!username.isEmpty()&& !password.isEmpty()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(username, password);

                // Prompt the user to re-provide their sign-in credentials
                if (user != null) {
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("RE-AUTHENTICATION", user.getEmail());
                                    NotificationsManager notificationsManager = new NotificationsManager();
                                    notificationsManager.setUp(getApplicationContext());
                                    Intent intent = new Intent(StartPage.this, MainPage.class);
                                    startActivity(intent);
                                }
                            });
                }
            }

        }

    }

    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void onStartActivityLoginBttn(View v){
        Editable username= ((EditText) findViewById(R.id.name)).getText();
        Editable password= ((EditText) findViewById(R.id.username)).getText();

        mAuth.signInWithEmailAndPassword(username.toString(), password.toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        logInSuccessful = true;
                        final String[] userkey = new String[1];

                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot child: dataSnapshot.getChildren()){
                                    Boolean test= child.child("email").getValue().toString().equals(username.toString());
                                    if(child.child("email").getValue().toString().equals(username.toString())) {
                                        userkey[0] = child.getKey();
                                        editor.putString("UserKey", userkey[0]);
                                        editor.putInt("RideId", (int) child.child("offeredRides").getChildrenCount());
                                        editor.apply();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.i("SHAREDPREFERROR", "Could not load userkey");
                            }
                        };

                        Query userkeyQuery= FirebaseDatabase.getInstance().getReference().child("Users");
                        userkeyQuery.addListenerForSingleValueEvent(valueEventListener);

                        if(loggedInCheckBox.isChecked()){
                            pref.edit().putBoolean("StayLoggedIn", true).apply();
                            pref.edit().putString("UserName", username.toString()).apply();
                            pref.edit().putString("UserPassword", password.toString()).apply();
                        }else{
                            pref.edit().putBoolean("StayLoggedIn", false).apply();
                        }
                        NotificationsManager notificationsManager = new NotificationsManager();
                        notificationsManager.setUp(getApplicationContext());
                        Intent intent = new Intent(StartPage.this, MainPage.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.getException());
                        Toast.makeText(StartPage.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                        logInSuccessful = false;
                    }
                });

    }

    public void onStartActivityLoginRegisterBttn(View v){
        LinearLayout registerDialog=(LinearLayout) findViewById(R.id.startActivityRegisterContentContainer);
        registerDialog.setVisibility(View.VISIBLE);
        loginDialog.setVisibility(View.INVISIBLE);
    }

    public void onStartActivityLoginForgotPasswordBttn(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(StartPage.this);
        final EditText edittext = new EditText(StartPage.this);
        edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edittext.setHint(AUTOFILL_HINT_EMAIL_ADDRESS);
        alert.setMessage("Enter Your E-mail");
        alert.setTitle("Forgot Password");

        alert.setView(edittext);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mAuth.sendPasswordResetEmail(edittext.getText().toString())
                        .addOnCompleteListener(StartPage.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(StartPage.this, "Reset link sent to your email", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(StartPage.this, "Unable to send reset mail, please enter your mail first", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();

    }

    public void onStartActivityRegisterCameraBttn(View v){
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(StartPage.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(StartPage.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(StartPage.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE);
                    }else{
                        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                    }

                } else if (options[item].equals("Choose from Gallery")) {
                    if (ContextCompat.checkSelfPermission(StartPage.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(StartPage.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }else{
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , 1);
                    }


                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void onStartActivityRemoveProfilePhotoBttn(View v){
        profilePhoto.setImageDrawable(getDrawable(R.drawable.start_register_profile_photo));
        newProfilePhotoSet=false;
        updateProfilePhotoResetBttn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        profilePhoto.setImageBitmap(selectedImage);
                        newProfilePhotoSet=true;
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                profilePhoto.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                newProfilePhotoSet=true;
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
            updateProfilePhotoResetBttn();
        }
    }

    private void updateProfilePhotoResetBttn(){
        ImageButton resetProfilePhotoBttn= findViewById(R.id.startActivityRemoveProfilePhotoBttn);
        if(newProfilePhotoSet){
            resetProfilePhotoBttn.setVisibility(View.VISIBLE);
        }else{
            resetProfilePhotoBttn.setVisibility(View.INVISIBLE);
        }
    }

    public void onStartActivityRegisterGenderBttn(View v){
        int id=v.getId();

            switch(id){
                case R.id.startActivityRegisterMaleGenderBttn:
                    if(gender!=Gender.MALE){
                        maleIcon.setImageResource(R.drawable.ic_male_selected);
                        femaleIcon.setImageResource(R.drawable.ic_female);
                        transIcon.setImageResource(R.drawable.ic_transgender);
                        gender=Gender.MALE;
                    }
                    break;
                case R.id.startActivityRegisterFemaleGenderBttn:
                    if(gender!=Gender.FEMALE){
                        maleIcon.setImageResource(R.drawable.ic_male);
                        femaleIcon.setImageResource(R.drawable.ic_female_selected);
                        transIcon.setImageResource(R.drawable.ic_transgender);
                        gender=Gender.FEMALE;
                    }
                    break;
                    case R.id.startActivityRegisterTransGenderBttn:
                    if(gender!=Gender.TRANSGENDER){
                        maleIcon.setImageResource(R.drawable.ic_male);
                        femaleIcon.setImageResource(R.drawable.ic_female);
                        transIcon.setImageResource(R.drawable.ic_transgender_selected);
                        gender=Gender.TRANSGENDER;
                    }
                    break;

            }
    }

    public void onStartActivityRegisterBttn(View v){
        Editable username= ((EditText) findViewById(R.id.startActivityRegisterNameTextInput)).getText();
        Editable mailAdress= ((EditText) findViewById(R.id.startActivityRegisterMailTextInput)).getText();
        Editable password= ((EditText) findViewById(R.id.startActivityRegisterPasswordTextInput)).getText();
        mAuth.createUserWithEmailAndPassword(mailAdress.toString(), password.toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference().child("Users");

                            ArrayList offeredRide = new ArrayList<OfferedRide>();
                            ArrayList bookedRide = new ArrayList<BookedRide>();
                            User exampleUser = new User(bookedRide, mailAdress.toString(), gender.toString(),  offeredRide, null, null, username.toString());
                            //myRef.push().setValue(exampleUser);
                            //String key = myRef.getKey();

                            DatabaseReference ref = myRef.push();
                            ref.setValue(exampleUser);
                            String key = ref.getKey();
                            if(newProfilePhotoSet){
                                uploadProfilePhoto(key);
                            }

                            Log.i("key", ""+key);
                            editor.putString("UserKey", key);
                            editor.putInt("RideId", 0);
                            editor.apply();
                            NotificationsManager notificationsManager = new NotificationsManager();
                            notificationsManager.setUp(getApplicationContext());
                            Intent intent = new Intent(StartPage.this, MainPage.class);
                            startActivity(intent);

                        } else {
                            TextView passwordHintText= (TextView) findViewById(R.id.startActivityRegisterPasswordInstruction);
                            passwordHintText.setText(R.string.start_login_password_hint);
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(StartPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);

                        }
                    }
                });
    }



    private Boolean uploadProfilePhoto(String userKey){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+userKey+".jpg");

        Boolean successfullyUpdated=false;
        profilePhoto.setDrawingCacheEnabled(true);
        profilePhoto.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePhoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w("TAG", "uploadProfileImage:failure", exception);
                Toast.makeText(StartPage.this, "Profile image upload failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.w("TAG", "uploadProfileImage:Success");
            }
        });

        return successfullyUpdated;
    }

    public enum Gender {
        FEMALE, MALE, TRANSGENDER;
    }

    public void updateUI (FirebaseUser actualUser) {
        currentUser = actualUser;
    }

    public void onRegisterPasswordHint(View v){
        TextView passwordHintText= (TextView) findViewById(R.id.startActivityRegisterPasswordInstruction);
        passwordHintText.setText(R.string.start_login_password_hint);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void OnRegisterLoginBttn (View v) {
        LinearLayout registerDialog=(LinearLayout) findViewById(R.id.startActivityRegisterContentContainer);
        registerDialog.setVisibility(View.INVISIBLE);
        loginDialog.setVisibility(View.VISIBLE);
    }
}
