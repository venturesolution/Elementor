/*
 * Copyright 2022 Element Powered by @fernandoangeli and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fasofts.element.ui
import java.io.File
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import fasofts.element.service.VpnController
import android.widget.Toast
import android.util.Log
import fasofts.element.AntiLoader
//import fasofts.element.ui.AppUpdateChecker

class PrepareVpnActivity : ComponentActivity() {
companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    private external fun nativeAdd(a: Int, b: Int): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val r = nativeAdd(2, 3)
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    VpnController.start(this)
                }
                finish()
            }
            .launch(VpnService.prepare(this))
    }
}
