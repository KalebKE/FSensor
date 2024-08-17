package com.tracqi.fsensorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tracqi.fsensorapp.fragment.GaugesFragment

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
 * A class that provides a navigation menu to the features of Acceleration
 * Explorer.
 *
 * @author Kaleb
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)
        // Create new fragment and transaction
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)

        // Replace whatever is in the fragment_container view with this fragment
        transaction.replace(R.id.container, GaugesFragment::class.java, null)

        // Commit the transaction
        transaction.commit()
    }
}
