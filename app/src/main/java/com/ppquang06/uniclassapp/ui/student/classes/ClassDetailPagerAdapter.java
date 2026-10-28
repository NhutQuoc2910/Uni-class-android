package com.ppquang06.uniclassapp.ui.student.classes;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ClassDetailPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_COUNT = 4;
    public static final int TAB_ANNOUNCEMENTS = 0;
    public static final int TAB_MATERIALS = 1;
    public static final int TAB_ASSIGNMENTS = 2;
    public static final int TAB_ATTENDANCE = 3;

    public ClassDetailPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_ANNOUNCEMENTS:
                return new ClassAnnouncementsTabFragment();
            case TAB_MATERIALS:
                return new ClassMaterialsTabFragment();
            case TAB_ASSIGNMENTS:
                return new ClassAssignmentsTabFragment();
            case TAB_ATTENDANCE:
                return new ClassAttendanceTabFragment();
            default:
                throw new IllegalArgumentException("Invalid tab position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
