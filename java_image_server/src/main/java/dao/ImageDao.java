package dao;

import common.JavaImagerServerException;
import org.omg.PortableInterceptor.ServerRequestInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImageDao {
    /**
     * 把image对象插入到数据库中
     */
    public void insert(Image image){
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 创建并拼装 SQL 语句（使用PrepareStatement拼装）
        String sql = "insert into image_table values(null,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);//受查异常
            statement.setString(1,image.getImageName());
            statement.setInt(2,image.getSize());
            statement.setString(3,image.getUpLoadTime());
            statement.setString(4,image.getContentType());
            statement.setString(5,image.getPath());
            statement.setString(6,image.getMd5());

            //3. 执行 SQL 语句
            int ret = statement.executeUpdate();//更新数据库
            if (ret != 1) {
                // 程序出现问题，抛出一个异常
                throw new JavaImagerServerException("插入数据库出错！");
            }
        } catch (SQLException | JavaImagerServerException e) {
            e.printStackTrace();
        }finally {
            //4. 关闭连接和statement对象
            DBUtil.close(connection,statement,null);
        }
    }

    /**
     * 查找数据库中的所有图片的信息
     * @return
     */
    public List<Image> selectAll(){
        List<Image> images = new ArrayList<>();
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 构造 SQL 语句
        String sql = "select * from image_table";
        //3. 执行 SQL 语句
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();//查找数据库
            //4. 处理结果集
            while (resultSet.next()){//判断当前有没有得到结果集
                Image image = new Image();
                image.setImageID(resultSet.getInt("imageID"));//将获取到的imageID插入到image对象中
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUpLoadTime(resultSet.getString("upLoadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                images.add(image);
            }
            return images;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //5. 关闭连接
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }

    /**
     * 根据imageID查找指定图片信息
     * @param imageID
     * @return
     */
    public  Image selectOne(int imageID){
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 构造 SQL 语句
        String sql = "select * from image_table where imageID = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //3. 执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setInt(1,imageID);
            resultSet = statement.executeQuery();
            //4. 处理结果集
            if (resultSet.next()){
                Image image = new Image();
                image.setImageID(resultSet.getInt("imageID"));//将获取到的imageID插入到image对象中
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUpLoadTime(resultSet.getString("upLoadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //5. 关闭连接
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }

    /**
     * 根据imageID删除指定图片
     * @param imageID
     */
    public void delete(int imageID){
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 拼装 SQL 语句
        String sql = "delete from image_table where imageID = ?";
        PreparedStatement statement = null;
        //3. 执行 SQL 语句
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,imageID);
            int ret = statement.executeUpdate();

            if (ret != 1){
                throw new JavaImagerServerException("删除数据库操作失败");
            }
        } catch (SQLException | JavaImagerServerException e) {
            e.printStackTrace();
        }finally {
            //4. 关闭连接
            DBUtil.close(connection,statement,null);
        }
    }

    /**
     * 用于简单测试
     * @paramargs
     */
  //  public static void main(String[] args) {
        //1.测试插入数据
        //数据库连接使用的是127.0.0.1时会出现报错：
        //由于数据库是外置在云服务器上的，不在本地，这个程序直接在本地运行是无法访问数据库的
        //此时应该部署在云服务器上才能看到效果
        //具体步骤：打一个jar包，把jar包部署在云服务器上，就可以了
//        Image image = new Image();
//        image.setImageName("1.png");
//        image.setSize(100);
//        image.setUpLoadTime("20201018");
//        image.setContentType("image/png");
//        image.setPath("./data/1.png");
//        image.setMd5("11223344");
//        ImageDao imageDao = new ImageDao();
//        imageDao.insert(image);

        //2. 测试查找所有图片信息
//        ImageDao imageDao = new ImageDao();
//        List<Image> images = imageDao.selectAll();
//        System.out.println(images);

        //3. 测试查找指定图片的信息
//        ImageDao imageDao = new ImageDao();
//        Image image = imageDao.selectOne(1);
//        System.out.println(image);

        //4. 测试删除指定图片信息
//        ImageDao imageDao = new ImageDao();
//        imageDao.delete(1);
  //  }

    public Image selectByMd5(String md5){
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 构造 SQL 语句
        String sql = "select * from image_table where md5 = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //3. 执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setString(1,md5);
            resultSet = statement.executeQuery();
            //4. 处理结果集
            if (resultSet.next()){
                Image image = new Image();
                image.setImageID(resultSet.getInt("imageID"));//将获取到的imageID插入到image对象中
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUpLoadTime(resultSet.getString("upLoadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //5. 关闭连接
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }
}
