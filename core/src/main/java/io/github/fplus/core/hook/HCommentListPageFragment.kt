package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.comment.constants.CommentColorMode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods
import java.lang.reflect.Method

class HCommentListPageFragment(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HCommentListPageFragment"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.commentListPageFragmentClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isCommentColorMode) return

            if ((method as Method).returnType != CommentColorMode::class.java) return
            result = when (config.commentColorMode) {
                0 -> {
                    CommentColorMode.MODE_LIGHT
                }

                1 -> {
                    CommentColorMode.MODE_DARK
                }

                else -> {
                    CommentColorMode.MODE_LIGHT_OR_DARK
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}