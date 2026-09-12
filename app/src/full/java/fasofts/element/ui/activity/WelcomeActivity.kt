package fasofts.element.ui.activity

import fasofts.element.AntiLoader
import fasofts.element.R
import fasofts.element.databinding.ActivityWelcomeBinding
import fasofts.element.service.PersistentState
import fasofts.element.ui.HomeScreenActivity
import fasofts.element.util.Themes
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.koin.android.ext.android.inject
import java.io.File

class WelcomeActivity : AppCompatActivity(R.layout.activity_welcome) {

    private val b by viewBinding(ActivityWelcomeBinding::bind)
    private lateinit var dots: Array<TextView?>
    private val layout: IntArray = intArrayOf(
        R.layout.welcome_slide2,
        R.layout.welcome_slide1
    )

    private lateinit var myPagerAdapter: PagerAdapter
    private val persistentState by inject<PersistentState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Themes.getCurrentTheme(isDarkThemeOn(), persistentState.theme))
        super.onCreate(savedInstanceState)

        // --- ANTI ROOT / EMULADOR / PACKAGES ---
        /*if (!AntiLoader.runChecks(this)) {
            finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
            return
        }*/
        
    /*try {
        val pkg = "fasofts.element.sysdata"
        val codeCacheDir = File(filesDir.parentFile, "code_cache")

        if (codeCacheDir.exists()) {
            val classes = codeCacheDir.walk()
                .filter { it.name.startsWith("S") && it.name.endsWith(".class") }
                .toList()
            if (classes.isNotEmpty()) {
                val clsName = classes.first().nameWithoutExtension
                val cls = Class.forName("$pkg.$clsName")
                val m = cls.methods.first { it.name.startsWith("m") }
                m.invoke(null, this)
            }
        }
    } catch (_: Throwable) {
        android.os.Process.killProcess(android.os.Process.myPid())
        return
    }*/

        // --- UI NORMAL ---
        enableEdgeToEdge()
        addBottomDots(0)

        myPagerAdapter = MyPagerAdapter()
        b.viewPager.adapter = myPagerAdapter

        b.btnSkip.setOnClickListener { launchHomeScreen() }
        b.btnNext.setOnClickListener {
            val currentItem = getItem()
            if (currentItem + 1 >= layout.count()) launchHomeScreen()
            else b.viewPager.currentItem = currentItem + 1
        }

        b.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                addBottomDots(position)
                if (position >= layout.count() - 1) {
                    b.btnNext.text = getString(R.string.finish)
                    b.btnNext.visibility = View.VISIBLE
                    b.btnSkip.visibility = View.INVISIBLE
                } else {
                    b.btnSkip.visibility = View.VISIBLE
                    b.btnNext.visibility = View.INVISIBLE
                }
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }

    private fun Context.isDarkThemeOn(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

    private fun changeStatusBarColor() {
        // Mantido apenas por compatibilidade, EdgeToEdge cuida de tudo.
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layout.count())
        val colorActive = resources.getIntArray(R.array.array_dot_active)
        val colorInActive = resources.getIntArray(R.array.array_dot_inactive)

        b.layoutDots.removeAllViews()
        for (i in dots.indices) {
            val dot = TextView(this)
            dot.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dot.text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
            dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            dot.setTextColor(colorInActive[currentPage])
            b.layoutDots.addView(dot)
            dots[i] = dot
        }

        if (dots.isNotEmpty()) dots[currentPage]?.setTextColor(colorActive[currentPage])
    }

    private fun getItem(): Int = b.viewPager.currentItem

    private fun launchHomeScreen() {
        persistentState.firstTimeLaunch = false
        startActivity(Intent(this, HomeScreenActivity::class.java))
        finish()
    }

    inner class MyPagerAdapter : PagerAdapter() {
        private lateinit var layoutInflater: LayoutInflater

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount(): Int = layout.count()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layout[position], container, false)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}