import com.google.gson.JsonObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @Author: zwj
 * @Date: 2019-10-29 17:43
 */
public class CommonPoolStatMetric {

    private String jmxName;
    private static String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    private static String localConnectorAddress = null;

    static {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : vms) {
            if (!desc.id().equals(pid)) {
                continue;
            }
            VirtualMachine vm;
            try {
                vm = VirtualMachine.attach(desc);
            } catch (Exception e) {
                continue;
            }
            try {
                Properties props = vm.getAgentProperties();
                localConnectorAddress = props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
            } catch (Exception e) {
            }
        }
    }

    /*
    jmxName 是org.apache.commons.pool2 里面注册jmx的jmxNamePrefix 配置，默认是pool
    当一个项目中用了多个commons.pool 的时候更具这个来区分查找
    比如用了redis 多数据源，jedis/lettcue
     */
    CommonPoolStatMetric(String jmxName) {
        this.jmxName = jmxName;
    }

    public JsonObject getMetric() {
        if (localConnectorAddress == null) {
            return null;
        }
        try {
            JMXServiceURL url = new JMXServiceURL(localConnectorAddress);
            JMXConnector connector = JMXConnectorFactory.connect(url);
            MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
            ObjectName objectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + jmxName);
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

        }
        return null;
    }
}
