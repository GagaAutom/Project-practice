package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageServlet extends HttpServlet {
    /**
     * 实现插入图片功能
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1. 获取图片的属性信息，并且存入数据库
        //1）需要创建一个 factory 对象和 upload 对象，这是为了获取到图片属性做的准备工作
        //    固定逻辑
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        //2）通过 upload 对象进一步解析请求，解析HTTP请求中奇怪的body中的内容
        //       FileItem就代表一个上传文件的对象
        //       理论上，HTTP支持一个请求中同时上传多个文件
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(req);
        } catch (FileUploadException e) {
            //出现异常说明解析出错！
            e.printStackTrace();

            //告诉客户端出现的具体错误是什么
            resp.setStatus(200);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"请求解析失败！\"}");//JSON数据格式
            return;
        }
        //3）把 FileItem 对象中的属性提取出来，转换成Image对象，才能存储到数据库中
        //    当前只考虑一张图片的情况
        FileItem fileItem = items.get(0);
        Image image = new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int)fileItem.getSize());
        //手动获取一下当前日期，并转换成格式化日期，yy-MM-dd 对应 2020-10-20
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        image.setUpLoadTime(simpleDateFormat.format(new Date()));//报文中不含上传时间信息，我们可以直接取当前服务器时间
        image.setContentType(fileItem.getContentType());
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));
        //需要我们自己构造一个路径来储存，加入时间戳保证路径唯一
        image.setPath("./image/" + image.getMd5() + "_" + image.getImageName());
        //存到数据库当中
        ImageDao imageDao = new ImageDao();
        //查看数据库中是否存在相同的 MD5 的图片，不存在就返回null；
        Image existImage = imageDao.selectByMd5(image.getMd5());

        imageDao.insert(image);

        //2. 获取图片的内容信息，并且写入磁盘文件
        File file = new File(image.getPath());
        if (existImage == null) {
            try {
                fileItem.write(file);
            } catch (Exception e) {
                e.printStackTrace();

                resp.setStatus(200);
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().write("{ \"ok\": false,\"reason\": \"写入磁盘失败！\"}");
                return;
            }
        }
        //3. 给客户端返回一个结果数据
        resp.setStatus(302);
//        resp.setContentType("application/json; charset=utf-8");
//        resp.getWriter().write("{ \"ok\": true }");
        resp.sendRedirect("index.html");
    }

    /**
     * 实现查看所有和查看指定图片的属性功能
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 考虑到查看所有图片属性和查看指定图片属性
        // 通过查看 URL 中 是否带有 imageID 的参数区分两种方法
        // 存在 imageID 查看指定图片属性，否则就查看所有图片属性
        // 如果 URL 种不存在 imageID 那么返回 null
        String imageID = req.getParameter("imageID");
        if (imageID == null || imageID.equals("")){
            //被认为使用查看所有图片属性方法
            selectAll(req,resp);
        }else {
            // 查看指定图片属性
            selectOne(imageID,resp);
        }
    }

    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //1. 创建一个 ImageDao 对象，并查找数据库
        ImageDao imageDao = new ImageDao();
        List<Image> images = imageDao.selectAll();
        //2. 把查找到的结果转成 JSON 格式字符串，并且写入 resp 对象
        if (images == null){
            //此时请求在数据库中查找不到数据
            resp.setStatus(200);
            resp.getWriter().write("[ ]");
            return;
        }
        Gson gson = new GsonBuilder().create();
        //   jsonData字符串就和我们设计时约定的是一样的
        //  只要把之前的相关字段都统一约定成一样的命名格式，下面的操作就可以一步到位的完成转换
        String jsonData = gson.toJson(images);
        resp.setContentType("application/json; charset=utf-8");
        resp.setStatus(200);
        resp.getWriter().write(jsonData);
    }

    private void selectOne(String imageID, HttpServletResponse resp) throws IOException {
        ImageDao imageDao = new ImageDao();
        Image image = imageDao.selectOne(Integer.parseInt(imageID));
        resp.setContentType("application/json; charset=utf-8");
        if (image == null){
            //此时请求中传入的 id 在数据库中不存在
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"imageID在数据库中不存在！\"}");
            return;
        }
        Gson gson = new GsonBuilder().create();
        String jsonData = gson.toJson(image);
        resp.setContentType("application/json; charset=utf-8");
        resp.setStatus(200);
        resp.getWriter().write(jsonData);
    }
    /**
     * 实现删除指定图片功能
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");
        //1. 先获取到请求中的 imageID
        String imageID = req.getParameter("imageID");
        if (imageID == null || imageID.equals("")){
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"解析请求失败！\"}");
            return;
        }
        //2. 创建ImageDao 对象，查看到该图片对象对应的相关属性，这是为了知道这个图片对应的文件路径
        ImageDao imageDao = new ImageDao();
        Image image = imageDao.selectOne(Integer.parseInt(imageID));
        if (image == null){
            //此时请求中传入的 id 在数据库中不存在
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"imageID在数据库中不存在！\"}");
            return;
        }
        //3. 删除数据库中的记录
        imageDao.delete(Integer.parseInt(imageID));
        //查看数据库中是否存在相同的 MD5 的图片，不存在就返回null；
        Image existImage = imageDao.selectByMd5(image.getMd5());
        //4. 删除本地磁盘文件
        if (existImage != null) {
            File file = new File(image.getPath());
            file.delete();
        }
        resp.setStatus(200);
        resp.getWriter().write("{ \"ok\": true }");
    }
}
