package com.the9.daisy.db;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.the9.daisy.common.exception.ServiceException;

public class FieldDesc {
	/**
	 * field对象
	 */
	private Field field;
	/**
	 * 成员变量名
	 */
	private String fieldName;
	/**
	 * 数据库列名
	 */
	private String columnName;
	/**
	 * 是否为主键
	 */
	private boolean isPrimaryKey;
	/**
	 * 字段类型
	 */
	private Class<?> clazz;

	public FieldDesc(Field field) {
		this.field = field;
		this.fieldName = field.getName();
		this.clazz = field.getType();
		Column column = field.getAnnotation(Column.class);
		this.columnName = column.column();
		this.isPrimaryKey = column.isPrimaryKey();
		if (null == columnName || columnName.isEmpty()) {
			throw new ServiceException(
					"name must be configed to column annotation,fieldName="
							+ field.getName());
		}
	}

	public Object getValue(final Object obj) {
		Object metaData = null;
		try {
			metaData = field.get(obj);
		} catch (IllegalAccessException e) {
			throw new ServiceException("error exists getValue className="
					+ obj.getClass().getName() + ",field=" + field.getName()
					+ ",obj=" + obj);
		}
		return metaData;
	}

	public void setValue(final Object obj, final Object value)
			throws IOException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (value == null) {
			return;
		}
		field.set(obj, value);
	}

	public Field getField() {
		return field;
	}

	public String getName() {
		return fieldName;
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Class<?> getClazz() {
		return clazz;
	}

}
