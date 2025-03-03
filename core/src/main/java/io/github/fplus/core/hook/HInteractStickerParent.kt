package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallConstructors
import io.github.xpler.core.thisViewGroup

class HInteractStickerParent(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<InteractStickerParent>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HInteractStickerParent"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // 移除悬浮贴纸
            if (config.isRemoveSticker) {
                thisViewGroup.postRunning {
                    removeInParent()
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}