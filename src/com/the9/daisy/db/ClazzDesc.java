package com.the9.daisy.db;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.the9.daisy.common.exception.ServiceException;

public class ClazzDesc {
	private static final Logger logger = LoggerFactory
			.getLogger(ClazzDesc.class);
	private Class<?> clazz;
	/**
	 * 表名
	 */
	private String tableName;
	/**
	 * 主键
	 */
	private FieldDesc pkField;

	/**
	 * 所有字段
	 */
	private FieldDesc[] fields;

	private Map<String, FieldDesc> columnFieldMap = new HashMap<String, FieldDesc>();

	public Class<?> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public FieldDesc getPkField() {
		return pkField;
	}

	public FieldDesc[] getFields() {
		return fields;
	}

	public ClazzDesc(Class<?> clazz) {
		this.clazz = clazz;
		Table table = clazz.getAnnotation(Table.class);
		if (table == null) {
			throw new ServiceException("no table annotation in this class="
					+ clazz.getName());
		}
		this.tableName = table.name();
		if (tableName == null) {
			throw new ServiceException(
					"no name confiured in the annotation of class="
							+ clazz.getName());
		}
		Field[] allFields = null;
		Class<?> superClass = clazz.getSuperclass();
		Field[] selfFields = clazz.getDeclaredFields();
		if (!superClass.getName().equals(Object.class.getName())) {
			Field[] superFields = superClass.getDeclaredFields();
			allFields = ArrayUtils.addAll(superFields, selfFields);
		} else {
			allFields = selfFields;
		}
		fields = new FieldDesc[allFields.length];
		for (int i = 0; i < allFields.length; i++) {
			Field f = allFields[i];
			f.setAccessible(true);
			Column column = f.getAnnotation(Column.class);
			if (column == null) {
				logger.info("ingore field={} in class={}", f.getName(),
						clazz.getName());
			} else {
				FieldDesc fd = new FieldDesc(f);
				columnFieldMap.put(fd.getColumnName(), fd);
				fields[i] = fd;
				if (fd.isPrimaryKey()) {
					if (this.pkField != null) {
						throw new ServiceException(
								"duplicate primaryKey in className="
										+ clazz.getName() + ",found["
										+ pkField.getName() + ","
										+ fd.getName() + "]");
					}
					this.pkField = fd;
				}
			}
		}
		logger.info("init class=[{}] finish", clazz.getName());
		init();
	}

	public FieldDesc[] getFieldDescsByColumns(String[] columns) {
		FieldDesc[] ret = new FieldDesc[columns.length];
		for (int i = 0; i < columns.length; i++) {
			ret[i] = columnFieldMap.get(columns[i]);
		}
		return ret;
	}

	/**
	 * 不带id的插入
	 */
	private String sql_insertWithoutPk;
	/**
	 * 带id的插入
	 */
	private String sql_insertWithPk;
	/**
	 * 根据条件删除相应的记录
	 */
	private String sql_deleteBy__;
	/**
	 * 根据id去删除一条记录
	 */
	private String sql_deleteByPk;
	/**
	 * 根据条件去选择相应的字段
	 */
	private String sql_select__by__;
	/**
	 * 根据id去选择相应的字段
	 */
	private String sql_select__byPk;
	/**
	 * 根据id去选择一条记录
	 */
	private String sql_selectByPk;
	/**
	 * 根据条件去更新某些字段
	 */
	private String sql_update__by__;
	/**
	 * 根据id去更新某些字段
	 */
	private String sql_update__byPk;
	/**
	 * 根据id去更新所有字段
	 */
	private String sql_updateById;

	public static final String comma = ",";
	public static final String questionMark = "?";
	public static final String equalsMark = "=";

