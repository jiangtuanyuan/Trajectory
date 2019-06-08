package org.xiaobai.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * PickerView  仿IOS
 */
public class PickerViewUtils {

    public static void showDatePicker(Context context, final TextView textView) {
        //时间选择器
        TimePickerView pvTime = new TimePickerView.Builder(context, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                textView.setText(DateUtils.getTime(date));
            }
        })//默认全部显示
                .setContentSize(16)//滚轮文字大小
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setLabel("年", "月", "日", "时", "分", "秒")
                .build();
        pvTime.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
        pvTime.show();
    }

    public static String getTime(Date date, boolean[] booleans) {
        if (booleans[1]) {
            //可根据需要自行截取数据显示
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format(date);
        } else {
            //可根据需要自行截取数据显示
            SimpleDateFormat format = new SimpleDateFormat("yyyy");
            return format.format(date);
        }
    }

    public static String getTime(Date date) {
        //可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static Date strToDate(String szBeginTime) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(szBeginTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 判斷是否時過期時間
     *
     * @param targetDate
     * @return true 过期 ， false 没有过期
     */
    public static boolean comparaData(String targetDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String currentTime = df.format(d);
        try {
            Date targetTime = df.parse(targetDate);
            Date curentTime = df.parse(currentTime);
            if (targetTime.getTime() <= curentTime.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取时间 yyyy-MM-dd hh:mm:ss
     */

    public static String getDate() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//如果hh为小写 那么就搜12小时制 如果为大写 那么就是24小时制
            Date day = new Date();
            return df.format(day);
        } catch (Exception e) {
            return "0000-00-00 00:00";
        }

    }

    public static String getDateYMDHMS() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//如果hh为小写 那么就搜12小时制 如果为大写 那么就是24小时制
            Date day = new Date();
            return df.format(day);
        } catch (Exception e) {
            return "0000-00-00 00:00";
        }
    }


    /**
     * 通过日期判断是周几
     *
     * @throws ParseException
     */
    public static String DateToDay(String daydate) {
        String dayNames[] = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar c = Calendar.getInstance();// 获得一个日历的实例
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            c.setTime(sdf.parse(daydate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dayNames[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * 判断时间是不是今天
     *
     * @param date
     * @return 是返回true，不是返回false
     */
    public static boolean isNow(String date) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //获取今天的日期
        String nowDay = sf.format(now);
        //对比的时间
        return date.equals(nowDay);

    }

    /**
     * 获取前一天的时间 yyyy-MM-dd
     * return:String
     */
    public static String getBeforeNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return sdf.format(date);
    }

    /**
     * 判断addtime是否在七天之内
     *
     * @param addtime
     * @return
     */
    public static boolean isLatestWeek(String addtime) {
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date now1;
        Date now2;
        try {
            now1 = format1.parse(getBeforeNow());
            now2 = format1.parse(addtime);
            Calendar calendar = Calendar.getInstance();  //得到日历
            calendar.setTime(now1);//把当前时间赋给日历
            calendar.add(Calendar.DAY_OF_MONTH, -7);  //设置为7天前
            Date before7days = calendar.getTime();   //得到7天前的时间
            if (before7days.getTime() <= now2.getTime())
                return true;

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 格式化时间
     *
     * @param date
     * @return
     */
    public static String getDate(Date date) {
        //可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

}
