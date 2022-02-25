package securus.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created by kimsc on 2019-02-20.
 */
public class ClickjackFilter implements Filter{

/*    private String mode = "SAMEORIGIN";*/
/*    private String mode = "DENY";*/
    private String mode = "ALLOW-FROM";
    /**
     * Add X-FRAME-OPTIONS response header to tell IE8 (and any other browsers who
     * decide to implement) not to display this content in a frame. For details, please
     * refer to http://blogs.msdn.com/sdl/archive/2009/02/05/clickjacking-defense-in-ie8.aspx.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse)response;
        res.setHeader("Access-Control-Allow-Origin", "http://localhost:9080");
        res.addHeader("X-FRAME-OPTIONS","ALLOW-FROM http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept");
        //ClickJacking
        res.addHeader("X-FRAME-OPTIONS", mode );
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
        String configMode = filterConfig.getInitParameter("mode");
        if ( configMode != null ) {
            mode = configMode;
        }
    }
}
