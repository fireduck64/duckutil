package duckutil.sql;

import duckutil.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class DBUtil
{
    private static void loadDriver(String driver)
        throws SQLException
    {
        try
        {
            Class.forName(driver).newInstance();
        }
        catch(Exception e)
        {
            throw new SQLException("Unable to load driver: " + driver);
        }
    }

    public static void openConnectionPool(String pool_name, Config config)
        throws SQLException
    {
      config.require(pool_name +"_db_driver");
      config.require(pool_name +"_db_uri");
      config.require(pool_name +"_db_username");
      config.require(pool_name +"_db_password");

      openConnectionPool(pool_name, 
        config.get(pool_name + "_db_driver"),
        config.get(pool_name + "_db_uri"),
        config.get(pool_name + "_db_username"),
        config.get(pool_name + "_db_password"),
        config.getIntWithDefault(pool_name +"_db_max_active", 16),
        config.getIntWithDefault(pool_name +"_db_max_idle", 4));
    }

    public static void openConnectionPool(String pool_name, String driver_class, String uri, String user, String pass, int max_active, int max_idle)
        throws SQLException
    {

        Properties props = new Properties();
        props.put("autoReconnect","true");
        props.put("user",user);
        props.put("password",pass);

        loadDriver(driver_class);
        loadDriver("org.apache.commons.dbcp.PoolingDriver");


        GenericObjectPool connectionPool = new GenericObjectPool(null);

        connectionPool.setMaxActive(max_active);
        connectionPool.setMaxIdle(max_idle);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, props);

        new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

        driver.registerPool(pool_name,connectionPool);

    }


    public static Connection openConnection(String pool_name)
        throws SQLException
    {
        Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + pool_name);
        conn.setAutoCommit(true);
        return conn;

    }
    public static void safeClose(Connection conn)
    {
        if (conn==null) return;
        try
        {
            conn.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            
        }
    }

    public static void printDriverStats(String pool_name) throws Exception
    {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        ObjectPool connectionPool = driver.getConnectionPool(pool_name);
        System.out.println("NumActive: " + connectionPool.getNumActive());
        System.out.println("NumIdle: " + connectionPool.getNumIdle());
    }


}
