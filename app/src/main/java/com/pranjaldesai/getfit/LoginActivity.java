package com.pranjaldesai.getfit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @BindView(R.id.gender_selection2)
    RelativeLayout relativeLayout;
    @BindView(R.id.signin_description)
    TextView signInDescription;
    @BindView(R.id.signin_title)
    TextView signInTitle;
    @BindView(R.id.signin_button)
    SignInButton button;
    @BindView(R.id.gender_toggle2)
    StickySwitch genderToggle;
    String gender= "Male";
    FirebaseUser currentUser;
    @BindView(R.id.accountCreationProgressBar2)
    ProgressBar progressBar;
    @BindView(R.id.signin_finish)
    FloatingActionButton floatingActionButton;
    boolean userCreation;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setupWindowAnimations();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestScopes(Fitness.SCOPE_ACTIVITY_READ_WRITE, Fitness.SCOPE_NUTRITION_READ_WRITE, Fitness.SCOPE_BODY_READ_WRITE)
                .requestEmail()
                .build();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        genderToggle.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String text) {
                gender= text;
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser= mAuth.getCurrentUser();
                if(currentUser!=null) {
                    createUser(currentUser);
                }
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                }
            }
        }
    }

    private void setupWindowAnimations(){
        Slide slide= new Slide();
        slide.setDuration(1000);
        Fade fade= new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(fade);
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            SharedPreferences.Editor sharedPreferencesEditor =
                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                            sharedPreferencesEditor.putBoolean(
                                    getString(R.string.googleFit), true);
                            sharedPreferencesEditor.apply();
                            relativeLayout.setVisibility(View.VISIBLE);
                            button.setVisibility(View.GONE);
                            signInTitle.setText(getString(R.string.intro_four_title));
                            signInDescription.setText(getString(R.string.intro_four_description));
                            floatingActionButton.setVisibility(View.VISIBLE);
                        }else {
                            Snackbar.make(findViewById(R.id.main_layout), getString(R.string.authentication_fail), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean createUser(FirebaseUser currentUser){
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> newUserObject = new HashMap<>();
        newUserObject.put(getString(R.string.user_name), currentUser.getDisplayName());
        newUserObject.put(getString(R.string.user_email), currentUser.getEmail());
        newUserObject.put(getString(R.string.user_phoneNumber), currentUser.getPhoneNumber());
        newUserObject.put(getString(R.string.user_url), currentUser.getPhotoUrl().toString());
        newUserObject.put(getString(R.string.user_uid), currentUser.getUid());
        newUserObject.put(getString(R.string.user_gender), gender);


        db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                .update(newUserObject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userCreation=true;
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userCreation=false;
                        Snackbar.make(findViewById(R.id.main_layout), getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG).show();
                    }
                });

        return userCreation;
    }
}
