package com.example.remindmeat.View;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.transition.TransitionInflater;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remindmeat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author Patel Dhruv
 * @author Gakhar Tanvi
 * @author Kaur Sarbjit
 * @author Kaur Kamaljit
 * @author Varma Akshay
 * @author Dankhara Chintan
 * @author Karthik Modubowna
 * This java class is for {@link LoginFragment}
 */
public class LoginFragment extends Fragment {

    /**
     * Variables of TextView
     */
    TextView txt_reg,txt_forgot,txt_title,txt_tag;
    /**
     * variables of material TextInputLayout
     */
    TextInputLayout txt_layEmail,txt_layPass;
    /**
     * Variable of login button
     */
    Button btn_login;
    /**
     * Variable of String type for storing email, password.
     */
    String email,pass;
    /**
     * Object of {@link FirebaseAuth}
     */
    FirebaseAuth auth;
    /**
     * Variable of LinearLayout
     */
    LinearLayout layout_bottom;
    /**
     * Variable og ImageView for logo
     */
    ImageView img_logo;
    /**
     * Object of {@link FirebaseFirestore}
     */
    FirebaseFirestore db;
    /**
     * Final variable of int
     */
    private final int REQUEST_LOCATION_PERMISSION = 1;

    /**
     * Default constructor
     */
    public LoginFragment() {
        // Required empty public constructor
    }


    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setSharedElementEnterTransition(TransitionInflater.from(getContext())
                .inflateTransition(android.R.transition.move));

    }

    /**
     * onViewCreated
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_reg=view.findViewById(R.id.txt_loginRegister);
        txt_forgot=view.findViewById(R.id.txt_loginForgot);
        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        txt_layEmail=view.findViewById(R.id.edt_loginEmail);
        txt_layPass=view.findViewById(R.id.edt_loginPassword);
        btn_login=view.findViewById(R.id.btn_login);
        txt_title=view.findViewById(R.id.txt_loginTitle);
        txt_tag=view.findViewById(R.id.txt_loginHello);
        layout_bottom=view.findViewById(R.id.layout_bottom);
        img_logo=view.findViewById(R.id.img_loginLogo);
        btn_login.setOnClickListener(login);
        txt_forgot.setOnClickListener(forgot);
        txt_reg.setOnClickListener(newUser);
        requestLocationPermission();
    }

    /**
     * Click listener for forgot password textview.
     */
    View.OnClickListener forgot=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_main);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(img_logo, "logo_image")
                    .addSharedElement(txt_title, "logo_text")
                    .addSharedElement(txt_tag, "logo_tag")
                    .addSharedElement(txt_layEmail, "logo_email")
                    .addSharedElement(btn_login, "logo_btn")
                    .addSharedElement(layout_bottom, "login_signup")
                    .build();
            navController.navigate(R.id.forgotFragment,null,null,extras);
        }
    };

    /**
     * onRequestPermissionsResult method for getting result from permission request dialog.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * requestLocationPermission of easy permission library
     */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(EasyPermissions.hasPermissions(getActivity(), perms)) {
            Toast.makeText(getActivity().getApplicationContext(), "Permission already granted", Toast.LENGTH_SHORT).show();

        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }


    /**
     * Click listener for login button
     */
    View.OnClickListener login=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            email=txt_layEmail.getEditText().getText().toString().trim();
            pass=txt_layPass.getEditText().getText().toString().trim();

            if (!checkEmptyField()){
                AuthCredential credential = EmailAuthProvider.getCredential(email,pass);

                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            checkIfAdmin(auth.getCurrentUser().getUid());

                        }else{
                            try{
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e){
                                Toast.makeText(getActivity().getApplicationContext(),"Email not exist!",Toast.LENGTH_LONG).show();
                                txt_layEmail.getEditText().getText().clear();
                                txt_layPass.getEditText().getText().clear();
                                txt_layEmail.setError("Email not exist!");
                                txt_layEmail.getEditText().requestFocus();
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                // Toast.makeText(getActivity().getApplicationContext(),"Wrong Password!",Toast.LENGTH_LONG).show();
                                txt_layEmail.getEditText().getText().clear();
                                txt_layPass.getEditText().getText().clear();
                                txt_layEmail.setError("Please enter valid login credentials");
                                txt_layEmail.getEditText().requestFocus();
                            }catch (Exception e ){
                                Toast.makeText(getActivity().getApplicationContext(),"Login Failed!",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

            }

        }
    };

    /**
     * method for checking for user type(Admin/User).
     * @param uid
     */
    private void checkIfAdmin(String uid) {

        db.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("isAdmin")!=null){
                   // Toast.makeText(getActivity().getApplicationContext(),"Admin Login Success!",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),AdminActivity.class);
                    startActivity(intent);
                }else  {
                    //Toast.makeText(getActivity().getApplicationContext(),"User Login Success!",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),DashActivity.class);
                    startActivity(intent);
                }

            }
        });

    }


    /**
     * Click listener for navigation to register page
     */
    View.OnClickListener newUser=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_main);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(img_logo, "logo_image")
                    .addSharedElement(txt_title, "logo_text")
                    .addSharedElement(txt_tag, "logo_tag")
                    .addSharedElement(txt_layEmail, "logo_email")
                    .addSharedElement(txt_layPass, "logo_pass")
                    .addSharedElement(btn_login, "logo_btn")
                    .addSharedElement(layout_bottom, "login_signup")
                    .build();
            navController.navigate(R.id.registrationFragment,null,null,extras);
        }
    };

    /**
     * onCreateView
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * method for checking validations
     * @return
     */
    public boolean checkEmptyField() {

        if (TextUtils.isEmpty(email)) {
            txt_layEmail.setError("Email Cannot be Empty!");
            txt_layEmail.requestFocus();
            return true;
        } else if (TextUtils.isEmpty(pass)) {
            txt_layEmail.setError(null);
            txt_layEmail.setErrorEnabled(false);
            txt_layPass.setError("Password Cannot be Empty!");
            txt_layPass.requestFocus();
            return true;
        }else if (pass.length()<6){
            txt_layPass.getEditText().getText().clear();
            txt_layPass.setError("Password Cannot less than 6 characters");
            txt_layPass.requestFocus();
            return true;
        }
        txt_layPass.setErrorEnabled(false);
        return false;
    }

    /**
     * onStart for checking if user is already login or not.
     */
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user=auth.getCurrentUser();
        if (user!=null){
            checkIfAdmin(user.getUid());

        }
    }
}