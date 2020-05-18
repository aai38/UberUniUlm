package de.uni_ulm.uberuniulm;

import android.Manifest;
import android.app.AlertDialog;
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
import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;

import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import java.io.ByteArrayOutputStream;

import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;

public class StartPage  extends AppCompatActivity {
    LinearLayout loginDialog;
    Gender gender= Gender.FEMALE;
    SharedPreferences pref;
    private FirebaseAuth mAuth;
    private Boolean isValid = false, newProfilePhotoSet=false;
    private Boolean logInSuccessful = false;
    private FirebaseUser currentUser;
    private ImageButton maleIcon;
    private ImageButton femaleIcon;
    private ImageButton transIcon;
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

    }

    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void onStartActivityLoginBttn(View v){
        if(tryLogin()){
            Intent intent = new Intent(StartPage.this, MainPage.class);
            startActivity(intent);
        } else {
           /* Log.d("TAG", "signInWithEmail:failure");
            Toast.makeText(StartPage.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();*/
        }
    }

    public void onStartActivityLoginRegisterBttn(View v){
        LinearLayout registerDialog=(LinearLayout) findViewById(R.id.startActivityRegisterContentContainer);
        registerDialog.setVisibility(View.VISIBLE);
        loginDialog.setVisibility(View.INVISIBLE);
    }

    public void onStartActivityLoginForgotPasswordBttn(View v){
        Editable username= ((EditText) findViewById(R.id.startActivityLoginNameTextInput)).getText();
        mAuth.sendPasswordResetEmail(username.toString())
                .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(this, "Unable to send reset mail, please enter your mail first", Toast.LENGTH_LONG)
                        .show();
            }
        });
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
        if(checkRegistration()){
            Intent intent = new Intent(StartPage.this, MainPage.class);
            startActivity(intent);
        }
    }

    private Boolean tryLogin(){

        Editable username= ((EditText) findViewById(R.id.startActivityLoginNameTextInput)).getText();
        Editable password= ((EditText) findViewById(R.id.startActivityLoginPasswordTextInput)).getText();


        mAuth.signInWithEmailAndPassword(username.toString(), password.toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        logInSuccessful = true;


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.getException());
                        Toast.makeText(StartPage.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                        logInSuccessful = false;
                    }
                });


        return logInSuccessful;
    }

    private Boolean checkRegistration(){
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
                            isValid = true;
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference();

                            ArrayList offeredRide = new ArrayList<OfferedRide>();
                            ArrayList bookedRide = new ArrayList<BookedRide>();
                            User exampleUser = new User(bookedRide, mailAdress.toString(), gender.toString(), "bla.png", offeredRide, null, -1, username.toString());
                            //myRef.push().setValue(exampleUser);
                            //String key = myRef.getKey();

                            DatabaseReference ref = myRef.push();
                            ref.setValue(exampleUser);
                            if(newProfilePhotoSet){
                                uploadProfilePhoto();
                            }
                            String key = ref.getKey();
                            pref = getApplicationContext().getSharedPreferences("UserKey", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            Log.i("key", ""+key);
                            editor.putString("UserKey", key);
                            editor.putInt("RideId", 0);
                            editor.apply();

                        } else {
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(StartPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            isValid = false;
                        }
                    }
                });



        return isValid;
    }

    private Boolean uploadProfilePhoto(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+currentUser.getUid()+".jpg");

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
}
