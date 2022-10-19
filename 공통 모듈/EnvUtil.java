

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvUtil
{
    @Autowired
    private Environment environment;

    private String port;
    private String hostname;

    /**
     * Get port.
     *
     * @return
     */
    public String getPort()
    {
        if (port == null) {
            port = environment.getProperty("local.server.port");
        }
        return port;
    }

    /**
     * Get port, as Integer.
     *
     * @return
     */
    public Integer getPortAsInt()
    {
        return Integer.valueOf(getPort());
    }

    /**
     * Get Hostname.
     *
     * @return
     */
    public String getHostname() throws UnknownHostException
    {
        // TODO ... would this cache cause issue, when network env change ???
        if (hostname == null) {
            hostname = InetAddress.getLocalHost().getHostAddress();
        }
        return hostname;
    }

    public String getServerUrlPrefi() throws UnknownHostException
    {
        return "http://" + getHostname() + ":" + getPort();
    }
}
