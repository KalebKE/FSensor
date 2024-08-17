package com.tracqi.fsensorapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tracqi.fsensor.sensor.FSensorEvent;
import com.tracqi.fsensor.sensor.FSensorEventListener;
import com.tracqi.fsensorapp.R;
import com.tracqi.fsensorapp.gauge.GaugeAcceleration;
import com.tracqi.fsensorapp.viewmodel.FSensorViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/*
 * Copyright 2024, Tracqi Technology, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

public class AccelerationGaugeFragment extends Fragment {

    private GaugeAcceleration gaugeAcceleration;
    private Handler handler;
    private Runnable runnable;

    private float[] acceleration = new float[3];

    private  FSensorViewModel viewModel;

    private final FSensorEventListener sensorEventListener = fSensorEvent -> acceleration = fSensorEvent.values();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        viewModel = viewModelProvider.get(FSensorViewModel.class);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateAccelerationGauge();
                handler.postDelayed(this, 20);
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acceleration_gauge, container, false);

        gaugeAcceleration = view.findViewById(R.id.gauge_acceleration);

        return view;
    }

    @Override
    public void onPause() {
        viewModel.unregisterLinearAccelerationSensorListener(sensorEventListener);
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.registerLinearAccelerationSensorListener(sensorEventListener);
        handler.post(runnable);
    }

    private void updateAccelerationGauge() {
        gaugeAcceleration.updatePoint(acceleration[0], acceleration[1]);
    }
}
