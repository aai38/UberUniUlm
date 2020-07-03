package de.uni_ulm.uberuniulm.ui.main;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import de.uni_ulm.uberuniulm.ui.fragments.MainPageFragment;
import de.uni_ulm.uberuniulm.ui.fragments.ParkingFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0) {
            return new MainPageFragment();
        } else {
            return new ParkingFragment();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Rides";
        } else {
            return "Parking";
        }

    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}