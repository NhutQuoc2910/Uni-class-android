package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.ppquang06.uniclassapp.utils.ThemeHelper;

import org.junit.Test;

public class ThemeHelperTest {

    @Test
    public void testConstants() {
        assertEquals("uniclass_theme_prefs", ThemeHelper.PREFS_NAME);
        assertEquals("dark_mode_enabled", ThemeHelper.KEY_DARK_MODE);
    }

    @Test
    public void testNullContextSafety() {
        assertFalse(ThemeHelper.isDarkMode(null));
        // setDarkMode with null context must not crash
        ThemeHelper.setDarkMode(null, true);
    }
}
