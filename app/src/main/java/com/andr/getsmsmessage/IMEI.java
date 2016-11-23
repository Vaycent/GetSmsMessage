package com.andr.getsmsmessage;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Vaycent on 2016/11/21.
 */

public class IMEI {
    /** SIM卡是中国移动 */
    public static boolean isChinaMobile(Context context) {
        String imsi = getSimOperator(context);
        if (imsi == null)
            return false;
        return imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007");
    }

    /** SIM卡是中国联通 */
    public static boolean isChinaUnicom(Context context) {
        String imsi = getSimOperator(context);
        if (imsi == null)
            return false;
        return imsi.startsWith("46001");
    }

    /** SIM卡是中国电信 */
    public static boolean isChinaTelecom(Context context) {
        String imsi = getSimOperator(context);
        if (imsi == null) return false;
        return imsi.startsWith("46003");
    }

    private static String getSimOperator(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }
}