	public void init() {
		StringBuilder columnsStr = new StringBuilder();
		StringBuilder columnsStrWithoutPk = new StringBuilder();
		StringBuilder insertValuesStr = new StringBuilder();
		StringBuilder insertValuesStrWithoutPk = new StringBuilder();
		StringBuilder updateSetStrWithoutPk = new StringBuilder();
		for (FieldDesc fd : fields) {
			columnsStr.append(fd.getColumnName()).append(comma);
			insertValuesStr.append(questionMark).append(comma);
			if (!fd.isPrimaryKey()) {
				columnsStrWithoutPk.append(fd.getColumnName()).append(comma);
				insertValuesStrWithoutPk.append(questionMark).append(comma);
				updateSetStrWithoutPk.append(fd.getColumnName())
						.append(equalsMark).append(questionMark).append(comma);
			}
		}
		columnsStr.deleteCharAt(columnsStr.length() - 1);
		columnsStrWithoutPk.deleteCharAt(columnsStrWithoutPk.length() - 1);
		insertValuesStr.deleteCharAt(insertValuesStr.length() - 1);
		insertValuesStrWithoutPk
				.deleteCharAt(insertValuesStrWithoutPk.length() - 1);
		updateSetStrWithoutPk.deleteCharAt(updateSetStrWithoutPk.length() - 1);
		String insertTemplate = "insert into {0}({1}) values ({2})";
		sql_insertWithoutPk = MessageFormat.format(insertTemplate, tableName,
				columnsStrWithoutPk, insertValuesStrWithoutPk);
		logger.info("[{}] sql_insertWithoutPk=[{}]", tableName,
				sql_insertWithoutPk);
		sql_insertWithPk = MessageFormat.format(insertTemplate, tableName,
				columnsStr, insertValuesStr);
		logger.info("[{}] sql_insertWithPk=[{}]", tableName, sql_insertWithPk);
		String deleteTemplate = "delete from " + tableName + " where {0}";
		sql_deleteBy__ = deleteTemplate;
		logger.info("[{}] sql_deleteBy__=[{}]", tableName, sql_deleteBy__);
		String pkWhere = new StringBuilder(pkField.getColumnName())
				.append(equalsMark).append(questionMark).toString();
		sql_deleteByPk = MessageFormat.format(deleteTemplate, pkWhere);
		logger.info("[{}] sql_deleteByPk=[{}]", tableName, sql_deleteByPk);
		String selectTemplate = "select {0} from " + tableName + " where {1}";
		sql_select__by__ = selectTemplate;
		logger.info("[{}] sql_select__by__=[{}]", tableName, sql_select__by__);
		sql_select__byPk = MessageFormat.format(selectTemplate, "{0}", pkWhere);
		logger.info("[{}] sql_select__byPk=[{}]", tableName, sql_select__byPk);
		sql_selectByPk = MessageFormat.format(selectTemplate, columnsStr,
				pkWhere);
		logger.info("[{}] sql_selectByPk=[{}]", tableName, sql_selectByPk);
		String updateTemplate = "update " + tableName + " set {0} where {1}";
		sql_update__by__ = updateTemplate;
		logger.info("[{}] sql_update__by__=[{}]", tableName, sql_update__by__);
		sql_update__byPk = MessageFormat.format(updateTemplate, "{0}", pkWhere);
		logger.info("[{}] sql_update__byPk=[{}]", tableName, sql_update__byPk);
		sql_updateById = MessageFormat.format(updateTemplate,
				updateSetStrWithoutPk, pkWhere);
		logger.info("[{}] sql_updateById=[{}]", tableName, sql_updateById);
		logger.info("init sql finish for class=[{}]", clazz.getName());
	}

	public String getSql_insertWithoutPk() {
		return sql_insertWithoutPk;
	}

	public String getSql_insertWithPk() {
		return sql_insertWithPk;
	}

	public String getSql_deleteBy__() {
		return sql_deleteBy__;
	}

	public String getSql_deleteByPk() {
		return sql_deleteByPk;
	}

	public String getSql_select__by__() {
		return sql_select__by__;
	}

	public String getSql_select__byPk() {
		return sql_select__byPk;
	}

	public String getSql_selectByPk() {
		return sql_selectByPk;
	}

	public String getSql_update__by__() {
		return sql_update__by__;
	}

	public String getSql_update__byPk() {
		return sql_update__byPk;
	}

	public String getSql_updateById() {
		return sql_updateById;
	}
}
