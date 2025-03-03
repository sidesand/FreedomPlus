package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.NoneHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.hookBlockRunning

class HConversationFragment(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HConversationFragment"
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.conversationFragmentClazz ?: NoneHook::class.java
    }

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(params: XC_MethodHook.MethodHookParam, view: View, bundle: Bundle?) {
        hookBlockRunning(params) {
            val views = thisObject.fieldGets(type = View::class.java)
                .asOrNull<List<View?>>() ?: emptyList()

            if (view.context.isDarkMode) {
                views.firstOrNull { it?.javaClass?.name?.contains("ConstraintLayout") == true }
                    ?.background = KtXposedHelpers.getDrawable(R.drawable.aweme_bottom_panel_night_background)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}