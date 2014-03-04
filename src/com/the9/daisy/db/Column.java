package com.the9.daisy.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * db列
 * 
 * @author dingshengheng
 * 
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * 对应数据库中的列名
	 * 
	 * @return
	 */
	String column() default "";

	/**
	 * 是否为主键，默认为否
	 * 
	 * @return
	 */
	boolean isPrimaryKey() default false;

}
