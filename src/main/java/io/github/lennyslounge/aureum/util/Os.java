package io.github.lennyslounge.aureum.util;

public enum Os {
    WINDOWS,
    MAC,
    OTHER;

    public static Os getOs(){
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("windows")){
            return WINDOWS;
        }
        if(osName.contains("mac")){
            return MAC;
        }
        return OTHER;
    }
}
