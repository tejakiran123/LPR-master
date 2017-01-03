package com.example.aditya.lpr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.aditya.lpr.constants.MyAppPreferences;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = LoginActivity.class.getSimpleName();
    private final int REQUEST_CODE_FOR_GOOGLE_SIGN_IN = 9001; // Some random number

    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private Context context;

    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        registerWithGoogleServices();
        registerWithFacebookServices();
    }

    private void registerWithGoogleServices() {
        // Google SignIn code

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setScopes(gso.getScopeArray());
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    private void registerWithFacebookServices() {
        // Facebook SignIn code

        // Generate KeyHash programmatically
        // If we use the keyhash generated from command line the facebook login won't function properly
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.aditya.lpr",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(TAG, "KeyHash for facebook: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Initialize facebook SDK so that we can use the methods present in it to perform our operations
        FacebookSdk.sdkInitialize(context);

        // Create a callback manager object so that we can handle callbacks
        mCallbackManager = CallbackManager.Factory.create();

        // Get a reference to login button and set permissions on it
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email"));
        // Registering the callback
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                // Logging the JSON response
                                Log.d(TAG,"JSON RESPONSE: " + object);

                                try {
                                    String id = object.getString("id");
                                    String name = object.getString("name");
                                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    String email = object.getString("email");

                                    // Update the sharedPreferences object to reflect the changes
                                    // i.e. we need to update the id, display_name etc so that we can use them
                                    // later while rendering the UI in MainActivity

                                    SharedPreferences sharedPreferences = PreferenceManager.
                                            getDefaultSharedPreferences(context);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(MyAppPreferences.LOGGED_IN_VIA_FACEBOOK,true);
                                    editor.putString(MyAppPreferences.FACEBOOK_DISPLAY_NAME ,name);
                                    editor.putString(MyAppPreferences.FACEBOOK_ID,id);
                                    editor.putString(MyAppPreferences.FACEBOOK_DISPLAY_PICTURE,image);
                                    editor.putString(MyAppPreferences.FACEBOOK_EMAIL_ID,email);
                                    editor.apply();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        });

                // Execute Asynchronously
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,email,picture");
                request.setParameters(parameters);
                request.executeAsync();

                // Start the MainActivity after logging in from facebook
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                // Don't forget to destroy this activity, otherwise when the
                // user clicks on the back button when in MainActivity, this activity shows up
                finish();

            }

            @Override
            public void onCancel() {
                Log.d(TAG,"onCancel method called in MyFacebookCallback!");
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(),"Sorry, there was an error signing in with facebook",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Forward the result to mCallbackManager
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_FOR_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {

        // Log the result
        Log.d(TAG, "handleGoogleSignInResult:" + result.isSuccess());

        if (result.isSuccess()) {
            // Signed in successfully.
            GoogleSignInAccount acct = result.getSignInAccount();

            // Get a reference to the SharedPreferences object and update the details
            // such as google_id, google_display_name so that we can use these details later
            // while updating the UI in MainActivity

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(MyAppPreferences.LOGGED_IN_VIA_GOOGLE,true);
            editor.putString(MyAppPreferences.GOOGLE_ID,acct.getId());
            editor.putString(MyAppPreferences.GOOGLE_DISPLAY_NAME,acct.getDisplayName());
            editor.putString(MyAppPreferences.GOOGLE_EMAIL_ID,acct.getEmail());
            editor.putString(MyAppPreferences.GOOGLE_DISPLAY_PICTURE,acct.getPhotoUrl().toString());
            editor.apply();


            // Start the MainActivity and finish() the current activity
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            // Sign in Failed
            Toast.makeText(context,R.string.google_sign_in_fail,Toast.LENGTH_SHORT).show();
        }
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_FOR_GOOGLE_SIGN_IN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                googleSignIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onBackPressed() {

        // Display an Alert Dialog to the user

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit LPR?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {}
                })
                .show();
    }

}
