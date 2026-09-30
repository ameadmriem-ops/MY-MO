package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.config.AdConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager private constructor(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    init {
        try {
            MobileAds.initialize(context) {}
            loadRewardedAd()
        } catch (e: Exception) {
            Log.e("AdManager", "Error initializing MobileAds: ${e.message}")
        }
    }

    fun loadRewardedAd(onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        if (isLoading || rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AdConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d("AdManager", "Rewarded ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w("AdManager", "Rewarded ad failed to load: ${loadAdError.message}")
                    onFailed?.invoke(loadAdError.message)
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onErrorOrFallback: (String) -> Unit
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            var userEarnedReward = false

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d("AdManager", "Rewarded ad displayed fullscreen.")
                }

                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd() // Preload next ad
                    if (userEarnedReward) {
                        onRewardEarned()
                    } else {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    onErrorOrFallback(adError.message)
                }
            }

            currentAd.show(activity) { rewardItem ->
                userEarnedReward = true
                Log.d("AdManager", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            }
        } else {
            // Ad not loaded yet - try to load in background and allow smooth fallback
            loadRewardedAd()
            onErrorOrFallback("الإعلان غير جاهز حالياً، جاري المتابعة مباشرة...")
        }
    }

    companion object {
        @Volatile
        private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
