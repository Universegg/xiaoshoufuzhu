package com.example.xiaoshoufuzhu;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mysql);

        testMyql();
    }
    private void testMyql(){
        new Thread(new Runnable() {//新建线程
            @Override
            public void run() {


                try {
                    Log.v("tag","run");//调试信息
                    Class.forName("com.mysql.jdbc.Driver");//加载驱动
                    java.sql.Connection cn= DriverManager.getConnection("jdbc:mysql://172.20.10.7:3306/agri_sales","root","123456");//连接
                    String sql="select * from products";//准备语句
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);//执行

                    while(rs.next()){//遍历结果
                        final String mybook=rs.getString("name");//查找字段
                        Log.v("tag",mybook+"");//调试信息

                    }
                    cn.close();//记得关闭 不然内存泄漏
                    st.close();
                    rs.close();
                    Log.v("tag","end");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();//执行线程
    }
}