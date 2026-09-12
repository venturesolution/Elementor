/*
 * Copyright 2023 Element Powered by @fernandoangeli and its authors
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
package fasofts.element.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import fasofts.element.R
import fasofts.element.databinding.FragmentConfigureBinding
import fasofts.element.ui.activity.AppListActivity
import fasofts.element.ui.activity.DnsDetailActivity
import fasofts.element.ui.activity.FirewallActivity
import fasofts.element.ui.activity.MiscSettingsActivity
import fasofts.element.ui.activity.NetworkLogsActivity
import fasofts.element.ui.activity.ProxySettingsActivity
import fasofts.element.ui.activity.TunnelSettingsActivity

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener

import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


class ConfigureFragment : Fragment(R.layout.fragment_configure) {

    private val b by viewBinding(FragmentConfigureBinding::bind)

    enum class ScreenType {
        APPS,
        DNS,
        FIREWALL,
        PROXY,
        VPN,
        OTHERS,
        LOGS
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        
        val adRequest = AdRequest.Builder().build()
         b.adView.loadAd(adRequest)
    }

    private fun initView() {
        b.fsNetworkTv.text = getString(R.string.lbl_network).replaceFirstChar(Char::titlecase)
        b.fsLogsTv.text = getString(R.string.lbl_logs).replaceFirstChar(Char::titlecase)

        b.fsAppsCard.setOnClickListener {
            // open apps configuration
            startActivity(ScreenType.APPS)
        }

        b.fsDnsCard.setOnClickListener {
            // open dns configuration
            startActivity(ScreenType.DNS)
        }

        b.fsFirewallCard.setOnClickListener {
            // open firewall configuration
            startActivity(ScreenType.FIREWALL)
        }

        b.fsProxyCard.setOnClickListener {
            // open proxy configuration
            startActivity(ScreenType.PROXY)
        }

        b.fsNetworkCard.setOnClickListener {
            // open vpn configuration
            startActivity(ScreenType.VPN)
        }

        b.fsOthersCard.setOnClickListener {
            // open others configuration
            startActivity(ScreenType.OTHERS)
        }

        b.fsLogsCard.setOnClickListener {
            // open logs configuration
            startActivity(ScreenType.LOGS)
        }
    }

    private fun startActivity(type: ScreenType) {
        val intent =
            when (type) {
                ScreenType.APPS -> Intent(requireContext(), AppListActivity::class.java)
                ScreenType.DNS -> Intent(requireContext(), DnsDetailActivity::class.java)
                ScreenType.FIREWALL -> Intent(requireContext(), FirewallActivity::class.java)
                ScreenType.PROXY -> Intent(requireContext(), ProxySettingsActivity::class.java)
                ScreenType.VPN -> Intent(requireContext(), TunnelSettingsActivity::class.java)
                ScreenType.OTHERS -> Intent(requireContext(), MiscSettingsActivity::class.java)
                ScreenType.LOGS -> Intent(requireContext(), NetworkLogsActivity::class.java)
            }
        intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(intent)
    }
}
