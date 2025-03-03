package io.github.fplus.core.hook

import android.app.Dialog
import android.widget.TextView
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.ellipsis
import com.freegang.ktutils.view.onEachWhereChild
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.OnAfter
import io.github.xpler.core.hookBlockRunning

class HDialog(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Dialog>(lpparam) {
    companion object {
        const val TAG = "HDialog"
    }

    private val config get() = ConfigV1.get()

    @OnAfter("show")
    fun showAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isDialogFilter) {
                return
            }

            val dialog = thisObject as Dialog
            val mDecorView = dialog.window?.decorView ?: return

            val keywords = config.dialogFilterKeywords
                .removePrefix(",").removePrefix("，")
                .removeSuffix(",").removeSuffix("，")
                .replace("\\s".toRegex(), "")
                .replace("[,，]".toRegex(), "|")
                .toRegex()

            if (keywords.pattern.isEmpty()) {
                return
            }

            mDecorView.onEachWhereChild {
                if ("${this.contentDescription}".contains(keywords)) {
                    dialog.dismiss()
                    if (config.dialogDismissTips) {
                        // KToastUtils.show(dialog.context, "弹窗关闭成功!")
                        KToastUtils.show(dialog.context, "“${this.contentDescription.ellipsis(5)}”关闭成功!")
                    }
                    return@onEachWhereChild true
                } else if (this is TextView) {
                    if ("${this.text}".contains(keywords)) {
                        dialog.dismiss()
                        if (config.dialogDismissTips) {
                            // KToastUtils.show(dialog.context, "弹窗关闭成功!")
                            KToastUtils.show(dialog.context, "“${this.text.ellipsis(5)}”关闭成功!")
                        }
                        return@onEachWhereChild true
                    }
                }

                false
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}