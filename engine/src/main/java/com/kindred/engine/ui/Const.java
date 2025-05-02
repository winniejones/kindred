package com.kindred.engine.ui;

import java.awt.*;

/**
 * Interface holding constant values for UI layout, colors, and fonts.
 * Helps centralize UI styling and dimensions.
 */
public interface Const {
    // --- Layout & Sizing (Keeping descriptive names for clarity) ---
    int MARGIN_2 = 2;                 // Margin around elements
    int MARGIN_10 = 10;                 // Margin between elements
    int SIDEBAR_WIDTH = 180;        // Width of the main sidebar
    int CHAT_HEIGHT = 120;          // Height of the chat area
    int STATS_HEIGHT = 50;         // Height of the stats panel within sidebar
    int BAR_HEIGHT = 15;            // Height of the button bar within sidebar
    int SKILLS_HEIGHT = 100;        // Height of the skills panel within sidebar
    int OPTIONS_HEIGHT = 100;       // Height of the options panel within sidebar
    int INV_SLOT_HEIGHT = 80;       // Example height for inventory panels
    int CHAT_INPUT_HEIGHT = 25;     // Height of the chat input field
    int CLOSE_BTN_SIZE = 9;     // Size of the 'X' close buttons
    int MENU_BTN_WIDTH = 40;     // Width of main menu buttons (Skills, Options)
    int MENU_BTN_HEIGHT = 10;    // Height of main menu buttons
    int HEALTH_BAR_HEIGHT = 4;      // Height for in-world health bars
    int STATUS_BAR_HEIGHT = 4;      // Height for in-world health bars
    int HEALTH_BAR_WIDTH = 30;      // Width for in-world health bars

    // --- Colors (Using Prefix_Category_Descriptor format) ---

    // --- Color Palette (Opaque ARGB Hex: 0xFFRRGGBB) ---
    /*
    * | Transparency | Opacity | Alpha (dec) | Alpha (hex) | Example ARGB for black |
    * |--------------|---------|-------------|-------------|------------------------|
    * | 10 %         | 90 %    | 229         | --E5--      | `#E5000000`            |
    * | 20 %         | 80 %    | 204         | --CC--      | `#CC000000`            |
    * | 30 %         | 70 %    | 178         | --B2--      | `#B2000000`            |
    * | 40 %         | 60 %    | 153         | --99--      | `#99000000`            |
    * | 50 %         | 50 %    | 127         | --7F--      | `#7F000000`            |
    * | 60 %         | 40 %    | 102         | --66--      | `#66000000`            |
    * | 70 %         | 30 %    | 76          | --4C--      | `#4C000000`            |
    * | 80 %         | 20 %    | 51          | --33--      | `#33000000`            |
    * | 90 %         | 10 %    | 25          | --19--      | `#19000000`            |
    * */

    Color COLOR_STONE_100_70 = new Color(0xB2f4f4f5, true);
    Color COLOR_STONE_100 = new Color(0xFFf4f4f5);
    Color COLOR_STONE_300 = new Color(0xFFd4d4d8);
    Color COLOR_STONE_500 = new Color(0xFF71717a);
    Color COLOR_STONE_700 = new Color(0xFF3f3f46);
    Color COLOR_STONE_900 = new Color(0xFF18181b);
    Color COLOR_STONE_900_70 = new Color(0xB218181b);
    Color COLOR_STONE_950 = new Color(0xFF09090b);
    Color COLOR_STONE_950_70 = new Color(0xB209090b, true);

    // Red
    Color COLOR_RED_100 = new Color(0xFFfee2e2);
    Color COLOR_RED_300 = new Color(0xFFfca5a5);
    Color COLOR_RED_500 = new Color(0xFFef4444);
    Color COLOR_RED_700 = new Color(0xFFb91c1c);
    Color COLOR_RED_900 = new Color(0xFF7f1d1d);
    Color COLOR_RED_950 = new Color(0xFF460809);

    // Yellow
    Color COLOR_YELLOW_100 = new Color(0xFFfef9c3);
    Color COLOR_YELLOW_300 = new Color(0xFFfde047);
    Color COLOR_YELLOW_500 = new Color(0xFFeab308);
    Color COLOR_YELLOW_700 = new Color(0xFFb45309);
    Color COLOR_YELLOW_900 = new Color(0xFF713f12);
    Color COLOR_YELLOW_950 = new Color(0xFF432004);

    // Green
    Color COLOR_GREEN_100 = new Color(0xFFdcfce7);
    Color COLOR_GREEN_300 = new Color(0xFF86efac);
    Color COLOR_GREEN_500 = new Color(0xFF22c55e);
    Color COLOR_GREEN_700 = new Color(0xFF15803d);
    Color COLOR_GREEN_900 = new Color(0xFF14532d);

