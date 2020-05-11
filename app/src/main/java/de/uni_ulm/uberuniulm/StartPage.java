package de.uni_ulm.uberuniulm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartPage  extends AppCompatActivity {
    LinearLayout loginDialog;
    Gender gender= Gender.FEMALE;
    SharedPreferences pref;
    private FirebaseAuth mAuth;
    private Boolean isValid = false;
    private Boolean logInSuccessful = false;
    private FirebaseUser currentUser;
    private ImageButton maleIcon;
    private ImageButton femaleIcon;
    private ImageButton transIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_page);

        loginDialog= (LinearLayout) findViewById(R.id.startActivityLoginContentContainer);
        mAuth = FirebaseAuth.getInstance();

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
                            User exampleUser = new User(null, mailAdress.toString(), gender.toString(), "bla.png", null, null, -1, new Settings("de", "black"), username.toString());
                            //myRef.push().setValue(exampleUser);
                            //String key = myRef.getKey();

                            DatabaseReference ref = myRef.push();
                            ref.setValue(exampleUser);
                            String key = ref.getKey();
                            pref = getApplicationContext().getSharedPreferences("UserKey", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            Log.i("key", ""+key);
                            editor.putString("UserKey", key);

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

    public enum Gender {
        FEMALE, MALE, TRANSGENDER;
    }

    public void updateUI (FirebaseUser actualUser) {
        currentUser = actualUser;
    }
}
