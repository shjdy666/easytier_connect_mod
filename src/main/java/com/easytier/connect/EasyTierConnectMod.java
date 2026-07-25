package com.easytier.connect;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyTierConnectMod implements ModInitializer {
    public static final String MOD_ID = "easytier_connect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("EasyTier Connect (Terracotta web) initialized!");
    }

    /** 打开陶瓦联机网页 */
    public static void openTerracotta() {
        TerracottaLauncher.getInstance().getOrStart().whenComplete((url, ex) -> {
            if (ex != null) {
                LOGGER.error("Failed to start Terracotta", ex);
            } else {
                try {
                    // 使用 AWT Desktop 打开浏览器
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } else {
                        // 回退：用 Runtime
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec("open " + url);
                        } else {
                            Runtime.getRuntime().exec("xdg-open " + url);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to open browser", e);
                }
            }
        });
    }
}