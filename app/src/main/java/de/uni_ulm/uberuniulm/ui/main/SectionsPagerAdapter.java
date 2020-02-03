package de.uni_ulm.uberuniulm.ui.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.uni_ulm.uberuniulm.MainPageFragment;
import de.uni_ulm.uberuniulm.ParkingFragment;
import de.uni_ulm.uberuniulm.R;

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