import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.AttributeValueExp;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.StringValueExp;

/**
 * @Author: zwj
 * @Date: 2019-11-12 11:24
 * tomcat在jmx中的信息 提取重要的出来
 */
public class TomcatMetric {
    private static ObjectName objectName;
    private static final StringValueExp STRING_VALUE_EXP = new StringValueExp("http*");
    private static final AttributeValueExp ATTRIBUTE_VALUE_EXP = new AttributeValueExp("name");

    static {
        try {
            Set<ObjectName> objectNames = ManagementFactory.getPlatformMBeanServer().queryNames(new ObjectName("*:type=ThreadPool,name=*"),
                    Query.match(ATTRIBUTE_VALUE_EXP, STRING_VALUE_EXP));
            objectName = objectNames.iterator().next();
        } catch (Exception e) {
            //log.warn("TomcatMetric objectName init failed", e);
        }
    }

    public JsonObject getMetric() {
        try {
            MBeanServerConnection mbeanConn = ManagementFactory.getPlatformMBeanServer();
            MBeanAttributeInfo[] mBeanAttributeInfos = mbeanConn.getMBeanInfo(objectName).getAttributes();
            JsonObject jsonObject = new JsonObject();
            for (MBeanAttributeInfo attr : mBeanAttributeInfos) {

                if (attr.getName().equalsIgnoreCase("maxThreads")) {
                    //tomcat设置的最大线程数
                    jsonObject.addProperty("maxThreads", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("currentThreadCount")) {
                    //tomcat运行时状态，当前线程数
                    jsonObject.addProperty("currentThreadCount", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("currentThreadsBusy")) {
                    //tomcat运行时状态，当前活动线程数
                    jsonObject.addProperty("currentThreadsBusy", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("acceptCount")) {
                    //tomcat设置的等待队列数
                    jsonObject.addProperty("acceptCount", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("maxConnections")) {
                    //tomcat设置的可以同时处理的最大连接数
                    jsonObject.addProperty("maxConnections", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("connectionCount")) {
                    //tomcat运行时状态，当前连接数
                    jsonObject.addProperty("connectionCount", (Long) mbeanConn.getAttribute(objectName, attr.getName()));
                }
            }

            return jsonObject;
        } catch (Exception e) {
            //log.warn("获取tomcat监控失败", e);
        }
        return null;
    }
}
