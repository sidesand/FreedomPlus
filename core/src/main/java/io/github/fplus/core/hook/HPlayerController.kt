package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.reflect.methods
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning

class HPlayerController(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HPlayerController"

        var playingAid = ""

        @get:Synchronized
        @set:Synchronized
        var isPlaying = true
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.controller.PlayerController")
    }

    @OnBefore("onPlaying")
    fun onPlayingAfter(params: XC_MethodHook.MethodHookParam, aid: String) {
        hookBlockRunning(params) {
            playingAid = aid
            isPlaying = true
            callOpenCleanMode(params, true)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onResumePlay")
    fun onResumePlayBefore(params: XC_MethodHook.MethodHookParam, aid: String) {
        hookBlockRunning(params) {
            playingAid = aid
            isPlaying = true
            callOpenCleanMode(params, true)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onPausePlay")
    fun onPausePlayAfter(params: XC_MethodHook.MethodHookParam, aid: String) {
        hookBlockRunning(params) {
            if (playingAid == aid) {
                isPlaying = false
            }
            callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // @OnBefore("onPlayCompleted")
    fun onPlayCompletedAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // @OnBefore("onPlayCompletedFirstTime")
    fun onPlayCompletedFirstTimeAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // @OnBefore("onPlayProgressChange")
    fun onPlayProgressChangeAfter(params: XC_MethodHook.MethodHookParam, float: Float) {
        hookBlockRunning(params) {
            isPlaying = true
            // callOpenCleanMode(params, true)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        if (!config.isNeatMode) {
            return
        }

        if (!config.neatModeState) {
            return
        }

        val methodFirst = params.thisObject.methods(returnType = VideoViewHolder::class.java)
            .firstOrNull { it.parameterTypes.isEmpty() }
        val videoViewHolder = methodFirst?.invoke(params.thisObject)
        videoViewHolder?.methodInvokeFirst("openCleanMode", args = arrayOf(bool))

        //
        HMainActivity.toggleView(!bool)
    }
}