package com.offcn.config;

import jdk.internal.dynalink.beans.StaticClass;

public class Singleton {
    //懒汉式线程不安全（线程安全和线程不安全多些一个）
 /*   private static Singleton instance; //单例中的实例，也是唯一一个
                                         //成员变量就是单例对象的声明
    public Singleton() {
    }

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }*/
    //懒汉式线程安全，懒实例化，方法调用才实例化
  /*  private static Singleton instance; //单例中的实例，也是唯一一个

    public Singleton() {
    }

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }*/
  //饿汉式线程安全，饿实例化，类加载就创建实例
    private static Singleton instance = new Singleton();
    private Singleton(){}
    public static synchronized Singleton getInstance(){
        return instance;
    }

}
