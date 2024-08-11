package com.tracqi.fsensorapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tracqi.fsensorapp.R;
import com.tracqi.fsensorapp.gauge.GaugeRotation;
import com.tracqi.fsensorapp.preference.Preferences;
import com.tracqi.fsensorapp.viewmodel.FSensorViewModel;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;



/*
 * AccelerationExplorer
 * Copyright 2018 Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by kaleb on 7/8/17.
 */

public class RotationGaugeFragment extends Fragment {

    private GaugeRotation gaugeRotation;
    private Handler handler;
    private Runnable runnable;

    private float[] rotation = new float[3];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        FSensorViewModel model = viewModelProvider.get(FSensorViewModel.class);

        if(Preferences.getPrefFSensorLpfLinearAccelerationEnabled(getContext())){
            model.getLowPassOrientationSensorLiveData().observe(this, floats -> rotation = floats);
        } else if(Preferences.getPrefFSensorComplimentaryLinearAccelerationEnabled(getContext())) {
            model.getComplimentaryOrientationSensorLiveData().observe(this, floats -> rotation = floats);
        } else if(Preferences.getPrefFSensorKalmanLinearAccelerationEnabled(getContext())) {
            model.getKalmanOrientationSensorLiveData().observe(this, floats -> rotation = floats);
        }

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable()
        {
            @Override
            public void run() {
                updateRotationGauge();
                handler.postDelayed(this, 20);
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rotation_gauge, container, false);

        gaugeRotation = view.findViewById(R.id.gauge_rotation);

        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("RotationGaugeFragment", "onResume");
        handler.post(runnable);
    }

    private void updateRotationGauge() {
        gaugeRotation.updateRotation(rotation);
    }
}
