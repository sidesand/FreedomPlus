package com.freegang.douyin

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.config.Version
import com.freegang.douyin.activity.FreedomSettingActivity
import com.freegang.douyin.logic.ClipboardLogic
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.color.KColorUtils
import com.freegang.ktutils.reflect.methodInvokes
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.findViewsByDesc
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.call
import com.freegang.xpler.core.findMethodsByReturnType
import com.freegang.xpler.core.thisActivity
import com.freegang.xpler.core.thisContext
import com.freegang.xpler.databinding.SideFreedomSettingBinding
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import com.ss.android.ugc.aweme.sidebar.SideBarNestedScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainActivity>(lpparam) {
    private val config get() = ConfigV1.get()
    private val clipboardLogic = ClipboardLogic(this)

    @OnAfter("onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlock(it) {
            showToast(thisContext, "Freedom+ Attach!")
        }
    }

    @OnAfter("onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            changeViewAlpha(thisActivity.contentView)
            setFreedomSetting(thisActivity)
            //checkVersionDialog(thisActivity)
            addClipboardListener(thisActivity)
        }
    }

    @OnBefore("onPause")
    fun onPause(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            saveConfig(thisContext)
            clipboardLogic.removeClipboardListener(thisContext)
        }
    }

    @OnAfter("onDestroy")
    fun onDestroy(it: XC_MethodHook.MethodHookParam) {

    }

    private fun findVideoAweme(activity: Activity): Aweme? {
        var aweme: Any? = null
        val methods = activity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            aweme = methods.first().call(activity)
        }

        if (aweme == null) {
            val curFragment = activity.methodInvokes("getCurFragment").firstOrNull()
            val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
            if (curFragmentMethods.isNotEmpty()) {
                aweme = curFragmentMethods.first().invoke(curFragment!!)
            }
        }
        return aweme as Aweme?
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) {
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HMainActivity, activity, aweme)
        }
    }

    //Freedom设置
    private fun setFreedomSetting(activity: Activity) {
        launch {
            delay(500L)
            val clazz = findClass("com.ss.android.ugc.aweme.homepage.ui.TopLeftFrameLayout") as Class<ViewGroup>
            val view = KViewUtils.findViews(activity.contentView, clazz).firstOrNull() ?: return@launch
            val firstOrNull = view.children.firstOrNull()
                ?: view.findViewsByDesc(View::class.java, "侧边栏").firstOrNull()
                ?: return@launch
            val onClickListener = KViewUtils.getOnClickListener(firstOrNull)
            firstOrNull.setOnClickListener {
                onClickListener?.onClick(it)
                launch {
                    delay(200)
                    val contentView = activity.contentView
                    val v = contentView.findViewsByType(SideBarNestedScrollView::class.java).firstOrNull() ?: return@launch

                    val sideRootView = v.children.first() as ViewGroup
                    if (sideRootView.children.last().contentDescription == "扩展功能") return@launch

                    val text = sideRootView.findViewsByType(TextView::class.java).firstOrNull() ?: return@launch
                    val isDark = KColorUtils.isDarkColor(text.currentTextColor)

                    val setting = KtXposedHelpers.inflateView<ViewGroup>(v.context, R.layout.side_freedom_setting)
                    setting.contentDescription = "扩展功能"
                    val binding = SideFreedomSettingBinding.bind(setting)

                    val backgroundRes: Int
                    val iconColorRes: Int
                    val dividerColorRes: Int
                    val textColorRes: Int
                    if (!isDark) {
                        backgroundRes = R.drawable.dialog_background_night
                        iconColorRes = R.drawable.ic_freedom_night
                        dividerColorRes = Color.parseColor("#14FFFFFF")
                        textColorRes = Color.parseColor("#E6FFFFFF")
                    } else {
                        backgroundRes = R.drawable.dialog_background
                        iconColorRes = R.drawable.ic_freedom
                        dividerColorRes = Color.parseColor("#1F161823")
                        textColorRes = Color.parseColor("#FF161823")
                    }

                    binding.freedomSettingContainer.background = KtXposedHelpers.getDrawable(backgroundRes)
                    binding.freedomSettingText.setTextColor(textColorRes)
                    binding.freedomSettingDivider.setBackgroundColor(dividerColorRes)
                    binding.freedomSettingIcon.background = KtXposedHelpers.getDrawable(iconColorRes)
                    binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                    binding.freedomSettingTitle.setTextColor(textColorRes)
                    binding.freedomSetting.setOnClickListener { view ->
                        val intent = Intent(view.context, FreedomSettingActivity::class.java)
                        intent.putExtra("isDark", view.context.isDarkMode)
                        val options = ActivityOptions.makeCustomAnimation(
                            activity,
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                        activity.startActivity(intent, options.toBundle())
                    }
                    sideRootView.addView(binding.root)
                }
            }
        }
    }

    //透明度
    private fun changeViewAlpha(viewGroup: ViewGroup) {
        if (!config.isTranslucent) return
        launch {
            delay(200L)
            viewGroup.traverse {
                //底部
                if (it::class.java.name.contains("MainBottomTabContainer")) {
                    it.alpha = 0.5f
                }
            }
        }
    }

    //检查模块版本
    @Synchronized
    private fun checkVersionDialog(activity: Activity) {
        launch {
            delay(2000L)
            val version = withContext(Dispatchers.IO) { Version.getRemoteReleasesLatest() } ?: return@launch
            if (version.name.compareTo("v${config.versionConfig.versionName}") >= 1) {
                showMessageDialog(
                    context = activity,
                    title = "发现新版本 ${version.name}!",
                    content = version.body,
                    cancel = "取消",
                    confirm = "更新",
                    needMultiple = false,
                    onConfirm = {
                        activity.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(version.browserDownloadUrl),
                            )
                        )
                    }
                )
            }
        }
    }

    //保存配置信息
    private fun saveConfig(context: Context) {
        val dyVersionName = context.appVersionName
        val dyVersionCode = context.appVersionCode
        config.isSupportHint = false
        config.versionConfig = config.versionConfig.copy(
            dyVersionName = dyVersionName,
            dyVersionCode = dyVersionCode
        )
    }
}