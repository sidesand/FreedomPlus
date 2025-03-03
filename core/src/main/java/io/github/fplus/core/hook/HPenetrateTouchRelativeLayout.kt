package io.github.fplus.core.hook

import android.view.View
import androidx.core.view.updatePadding
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.FutureHook
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods
import io.github.xpler.core.thisViewGroup

class HPenetrateTouchRelativeLayout(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<PenetrateTouchRelativeLayout>(lpparam), CallMethods {
    companion object {
        const val TAG = "HPenetrateTouchRelativeLayout"
    }

    private val config get() = ConfigV1.get()

    @FutureHook
    @OnBefore("setVisibility")
    fun setVisibilityBefore(params: XC_MethodHook.MethodHookParam, visibility: Int) {
        hookBlockRunning(params) {
            if (!config.isNeatMode) {
                return
            }

            if (!config.neatModeState) {
                return
            }

            if (visibility == View.GONE || visibility == View.INVISIBLE) {
                return
            }

            if (HPlayerController.isPlaying) {
                args[0] = View.GONE
                HMainActivity.toggleView(false)
            } else {
                args[0] = View.VISIBLE
                HMainActivity.toggleView(true)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisViewGroup.apply {
                    val bottomPadding = context.dip2px(58f) // BottomTabBarHeight
                    updatePadding(bottom = bottomPadding)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

}