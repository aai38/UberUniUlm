package de.uni_ulm.uberuniulm;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class StartPage  extends AppCompatActivity {
    LinearLayout loginDialog;
    Gender gender= Gender.FEMALE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_page);

        loginDialog= (LinearLayout) findViewById(R.id.startActivityLoginContentContainer);
    }

    public void onStartActivityLoginBttn(View v){
        if(tryLogin()){
            Intent intent = new Intent(StartPage.this, MainPage.class);
            startActivity(intent);
        }
    }

    public void onStartActivityLoginRegisterBttn(View v){
        LinearLayout registerDialog=(LinearLayout) findViewById(R.id.startActivityRegisterContentContainer);
        registerDialog.setVisibility(View.VISIBLE);
        loginDialog.setVisibility(View.INVISIBLE);
    }

    public void onStartActivityLoginForgotPasswordBttn(View v){

    }

    public void onStartActivityRegisterCameraBttn(View v){

    }

    public void onStartActivityRegisterGenderBttn(View v){
        int id=v.getId();
        ImageButton maleIcon= (ImageButton) findViewById(R.id.startActivityRegisterMaleGenderBttn);
        ImageButton femaleIcon=(ImageButton) findViewById(R.id.startActivityRegisterFemaleGenderBttn);
        ImageButton transIcon= (ImageButton) findViewById(R.id.startActivityRegisterTransGenderBttn);

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
        Boolean logInSuccessful=true;
        Editable username= ((EditText) findViewById(R.id.startActivityLoginNameTextInput)).getText();
        Editable password= ((EditText) findViewById(R.id.startActivityLoginPasswordTextInput)).getText();


        //Check if valid name or mail

        //Check if right password

        //Login if right input

        return logInSuccessful;
    }

    private Boolean checkRegistration(){
        Boolean isValid=true;
        Editable username= ((EditText) findViewById(R.id.startActivityRegisterNameTextInput)).getText();
        Editable mailAdress= ((EditText) findViewById(R.id.startActivityRegisterMailTextInput)).getText();
        Editable password= ((EditText) findViewById(R.id.startActivityRegisterPasswordTextInput)).getText();

        //Check if valid name

        //check if valid mail address

        //check if valid password

        //create user if true

        return isValid;
    }

    public enum Gender {
        FEMALE, MALE, TRANSGENDER;
    }
}
