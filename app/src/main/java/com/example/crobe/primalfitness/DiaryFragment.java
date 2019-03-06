package com.example.crobe.primalfitness;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiaryFragment extends Fragment {


    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;

    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;

    public DiaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        return view;
    }


}
