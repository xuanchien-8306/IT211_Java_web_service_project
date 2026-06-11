package com.rikkei.bank.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    public static String formatVND(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);

        return currencyVN.format(amount);
    }
}