package com.pranjaldesai.getfit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.github.paolorotolo.appintro.ISlidePolicy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View.OnClickListener;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import io.ghyeok.stickyswitch.widget.StickySwitch;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment implements ISlidePolicy {

    private FirebaseAuth mAuth;
    RelativeLayout relativeLayout;
    TextView signInDescription, signInTitle;
    SignInButton button;
    StickySwitch genderToggle;
    View view;
    String gender= "Male";
    FirebaseUser currentUser;
    ProgressBar progressBar;
    boolean userCreation;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    public SignInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_sign_in, container, false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestScopes(Fitness.SCOPE_ACTIVITY_READ_WRITE, Fitness.SCOPE_NUTRITION_READ_WRITE, Fitness.SCOPE_BODY_READ_WRITE)
                .requestEmail()
                .build();

        relativeLayout = (RelativeLayout)view.findViewById(R.id.gender_selection);
        signInDescription = (TextView)view.findViewById(R.id.sign_in_description);
        signInTitle = (TextView)view.findViewById(R.id.sign_in_title);
        genderToggle = (StickySwitch)view.findViewById(R.id.gender_toggle);
        button= view.findViewById(R.id.sign_in_button);
        progressBar= view.findViewById(R.id.accountCreationProgressBar);

        button.setOnClickListener(new OnClickListener() {
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

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mAuth = FirebaseAuth.getInstance();

        return view;
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

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            SharedPreferences.Editor sharedPreferencesEditor =
                                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                            sharedPreferencesEditor.putBoolean(
                                    getResources().getString(R.string.googleFit), true);
                            sharedPreferencesEditor.apply();
                            relativeLayout.setVisibility(View.VISIBLE);
                            button.setVisibility(View.GONE);
                            signInTitle.setText(getString(R.string.intro_four_title));
                            signInDescription.setText(getString(R.string.intro_four_description));
                        }else {
                            Snackbar.make(view.findViewById(R.id.main_layout), getString(R.string.authentication_fail), Snackbar.LENGTH_SHORT).show();
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
                .set(newUserObject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userCreation=true;
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userCreation=false;
                        Snackbar.make(view.findViewById(R.id.main_layout), getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG).show();
                    }
                });

        return userCreation;
    }


    @Override
    public boolean isPolicyRespected() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            return createUser(currentUser);
        }
        return false;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            Snackbar.make(view.findViewById(R.id.main_layout), getString(R.string.snackbar_sign_in), Snackbar.LENGTH_LONG).show();
        }
    }
}
