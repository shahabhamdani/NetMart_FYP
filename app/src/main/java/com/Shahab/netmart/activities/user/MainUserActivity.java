package com.Shahab.netmart.activities.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.CustomPackageActivity;
import com.Shahab.netmart.R;
import com.Shahab.netmart.activities.CartActivity;
import com.Shahab.netmart.activities.authentication.LoginActivity;
import com.Shahab.netmart.activities.SettingsActivity;
import com.Shahab.netmart.activities.seller.MainSellerActivity;
import com.Shahab.netmart.adapters.AdapterOrderUser;
import com.Shahab.netmart.adapters.AdapterShop;
import com.Shahab.netmart.models.ModelOrderUser;
import com.Shahab.netmart.models.ModelShop;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainUserActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private TextView userName;
    private TextView userPhone;
    private TextView userEmail;
    private ImageView cartIv;
    private TextView  tabShopsTv;
    private TextView tabOrdersTv;

    private RelativeLayout shopsRl;
    private RelativeLayout ordersRl;
    private RecyclerView shopsRv;
    private RecyclerView ordersRv;
    private ImageView profileIv;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;
    LatLng myLatLng;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);


        tabShopsTv = (TextView) findViewById(R.id.tabShopsTv);
        tabOrdersTv = (TextView) findViewById(R.id.tabOrdersTv);
        shopsRl =  findViewById(R.id.shopsRl);
        ordersRl =  findViewById(R.id.ordersRl);
        shopsRv =  findViewById(R.id.shopsRv);
        ordersRv =  findViewById(R.id.ordersRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();



        checkUser();
        setUpToolbar();

        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent = null;
                switch (menuItem.getItemId()) {
                    case R.id.navProfile:
                        intent = new Intent(MainUserActivity.this, ProfileEditUserActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.navMyCart: {
                        intent = new Intent(MainUserActivity.this, CartActivity.class);
                        startActivity(intent);
                    }
                    break;

                    case R.id.navCustomPackage: {
                        intent = new Intent(MainUserActivity.this, CustomPackageActivity.class);
                        startActivity(intent);
                    }
                    break;

                    case R.id.navSetting: {
                        intent = new Intent(MainUserActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                    break;

                    case R.id.navSignOut: {
                        makeMeOffline();

                    }
                    break;

                }
                return false;
            }
        });

        //Cart Button Click
        cartIv = findViewById(R.id.cartIv);
        cartIv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainUserActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });


        //Products Tab Clcik
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showOrdersUI();
            }
        });

        tabShopsTv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                showProductsUI();
            }
        });

    }

    private void makeMeOffline() {

        //after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Updated Successfullly
                        firebaseAuth.signOut();

                        //if logged in Go to Dashboard else go to Login screen
                        checkUser();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed Updating
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void checkUser(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();

        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String email = ""+ds.child("email").getValue();
                            String city = ""+ds.child("city").getValue();


                            String lat = ""+ds.child("latitude").getValue();
                            String lng = ""+ds.child("longitude").getValue();
                            myLatLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));


                            userName = (TextView) findViewById(R.id.userNameTv);
                            userName.setText(name +" ("+accountType+")");

                            userPhone = (TextView) findViewById(R.id.phoneTv);
                            userPhone.setText(phone);

                            userEmail = (TextView) findViewById(R.id.emailTv);
                            userEmail.setText(email);

                            profileIv =  (ImageView) findViewById(R.id.hProfileIv);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_black).into(profileIv);
                            }
                            catch (Exception e){
                                Toast.makeText(MainUserActivity.this, "Add Profile Picture", Toast.LENGTH_SHORT).show();

                            }

                            loadShops(city);
                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }


    private void showOrdersUI() {
        //show orders ui and hide products ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colourBlack));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void showProductsUI() {
        //show orders ui and hide products ui
        ordersRl.setVisibility(View.GONE);
        shopsRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);


        tabOrdersTv.setTextColor(getResources().getColor(R.color.colourBlack));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }


    private void loadShops(final String myCity) {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();

                            //show only user city shops
                            if (shopCity.equals(myCity)){
                                shopsList.add(modelShop);
                            }

                            //shopsList.add(modelShop);
                        }


                        //setup adapter

                        ArrayList<ModelShop> sortedShopList = new ArrayList<>();
                        sortedShopList =findNearestShop(shopsList);

                        adapterShop = new AdapterShop(MainUserActivity.this, sortedShopList);

                        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainUserActivity.this,1, GridLayoutManager.VERTICAL, false);
                        shopsRv.setLayoutManager(gridLayoutManager);
                        //set adapter to recyclerview
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadOrders() {
        //init order list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //setup adapter

                                        ArrayList<ModelOrderUser> sortedOrderList = new ArrayList<ModelOrderUser>();

                                        sortedOrderList = sortOrderArrayListByDate(ordersList);
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, sortedOrderList);
                                        //set to recyclerview
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<ModelOrderUser> sortOrderArrayListByDate(ArrayList<ModelOrderUser> ordersList) {

        //bubble sort
        for (int i=ordersList.size()-1; i >= 0; i--){

            for (int j=1; j <= i; j++){

                if ( Double.parseDouble(ordersList.get(j-1).getOrderTime()) > Double.parseDouble(ordersList.get(j).getOrderTime())){

                    ModelOrderUser temp = new ModelOrderUser();
                    temp = ordersList.get(j-1);
                    ordersList.set((j-1),ordersList.get(j));
                    ordersList.set(j,temp);
                }
            }
        }
        return ordersList;
    }


    private ArrayList<ModelShop> findNearestShop(ArrayList<ModelShop> shopsList) {

        try {

            //bubble sort
            for (int i = (shopsList.size() - 1); i >= 0; i--) {

                for (int j = 1; j <= i; j++) {

                    if (calculateDistanceInKilometer(myLatLng.latitude, myLatLng.longitude, Double.parseDouble(shopsList.get(j - 1).getLatitude())
                            , Double.parseDouble(shopsList.get(j - 1).getLongitude())) >
                            calculateDistanceInKilometer(myLatLng.latitude, myLatLng.longitude, Double.parseDouble(shopsList.get(j).getLatitude())
                                    , Double.parseDouble(shopsList.get(j).getLongitude()))) {

                        ModelShop temp = new ModelShop();
                        temp = shopsList.get(j - 1);
                        shopsList.set((j - 1), shopsList.get(j));
                        shopsList.set(j, temp);


                    }
                }
            }

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        return shopsList;
    }

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public double calculateDistanceInKilometer(double userLat, double userLng,
                                               double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (double) Math.floor ( (AVERAGE_RADIUS_OF_EARTH_KM * c) * 100 ) / 100;

    }





}
