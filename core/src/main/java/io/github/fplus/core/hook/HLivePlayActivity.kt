package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.xpler.core.FutureHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity

class HLivePlayActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {

    companion object {
        const val TAG = "HLivePlayActivity"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.live.LivePlayActivity")
    }

    @FutureHook
    @OnBefore("onWindowFocusChanged")
    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                ImmersiveHelper.immersive(
                    thisActivity,
                    hideStatusBar = true,
                    hideNavigationBars = true,
                )
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}