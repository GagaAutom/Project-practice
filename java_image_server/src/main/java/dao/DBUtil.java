package dao;//数据访问层

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;


public class DBUtil {
    private static final String URL = "jdbc:mysql://rm-m5eb14wun22842aq3.mysql.rds.aliyuncs.com:3306/java_image_server?characterEncoding=utf8&useSSL=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "JAVAxuexi4";

    //加上volatile关键字保证每次线程访问都是更新的
    private static volatile DataSource dataSource = null;

    //连接url
    public static DataSource getDataSource(){
        //通过这个方法来创建 DataSource 的实例
        //单例模式，这样是线程不安全的，线程二容易发生抢占，多次创建dataSource
//        if (dataSource == null){
//            dataSource = new MysqlDataSource();
//        }
//        return dataSource;

        //线程安全方案：1)加锁 2）双重判定 3）volatile
        //仅仅只是加锁，是低效的，每次调用方法都会屏障，因此需要再加一层判断
        if (dataSource == null) {
            //只需保证第一次调用时获取锁，保证线程不能多次创建类即可
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    dataSource = new MysqlDataSource();
                    MysqlDataSource tmpDataSource = (MysqlDataSource)dataSource;
                    tmpDataSource.setURL(URL);
                    tmpDataSource.setUser(USERNAME);
                    tmpDataSource.setPassword(PASSWORD);
                }
            }
        }
        return dataSource;
    }

    //获取具体链接
    public static Connection getConnection(){
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //使用完毕要关闭连接
    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet){
        //关闭有顺序，不能打乱，遵循最先创建的最后关，最后创建的最后关
        try {
            if (resultSet != null){
                resultSet.close();
            }
            if (statement != null){
                statement.close();
            }
            if (connection != null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
