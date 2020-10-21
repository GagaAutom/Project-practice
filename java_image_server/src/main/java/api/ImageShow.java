package api;

import dao.Image;
import dao.ImageDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

public class ImageShow extends HttpServlet {
    static private HashSet<String> whiletList = new HashSet<>();

    static {
        whiletList.add("http://47.104.62.86:8080/java_image_server/index.html");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //防盗链，白名单
        String referer = req.getHeader("Referer");
        if (!whiletList.contains(referer)){
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"未授权的非法访问！\" }");
            return;
        }
        //1. 解析出 imageID
        String imageID = req.getParameter("imageID");
        if (imageID == null || imageID.equals("")){
            resp.setStatus(200);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"imageID 解析失败！\" }");
        }
        //2. 根据 imageID 查找数据库，得到对应的图片属性信息（需要知道图片的存储路径path）
        ImageDao imageDao = new ImageDao();
        Image image = imageDao.selectOne(Integer.parseInt(imageID));
        if (image == null){
            //此时请求中传入的 id 在数据库中不存在
            resp.setStatus(200);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{ \"ok\": false,\"reason\": \"imageID在数据库中不存在！\"}");
            return;
        }
        //3. 根据路径打开文件，读取其中的内容，写入到响应对象中
        resp.setContentType(image.getContentType());
        File file = new File(image.getPath());
        // 由于图片是二进制文件，我们应该使用字节流的方式来读取文件
        FileInputStream fileInputStream = new FileInputStream(file);
        OutputStream outputStream = resp.getOutputStream();
        byte[] bytes = new byte[1024];
        while (true){
            int len = fileInputStream.read(bytes);
            if (len == -1){
                //文件读取结束
                break;
            }
            // 此时已经读到一部分数据，放在 bytes 里，要把bytes中的内容写到响应对象中
            outputStream.write(bytes);
        }
        fileInputStream.close();
        outputStream.close();
    }
}
