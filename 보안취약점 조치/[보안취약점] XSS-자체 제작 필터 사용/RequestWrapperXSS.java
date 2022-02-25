


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class RequestWrapperXSS extends HttpServletRequestWrapper {

        public RequestWrapperXSS(HttpServletRequest servletRequest) {
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
                encodedValues[i] = cleanXSS(values[i]);
            }

            return encodedValues;
        }

        public String getParameter(String parameter) {

            String value = super.getParameter(parameter);
            if (value == null) {
                return null;
            }
            return cleanXSS(value);
        }

        public String getHeader(String name) {

            String value = super.getHeader(name);
            if (value == null)
                return null;
            return cleanXSS(value);

        }

//        private String cleanXSS(String value) {
//            //You'll need to remove the spaces from the html entities below
//            value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
//            value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
//            value = value.replaceAll("'", "& #39;");
//            value = value.replaceAll("eval\\((.*)\\)", "");
//            value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
//            value = value.replaceAll("script", "");
//            return value;
//        }

    private String cleanXSS(String cadena) {
        StringBuffer sb = new StringBuffer(cadena.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = cadena.length();
        char c;

        for (int i = 0; i < len; i++)
        {
            c = cadena.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                }
                else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            }
            else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else if (c == '\n')
                    // Handle Newline
                    sb.append("&lt;br/&gt;");
                else {
                    int ci = 0xffff & c;
                    if (ci < 160 )
                        // nothing special only 7 Bit
                        sb.append(c);
                    else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(new Integer(ci).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

}