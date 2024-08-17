package com.tracqi.fsensorapp.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tracqi.fsensor.sensor.FSensorEvent
import com.tracqi.fsensor.sensor.FSensorEventListener
import com.tracqi.fsensorapp.R
import com.tracqi.fsensorapp.gauge.GaugeRotation
import com.tracqi.fsensorapp.viewmodel.FSensorViewModel

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
class RotationGaugeFragment : Fragment() {
    private var gaugeRotation: GaugeRotation? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private var rotation = FloatArray(3)

    private var viewModel: FSensorViewModel? = null

    private val sensorEventListener = FSensorEventListener { fSensorEvent: FSensorEvent -> rotation = fSensorEvent.values }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelProvider = ViewModelProvider(requireActivity())
        viewModel = viewModelProvider.get(FSensorViewModel::class.java)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                updateRotationGauge()
                handler!!.postDelayed(this, 20)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rotation_gauge, container, false)

        gaugeRotation = view.findViewById(R.id.gauge_rotation)

        return view
    }

    override fun onPause() {
        viewModel!!.unregisterRotationSensorListener(sensorEventListener)
        handler!!.removeCallbacks(runnable!!)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.registerRotationSensorListener(sensorEventListener)
        handler!!.post(runnable!!)
    }

    private fun updateRotationGauge() {
        gaugeRotation!!.updateRotation(rotation)
    }
}
