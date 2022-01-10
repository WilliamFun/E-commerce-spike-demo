package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@EnableAutoConfiguration//启动内嵌的tomcat，加载默认的配置
//把该启动类当成可以自动配置的bean，开启整个工程基于Springboot的自动化配置
@RestController
//实现MVC一些复杂的配置
public class App 
{
    @RequestMapping("/")
    public String home(){
        return "hello world!";
    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class,args);//启动Springboot项目
    }
}
