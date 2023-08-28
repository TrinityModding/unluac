package me.hydos.unluac.test.legacy;

import java.util.Collections;
import java.util.List;

public class LuaSpec {

    private final boolean isDefault;
    private final int version;
    private final int minorVersion;
    private final NumberFormat numberFormat;
    private final boolean strip;

    public LuaSpec(int version) {
        this(version, -1);
    }

    public LuaSpec(int version, int minorVersion) {
        this.isDefault = false;
        this.version = version;
        this.minorVersion = minorVersion;
        this.numberFormat = NumberFormat.DEFAULT;
        this.strip = false;
    }

    public String id() {
        var id = "lua";
        id += Integer.toHexString(version);
        id += getMinorVersionString();
        return id;
    }

    public String getLuaCName() {
        return "luac" + getVersionString() + getMinorVersionString() + getNumberFormatString();
    }

    public List<String> getArgs() {
        return strip ? List.of("-s") : Collections.emptyList();
    }

    public boolean compatible(String filename) {
        var version = 0;
        var underscore = filename.indexOf('_');
        if (underscore != -1) {
            var prefix = filename.substring(0, underscore);
            try {
                version = Integer.parseInt(prefix, 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return version == 0 || this.version >= version;
    }

    private String getVersionString() {
        if (isDefault) {
            return "";
        } else {
            return Integer.toHexString(version);
        }
    }

    private String getMinorVersionString() {
        if (minorVersion >= 0) {
            return Integer.toString(minorVersion);
        } else {
            return "";
        }
    }

    private String getNumberFormatString() {
        return switch (numberFormat) {
            case DEFAULT -> "";
            case FLOAT -> "_float";
            case INT32 -> "_int32";
            case INT64 -> "_int64";
        };
    }
    public enum NumberFormat {
        DEFAULT,
        FLOAT,
        INT32,
        INT64
    }
}
