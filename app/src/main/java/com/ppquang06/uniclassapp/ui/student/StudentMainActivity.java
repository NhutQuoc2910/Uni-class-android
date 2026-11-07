package com.ppquang06.uniclassapp.ui.student;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.ActivityStudentMainBinding;
import com.ppquang06.uniclassapp.utils.FcmTokenHelper;
import com.ppquang06.uniclassapp.utils.RoleRouter;
import com.ppquang06.uniclassapp.utils.SessionManager;
import com.ppquang06.uniclassapp.utils.ThemeHelper;

public class StudentMainActivity extends AppCompatActivity {

    private ActivityStudentMainBinding binding;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                FcmTokenHelper.syncFcmToken(this);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        UserResponse user = SessionManager.getInstance(this).getUser();
        if (user != null && RoleRouter.isLecturerRole(user.getRole())) {
            RoleRouter.navigateToHomeByRole(this);
            return;
        }

        binding = ActivityStudentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavStudent, (v, insets) -> {
            if (v != null && insets != null) {
                Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, navBarInsets.bottom);
            }
            return insets;
        });

        setupNavigation();
        requestNotificationPermissionAndSyncToken();
    }

    private void requestNotificationPermissionAndSyncToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                FcmTokenHelper.syncFcmToken(this);
            }
        } else {
            FcmTokenHelper.syncFcmToken(this);
        }
    }

    public void switchToTab(int menuItemId) {
        if (binding != null && binding.bottomNavStudent != null) {
            binding.bottomNavStudent.setSelectedItemId(menuItemId);
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_student);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavStudent, navController);
            binding.bottomNavStudent.setOnItemReselectedListener(item -> {
                navController.popBackStack(item.getItemId(), false);
            });
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean showBottomBar = (id == R.id.nav_student_home
                        || id == R.id.nav_student_classes
                        || id == R.id.nav_student_scan_qr
                        || id == R.id.nav_student_notifications
                        || id == R.id.nav_student_profile);
                binding.bottomNavStudent.setVisibility(showBottomBar ? View.VISIBLE : View.GONE);
            });
        }
    }
}
