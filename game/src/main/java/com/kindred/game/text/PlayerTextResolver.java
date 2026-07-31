package com.kindred.game.text;

import java.util.Locale;
import java.util.ResourceBundle;

public class PlayerTextResolver {
    private static final String BUNDLE_NAME = "text.player_text";

    private final ResourceBundle bundle;

    private PlayerTextResolver(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public static PlayerTextResolver forLocale(Locale locale) {
        return new PlayerTextResolver(ResourceBundle.getBundle(BUNDLE_NAME, locale));
    }

    public String resolve(PlayerTextKey key) {
        return bundle.getString(key.key());
    }
}
