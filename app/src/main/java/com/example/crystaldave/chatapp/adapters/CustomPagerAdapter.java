package com.example.crystaldave.chatapp.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.crystaldave.chatapp.fragments.ChatsFragment;
import com.example.crystaldave.chatapp.fragments.FriendsFragment;
import com.example.crystaldave.chatapp.fragments.RequestsFragment;

public class CustomPagerAdapter extends FragmentPagerAdapter {

    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new RequestsFragment();
            case 2:
                return new FriendsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "";
            case 1:
                return "";
            case 2:
                return "";
            default:
                return null;
        }
    }


}
