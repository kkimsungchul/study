package filter;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RequestWrapperSQL extends HttpServletRequestWrapper {

        public RequestWrapperSQL(HttpServletRequest servletRequest) {
            super(servletRequest);
        }

        public String[] getParameterValues(String parameter) {

            String[] values = super.getParameterValues(parameter);
            if (values==null)  {
                return null;
            }
            int count = values.length;
            String[] encodedValues = new String[count];
            for (int i = 0; i < count; i++) {
                encodedValues[i] = cleanSQL(values[i]);
            }

            return encodedValues;
        }

        public String getParameter(String parameter) {

            String value = super.getParameter(parameter);
            if (value == null) {
                return null;
            }
            return cleanSQL(value);
        }

        public String getHeader(String name) {

            String value = super.getHeader(name);
            if (value == null)
                return null;
            return cleanSQL(value);

        }



    private String cleanSQL(String value) {

            //특수문자 공백처리
            Pattern SpecialCharacters = Pattern.compile("['\"\\-#;*/+]");
            value = SpecialCharacters.matcher(value).replaceAll("");
            //SQL 명령어 공백처리
            String regex ="(database|concat|select|having|union|insert|delete|drop|column|instance|null|0x20|0x21|0x27|0x28|0x29|0x2A|0x2B|0x2D|0x2F)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            value = pattern.matcher(value).replaceAll("");
            return value;
    }
}