    // Blue
    Color COLOR_BLUE_100 = new Color(0xFFdbeafe);
    Color COLOR_BLUE_300 = new Color(0xFF93c5fd);
    Color COLOR_BLUE_500 = new Color(0xFF3b82f6);
    Color COLOR_BLUE_700 = new Color(0xFF1d4ed8);
    Color COLOR_BLUE_900 = new Color(0xFF1e3a8a);
    Color COLOR_BLUE_950 = new Color(0xFF162456);

    // Backgrounds
    Color COLOR_BG_SIDEBAR = COLOR_STONE_700;
    Color COLOR_BG_CHAT = COLOR_STONE_950_70;
    Color COLOR_BG_MINIMAP = Color.BLACK;
    Color COLOR_BG_STATS = COLOR_STONE_700;
    Color COLOR_BG_SKILLS = COLOR_STONE_700;
    Color COLOR_BG_OPTIONS = COLOR_STONE_700;
    Color COLOR_BG_INVENTORY = COLOR_STONE_700;
    Color COLOR_BG_CLOSE_BTN = COLOR_RED_950;

    // Button Backgrounds
    Color COLOR_BG_BTN_BAR = COLOR_STONE_700;
    Color COLOR_BG_BTN_DEFAULT = new Color(0xaaaaaa);
    Color COLOR_BG_BTN_HOVER = new Color(0xcdcdcd);
    Color COLOR_BG_BTN_PRESSED = COLOR_RED_700;

    Color COLOR_BG_INPUT = COLOR_STONE_300;
    // Bar Backgrounds
    Color COLOR_BG_HEALTH_BAR = COLOR_RED_950; // Dark red background
    Color COLOR_BG_STATUS_XP_BAR = COLOR_YELLOW_950; // Dark yellow background
    Color COLOR_BG_MANA_BAR = COLOR_BLUE_950; // Dark yellow background

    // Foregrounds
    Color COLOR_FG_HEALTH_BAR = Color.RED; // Default health bar fill
    Color COLOR_FG_HEALTH_BAR_LOW = Color.RED.darker(); // When health is low
    Color COLOR_FG_STATUS_XP_BAR = COLOR_STONE_100;
    Color COLOR_FG_MANA_BAR = COLOR_BLUE_700;

    // Text
    Color COLOR_TEXT_LIGHT = COLOR_STONE_100;
    Color COLOR_TEXT_DARK = COLOR_STONE_950; // For button labels etc.
    Color COLOR_TEXT_PLAYER_NAME = COLOR_GREEN_700;
    Color COLOR_TEXT_ENEMY_NAME = COLOR_YELLOW_700;
    Color COLOR_TEXT_LEVEL = Color.CYAN;
    Color COLOR_TEXT_XP = Color.YELLOW;
    Color COLOR_TEXT_HEALTH = COLOR_STONE_100; // Label above health bar

    // --- Fonts (Using FONT_Family_Style_Size format where applicable) ---
    Font FONT_SANS_BOLD_7 = new Font("Arial", Font.BOLD, 7);   // Close button, maybe names
    Font FONT_SANS_BOLD_8 = new Font("Arial", Font.BOLD, 8);   // Close button, maybe names
    Font FONT_SANS_BOLD_9 = new Font("Arial", Font.BOLD, 9);   // Close button, maybe names
    Font FONT_SANS_BOLD_10 = new Font("Arial", Font.BOLD, 10);   // Close button, maybe names
    Font FONT_SANS_BOLD_12 = new Font("Arial", Font.BOLD, 12);
    Font FONT_SANS_BOLD_14 = new Font("Arial", Font.BOLD, 14);   // Larger headings? Health Label?
    Font FONT_SANS_PLAIN_8 = new Font("Arial", Font.PLAIN, 8);  // Even Smaller text
    Font FONT_SANS_PLAIN_9 = new Font("Arial", Font.PLAIN, 9);  // Even Smaller text
    Font FONT_SANS_PLAIN_10 = new Font("Arial", Font.PLAIN, 10);  // Smaller text
    Font FONT_SANS_PLAIN_11 = new Font("Arial", Font.PLAIN, 11);  // Chat area text?
    Font FONT_SANS_PLAIN_12 = new Font("Arial", Font.PLAIN, 12); // Default UI font

    // Specific use-case fonts (can reuse above if styles match)
    Font FONT_BTN = FONT_SANS_PLAIN_9;
    Font FONT_CLOSE_BTN = FONT_SANS_BOLD_9;
    Font FONT_NAMEPLATE = FONT_SANS_BOLD_9;
    Font FONT_CHAT_INPUT = FONT_SANS_PLAIN_10;
    Font FONT_CHAT_AREA = FONT_SANS_PLAIN_10;
    Font FONT_STATS_LABEL = FONT_SANS_PLAIN_8;
    Font FONT_STATS_VALUE = FONT_SANS_PLAIN_9; // Example

}