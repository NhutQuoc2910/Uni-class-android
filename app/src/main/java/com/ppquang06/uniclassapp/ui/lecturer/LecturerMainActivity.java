package com.ppquang06.uniclassapp.ui.lecturer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.ActivityLecturerMainBinding;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class LecturerMainActivity extends AppCompatActivity {

    private ActivityLecturerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityLecturerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_lecturer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavLecturer, navController);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.nav_lecturer_home
                        || id == R.id.nav_lecturer_classes
                        || id == R.id.nav_lecturer_notifications
                        || id == R.id.nav_lecturer_profile) {
                    binding.bottomNavLecturer.setVisibility(View.VISIBLE);
                } else {
                    binding.bottomNavLecturer.setVisibility(View.GONE);
                }
            });
        }
    }
}
