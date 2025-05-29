package com.example.waterconservationappblank.AddFragmentPackage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// ViewPagerAdapter is used with ViewPager 2 to swipe between two fragments
public class ViewPagerAdapter extends FragmentStateAdapter {

    // constructor: receives the parent fragment that holds the ViewPager
    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    // this method creates and returns the fragment for the given position
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0: return new AddDailyFragment(); // first tab, daily water log fragment
            case 1: return new AddMonthlyFragment(); // second tab, monthly water log fragment
            default: return new AddDailyFragment(); // default fallback, daily fragment
        }
    }

    // returns the number of tabs (2 tabs)
    @Override
    public int getItemCount() {
        return 2;
    }
}
