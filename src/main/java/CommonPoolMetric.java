import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @Author: zwj
 * @Date: 2019-11-12 11:20
 * 使用ManagementFactory.getPlatformMBeanServer() 替换CommonPoolStatMetric 类中从虚拟机中找程序连接jmx的过程,这个过程需要依赖jdk的tools包而且需要自己管理连接不好
 */
public class CommonPoolMetric {

    private String jmxName;
    private ObjectName objectName;

    CommonPoolMetric(String jmxName) {
        this.jmxName = jmxName;
        try {
            this.objectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + jmxName);
        } catch (MalformedObjectNameException e) {
            //log.warn("CommonPoolMetric objectName init failed", e);
        }

    }

    public JsonObject getMetric() {
        try {
            MBeanServerConnection mbeanConn = ManagementFactory.getPlatformMBeanServer();
            MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectName);
            MBeanAttributeInfo[] mBeanAttributeInfos = mBeanInfo.getAttributes();
            JsonObject jsonObject = new JsonObject();
            for (MBeanAttributeInfo attr : mBeanAttributeInfos) {

                if (attr.getName().equalsIgnoreCase("MaxIdle")) {
                    //连接池设置的最大空闲数
                    jsonObject.addProperty("maxIdle", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("MinIdle")) {
                    //连接池设置的最小空闲数
                    jsonObject.addProperty("minIdle", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("NumActive")) {
                    //连接池设运行时状态，活动的连接数
                    jsonObject.addProperty("numActive", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("numIdle")) {
                    //连接池设运行时状态，空闲的连接数
                    jsonObject.addProperty("numIdle", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("numWaiters")) {
                    //连接池设运行时状态，等待的连接请求数
                    jsonObject.addProperty("numWaiters", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("MaxTotal")) {
                    //连接池设置的最大的连接数
                    jsonObject.addProperty("maxTotal", (Integer) mbeanConn.getAttribute(objectName, attr.getName()));
                } else if (attr.getName().equalsIgnoreCase("CreatedCount")) {
                    //连接池设运行时状态,在启动到现在总共创建的连接数
                    jsonObject.addProperty("CreatedCount", (Long) mbeanConn.getAttribute(objectName, attr.getName()));
                }
            }

            return jsonObject;
        } catch (Exception e) {
            // log.warn("获取redis连接池失败，jmxName:{},", jmxName, e);
        }
        return null;
    }

}
