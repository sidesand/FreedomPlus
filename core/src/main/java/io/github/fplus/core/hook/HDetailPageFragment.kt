package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.updatePadding
import com.freegang.ktutils.collection.ifNotEmpty
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.findViewsByExact
import com.freegang.ktutils.view.postRunning
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.databinding.HookAppbarLayoutBinding
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.hook.logic.SaveCommentLogic
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.NoneHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.hookBlockRunning

class HDetailPageFragment(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HDetailPageFragment"

        @get:Synchronized
        @set:Synchronized
        var isComment = false
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> = DexkitBuilder.detailPageFragmentClazz ?: NoneHook::class.java

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(param: XC_MethodHook.MethodHookParam, view: View, bundle: Bundle?) {
        hookBlockRunning(param) {
            if (!config.isEmoji) return

            //
            HDetailPageFragment.isComment = false
            view.postRunning {
                val aweme = thisObject.methodInvokeFirst(returnType = Aweme::class.java) as? Aweme ?: return@postRunning

                // awemeType 【134:评论区图片, 133|136:评论区视频, 0:主页视频详情, 68:主页图文详情, 13:私信视频/图文, 6000:私信图片】 by 25.1.0 至今
                if (aweme.awemeType != 134 && aweme.awemeType != 133 && aweme.awemeType != 136) return@postRunning

                val imageViews = KViewUtils.findViewsByDesc(view, ImageView::class.java, "返回")
                if (imageViews.isEmpty()) return@postRunning
                val backBtn = imageViews.first()

                // 清空旧视图
                val viewGroup = backBtn.parent as ViewGroup
                viewGroup.removeAllViews()

                // 重新构建视图
                val appbar = KtXposedHelpers.inflateView<RelativeLayout>(viewGroup.context, R.layout.hook_appbar_layout)
                val binding = HookAppbarLayoutBinding.bind(appbar)
                binding.backBtn.setOnClickListener {
                    backBtn.performClick()
                }
                binding.saveBtn.setOnClickListener {
                    val awemeAgain = thisObject.methodInvokeFirst(returnType = Aweme::class.java) as? Aweme // 重新获取
                    SaveCommentLogic(this@HDetailPageFragment, it.context, awemeAgain)
                }
                viewGroup.addView(appbar)

                //
                HDetailPageFragment.isComment = true

                // 我也发一张
                view.findViewsByExact(TextView::class.java) {
                    "$text".contains("我也发") || "$contentDescription".contains("我也发")
                }.ifNotEmpty {
                    binding.rightSpace.updatePadding(right = KDisplayUtils.dip2px(view.context, 128f))
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onStop")
    fun onStopAfter(param: XC_MethodHook.MethodHookParam) {
        HDetailPageFragment.isComment = false
    }
}