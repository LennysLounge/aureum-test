package io.github.lennyslounge.aureum.ReflectionWriterTest;

public class ClassWithDifferentFeatures {

    private String privateField = "private fields should not be visible";
    protected String protectedField = "protected fields should not be visible";
    String defaultFields = "ok";
    public String publicField = "ok";


    private String privateFieldWithGetter = "ok";
    public String getPrivateFieldWithGetter(){
        return privateFieldWithGetter;
    }

    protected String protectedFieldWithGetter = "ok";
    public String getProtectedFieldWithGetter(){
        return protectedFieldWithGetter;
    }

    // Private getters should not make private field visible
    private String getPrivateField(){
        return privateField;
    }

    // Protected getters should not make protected fields visible
    protected String getProtectedField(){
        return protectedField;
    }

    public static String publicStaticField = "static fields should not be visible";

    // Not visible fields should only use the getter if the getter
    // follows either standard getter format or record format.
    public String getprivateField(){
        return privateField;
    }
    public String get_PrivateField(){
        return privateField;
    }
    public String getPRIVATE_FIELD(){
        return privateField;
    }

    private String recordFieldGetter = "ok";
    public String recordFieldGetter(){
        return recordFieldGetter;
    }

    // Not visible booleans fields should use the getter if the getter
    // follows either standard getter format, record format or "is" format.
    private boolean booleanWithGetter = true;
    public boolean getBooleanWithGetter(){
        return booleanWithGetter;
    }

    private boolean booleanWithRecordGetter = true;
    public boolean booleanWithRecordGetter(){
        return booleanWithRecordGetter;
    }

    private boolean booleanWithIsGetter = true;
    public boolean isBooleanWithIsGetter(){
        return booleanWithIsGetter;
    }

    private Boolean boxedBooleanWithGetter = true;
    public boolean getBoxedBooleanWithGetter(){
        return boxedBooleanWithGetter;
    }

    private Boolean boxedBooleanWithRecordGetter = true;
    public boolean boxedBooleanWithRecordGetter(){
        return boxedBooleanWithRecordGetter;
    }

    private Boolean boxedBooleanWithIsGetter = true;
    public boolean isBoxedBooleanWithIsGetter(){
        return boxedBooleanWithIsGetter;
    }

    // a boolean that already has the "is" prefix may work with a single
    // "is" or double "is" prefix.
    private boolean isBooleanWithIsPrefix = true;
    public boolean isBooleanWithIsPrefix(){
        return isBooleanWithIsPrefix;
    }
    private boolean isBooleanWithIsPrefixButMethodHasDoublePrefix = true;
    public boolean isIsBooleanWithIsPrefixButMethodHasDoublePrefix(){
        return isBooleanWithIsPrefixButMethodHasDoublePrefix;
    }
}
