package com.example.aditya.lpr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.media.tv.TvInputService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.aditya.lpr.adapters.ListAdapter;
import com.example.aditya.lpr.constants.MyAppPreferences;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = CameraActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions mGoogleSignInOptions;

    private static final int MY_PERMISSIONS_REQUEST = 1234;
    // Some random number to be used by the callback onRequestPermissionsResult

    private static Context context;
    private static boolean allPermissionsGranted = false;

    private ListView mListView;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mListView = (ListView) findViewById(R.id.license_plate_listview);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Instantiate google and facebook services.
        // These are required when the user performs the logout operation from the NavMenu
        instantiateGoogleServices();
        instantiateFacebookServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<String> imagePaths = getFromSdcard(); // Populate the arraylist with paths
        adapter = new ListAdapter(MainActivity.this,R.layout.list_view_item,imagePaths);
        mListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissionsRequiredForTheApp();
                if(allPermissionsGranted) {
                    Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                    startActivity(intent);
                }
            }
        });
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        drawer.setDrawerListener(toggle);
        navigationView.setNavigationItemSelectedListener(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,ImageDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // remove all the listeners
        fab.setOnClickListener(null);
        navigationView.setNavigationItemSelectedListener(null);
        mListView.setOnItemClickListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            logout(); // Perform logout opertion
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public ArrayList<String> getFromSdcard() {
        ArrayList<String> imgPaths = new ArrayList<String>();

        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Pictures/LPRApp");
        Log.v(LOG_TAG,"External storage returned is: " + Environment.getExternalStorageDirectory());
        if (file.isDirectory()) {

            Log.v(LOG_TAG,"Found the directory LPRApp");

            File[] listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                imgPaths.add(listFile[i].getAbsolutePath());
                Log.v(LOG_TAG,"Image path " + i + ": " + imgPaths.get(i));
            }
        } else {
            Log.v(LOG_TAG,"Didn't find the directory!!");
        }

        return imgPaths;
    }


    private void instantiateGoogleServices() {

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();
    }

    private void instantiateFacebookServices() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
    }

    private void logout() {

        // Get a reference to the SharedPreferences object
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        // Determine whether the user is logged in via facebook or google

        if(sharedPref.getBoolean(MyAppPreferences.LOGGED_IN_VIA_GOOGLE,false)) {

            // If the user if logged in via google
            // then set the LOGGED_IN_VIA_GOOGLE shared preference to false

            editor.putBoolean(MyAppPreferences.LOGGED_IN_VIA_GOOGLE,false);
            // Perform google logout operations
            googleSignOut();

        } else if(sharedPref.getBoolean(MyAppPreferences.LOGGED_IN_VIA_FACEBOOK,false)) {

            // If the user if logged in via facebook
            // then set the LOGGED_IN_VIA_FACEBOOK shared preference to false

            editor.putBoolean(MyAppPreferences.LOGGED_IN_VIA_FACEBOOK,false);
            // Perform facebook logout operations
            facebookSignOut();
        }

        editor.apply();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Toast.makeText(getApplicationContext(),"Successfully logged out",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void facebookSignOut() {
        LoginManager.getInstance().logOut();
        Toast.makeText(getApplicationContext(),"Successfully logged out",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),"Error logging out",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        // When the user clicks on back button, first determine
        // whether the drawerLayout is open or not

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // If it is open then close it
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Else pop up an Alter Dialog and ask user for confirmation to quit the app
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

    private void requestPermissionsRequiredForTheApp() {

        // Request permission to use camera
        // Request permission to use external storage for saving images
        // Request permission to acccess the user's GPS coordinates
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            allPermissionsGranted = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    allPermissionsGranted = true;

                } else {
                    // permission not granted
                    Toast.makeText(context,"Please grant the permission to proceed",Toast.LENGTH_SHORT).show();
                    allPermissionsGranted = false;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}