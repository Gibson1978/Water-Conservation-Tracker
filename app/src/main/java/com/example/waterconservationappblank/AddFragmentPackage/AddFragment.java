package com.example.waterconservationappblank.AddFragmentPackage;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.waterconservationappblank.R;
import com.google.android.material.tabs.TabLayout;


public class AddFragment extends Fragment {


    // UI components
    TabLayout tabLayout; // tab layout for switching between fragments
    ViewPager2 viewPager2; // ViewPager2 to swipe between different fragment page
    ViewPagerAdapter viewPagerAdapter; // custom adapter to manage which fragment to show for each tab

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the layout for this fragment
        View addView = inflater.inflate(R.layout.fragment_add, container, false);

        // link XML element to java variables
        tabLayout = addView.findViewById(R.id.tab_layout); // top tab bar
        viewPager2 = addView.findViewById(R.id.viewPager); // page swiper

        // initialize adapter and set it to the view pager
        viewPagerAdapter = new ViewPagerAdapter(this); //pass current fragment to adpater
        viewPager2.setAdapter(viewPagerAdapter); // set adapter to ViewPager2

        // disable saving view pager state to avoid potential bugs on refresh
        viewPager2.setSaveEnabled(false);

        // handle tab selection events
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                // when tab is selected, switch the ViewPager2 page to the corresponding position
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //empty because no action needed when tab is reselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //empty because no action needed when tab is reselected
            }
        });

        // sync tab layout with ViewPager2 swipe events
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //when the user swipes pages, update the selected tab
                tabLayout.getTabAt(position).select();
            }
        });
        return addView;
    }
}