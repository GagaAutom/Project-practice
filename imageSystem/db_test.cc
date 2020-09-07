#include "db.hpp"


void TestImageTable()
{
  //创建一个ImageTable 类，去调用其中的方法， 验证结果
  Json::StyledWriter writer;/////////////////////////
  MYSQL* mysql = image_system::MySQLInit();////////////
  image_system::ImageTable image_table(mysql);
  bool ret = false;

  /*
  //1.插入数据
   Json::Value image;
   image["image_name"] ="test.png";
   image["size"] = 1024;
   image["upload_time"] = "2019/8/29";
   image["md5"] = "abcdef";  
   image["type"] ="png";  
   image["path"] = "data/test.png";
   ret = image_table.Insert(image);
   printf("ret = %d\n", ret);
   */ 

   //2.查看所有图片信息
   /*Json::Value images;
   ret = image_table.SelectAll(&images);
   printf("ret = %d\n", ret);
   printf("%s\n", writer.write(images).c_str());
   */
   //3.查找指定图片信息
   /*Json::Value image1;
   ret = image_table.SelectOne(1,&image1);
   printf("ret = %d\n", ret);
   printf("%s\n", writer.write(image1).c_str()); 
   */
   //4.删除指定图片
   ret = image_table.Delete(1);
   printf("%d\n", ret);
   
   image_system::MySQLRelease(mysql);
}
int main()
{
  TestImageTable();
  return 0;
}
