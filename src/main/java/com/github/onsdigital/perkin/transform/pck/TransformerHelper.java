package com.github.onsdigital.perkin.transform.pck;

import org.apache.commons.lang3.StringUtils;

public abstract class TransformerHelper {

    private static final char ZERO = '0';

    public static String leftPadZeroes(String str, int length) {
        return StringUtils.leftPad(str, length, ZERO);
    }
}
