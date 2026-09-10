package im.umo.baro_input

import android.widget.Button
import android.view.View
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SetupFlowTest {
    private fun children(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { children(view.getChildAt(it)) } else emptyList()

    @Test fun `practice button opens the local practice activity`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val scroll = children(activity.window.decorView).filterIsInstance<android.widget.ScrollView>().single()
        assertTrue("Content must retain a horizontal gutter before insets dispatch", scroll.paddingLeft > 0)
        val button = children(activity.window.decorView).filterIsInstance<Button>().single { it.text == activity.getString(R.string.practice) }
        button.performClick()
        assertEquals(PracticeActivity::class.java.name, shadowOf(activity).nextStartedActivity.component?.className)
        controller.pause().stop().destroy()
    }

    @Test fun `practice fields deliberately opt out of saving entered content`() {
        val controller = Robolectric.buildActivity(PracticeActivity::class.java).setup()
        val fields = children(controller.get().window.decorView).filterIsInstance<android.widget.EditText>()
        assertEquals(2, fields.size)
        assertTrue(fields.all { !it.isSaveEnabled })
        controller.pause().stop().destroy()
    }
}
