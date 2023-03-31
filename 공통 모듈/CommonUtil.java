
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtil {

    /**
     * LTrim 왼쪽 공백문자 제거
     * 
     * @param str 변환전 문자열
     * @return result 변환후 문자열
     */
    public static String lTrim(String str) {
        if(str == null) {
            return str;
        }

        String result = str.replaceAll("^\\s+", "");
        return result;
    }

    /**
     * RTrim 오른쪽 공백문자 제거
     * 
     * @param str 변환전 문자열
     * @return result 변환후 문자열
     */
    public static String rTrim(String str) {
        if(str == null) {
            return str;
        }

        String result = str.replaceAll("\\s+$", "");
        return result;
    }

    /**
     * Trim 공백문자 제거 - 왼쪽,오른쪽 공백은 제거한 후, 문자간의 공백은 1문자로 변경한다
     *  Ex) a  b   c  -> a b c
     * 
     * @param str 변환전 문자열
     * @return result 변환후 문자열
     */
    public static String trim(String str) {
        if(str == null) {
            return str;
        }
        String result = str.trim();

        if(result.length() > 3) {
            StringBuffer sb = new StringBuffer();
            String[] tmpStrs = result.split("\\s+");
            if(tmpStrs.length > 1) {
                for(String tmpStr : tmpStrs) {
                    sb.append(tmpStr);
                    sb.append(" ");
                }
                result = sb.toString().trim();
            }
        }

        return result;
    }

    /**
     * VO의 모든 String 객체 공백문자 제거 - 왼쪽,오른쪽 공백은 제거한 후, 문자간의 공백은 1문자로 변경한다
     *  Ex) a  b   c  -> a b c
     * 
     * @param vo 변환전 VO object
     * @return
     */
    public static void trimForVOStringFields(Object vo) {
        if(vo == null) {
            return;
        }

        Field[] fields = vo.getClass().getDeclaredFields();
        if (fields == null) {
            return;
        }

        for (Field f : fields) {
            if (f.getType().isPrimitive()) {
                continue;
            }

            if (f.getType().equals(String.class)) {
                try {
                    f.setAccessible(true);
                    String value = (String) f.get(vo);
                    f.set(vo, trim(value));
                } catch (IllegalAccessException e) {
                    continue;
                }

            }
        }
    }

    /**
     * 문자열 체크(null, 공문자)
     * 
     * @param text 체크 대상 문자열
     * @return trur/false
     */
    public static boolean isValidText(String text) {
        if(text == null || text.length() == 0 || text.equalsIgnoreCase("null")) {
            return false;
        }
        return true;
    }

    /**
     * Date Format 형식의 날짜 문자열(Date String)을 지정한 날짜 포맷 형식의 문자열로 변환
     * @param inputDate(ex:"99991231010101")
     * @param inputDateformat(ex:"yyyyMMddHHmmss")
     * @param outDateformat(ex:"yyyy-MM-dd HH:mm:ss")
     * @return
     */
    public static String convertDateToStr(String inputDate, String inputDateformat, String outDateformat) {
        try {
            if(inputDate == null || inputDate.isEmpty()) {
                return null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(inputDateformat, Locale.getDefault());
            Date data = sdf.parse(inputDate);
            sdf.applyPattern(outDateformat);
            return sdf.format(data);

        } catch (ParseException ex) {

            /*
            String errorMessage = null;
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                errorMessage = sw.toString();
                sw.close();
                pw.close();
            }catch(IOException e1)
            {
                
            }
            */
            
			ex.getMessage();
        }
    }

    /**
     * Date Format 형식의 날짜 문자열(Date String)을 Date 객체로 변환
     * @param inputDate(ex:"9999-12-31 01:01:01")
     * @param inputDateformat(ex:"yyyy-MM-dd HH:mm:ss")
     * @return
     */
    public static Date convertStrToDate(String inputDate, String inputDateformat) {
        try {
            if(inputDate == null || inputDate.isEmpty()) {
                return null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(inputDateformat, Locale.KOREA);
            return sdf.parse(inputDate);

        } catch (ParseException ex) {
            /*
            String errorMessage = null;
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                errorMessage = sw.toString();
                sw.close();
                pw.close();
            }catch(IOException e1)
            {
                
            }
            */

			ex.getMessage();
        }
    }
}
