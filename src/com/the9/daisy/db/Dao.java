package com.the9.daisy.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.the9.daisy.common.exception.ServiceException;
import com.the9.daisy.db.config.DbConfig;

public class Dao {
	private static final Logger logger = LoggerFactory.getLogger(Dao.class);

	private JDBCTemplate jdbc;
	private static final Map<Class<?>, ClazzDesc> classDescMap = new HashMap<Class<?>, ClazzDesc>();

	public void init(DbConfig config) {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setJdbcUrl(config.getUrl());
		dataSource.setUser(config.getUserName());
		dataSource.setPassword(config.getPassword());
		try {
			dataSource.setDriverClass(config.getDriverClass());
		} catch (PropertyVetoException e) {
			throw new ServiceException("com.mysql.jdbc.Driver not found");
		}
		dataSource.setInitialPoolSize(config.getInitialPoolSize());
		dataSource.setMaxPoolSize(config.getMaxPoolSize());
		dataSource.setMinPoolSize(config.getMinPoolSize());
		dataSource.setAcquireIncrement(config.getAcquireIncrement());
		dataSource.setMaxIdleTime(config.getMaxIdleTime());
		dataSource.setMaxStatements(config.getMaxStatements());
		dataSource.setMaxStatementsPerConnection(config
				.getMaxStatementsPerConnection());
		dataSource.setPreferredTestQuery(config.getPreferredTestQuery());
		jdbc = new JDBCTemplate(dataSource);
		logger.info("init Dao finish");
	}

	private ClazzDesc getClassDesc(Class<?> clazz) {
		ClazzDesc ret = classDescMap.get(clazz);
		if (null != ret) {
			return ret;
		}
		ClazzDesc newDef = new ClazzDesc(clazz);
		classDescMap.put(clazz, newDef);
		return newDef;
	}

	private List<Object> handleResultSet(ResultSet rs, ClazzDesc cd)
			throws InstantiationException, IllegalAccessException,
			SQLException, IllegalArgumentException, IOException,
			InvocationTargetException {
		List<Object> ret = new ArrayList<Object>();
		while (rs.next()) {
			Object obj = cd.getClazz().newInstance();
			FieldDesc[] fdArray = cd.getFields();
			for (int i = 0; i < fdArray.length; i++) {
				FieldDesc fd = fdArray[i];
				if (null != fd) {
					Object value = rs.getObject(fd.getColumnName());
					fd.setValue(obj, value);
				}
			}
			ret.add(obj);
		}
		return ret;
	}

	private List<Map<String, Object>> handleResultSet(ResultSet rs,
			String[] columns) throws InstantiationException,
			IllegalAccessException, SQLException, IllegalArgumentException,
			IOException, InvocationTargetException {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < columns.length; i++) {
				String columnName = columns[i];
				map.put(columnName, rs.getObject(columnName));
			}
			ret.add(map);
		}
		return ret;
	}

	private String parseColumnArrayToString(String[] columns) {
		StringBuilder sb = new StringBuilder();
		for (String c : columns) {
			sb.append(c);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 连同id一并插入
	 * 
	 * @param obj
	 */
	public void insertNotGenerateId(final Object obj) {
		insert(obj, false);
	}

	/**
	 * 插入并自动生成一个id
	 * 
	 * @param obj
	 */
	public void insert(final Object obj) {
		insert(obj, true);
	}

	private void insert(final Object obj, final boolean generateId) {
		final ClazzDesc cd = getClassDesc(obj.getClass());
		String sql = null;
		if (generateId) {
			sql = cd.getSql_insertWithoutPk();
		} else {
			sql = cd.getSql_insertWithPk();
		}
		jdbc.execute(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				FieldDesc[] fds = cd.getFields();
				int count = 0;
				for (FieldDesc fd : fds) {
					if (!generateId || !fd.isPrimaryKey()) {
						Object value = fd.getValue(obj);
						pstmt.setObject(++count, value);
					}
				}
			}
		});
	}

	/**
	 * 删除指定id的记录
	 * 
	 * @param clazz
	 * @param id
	 */
	public void delete(Class<?> clazz, final Object id) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_deleteByPk();
		jdbc.execute(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				pstmt.setObject(1, id);
			}
		});
	}

	/**
	 * 根据条件删除记录
	 * 
	 * @param clazz
	 * @param where
	 */
	public void delete(Class<?> clazz, final String where) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_deleteBy__();
		sql = MessageFormat.format(sql, where);
		jdbc.execute(sql);
	}

	/**
	 * 更新一个对象
	 * 
	 * @param obj
	 */
	public void update(final Object obj) {
		final ClazzDesc cd = getClassDesc(obj.getClass());
		String sql = cd.getSql_updateById();
		jdbc.execute(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				FieldDesc[] fds = cd.getFields();
				int count = 0;
				for (FieldDesc fd : fds) {
					if (!fd.isPrimaryKey()) {
						Object value = fd.getValue(obj);
						pstmt.setObject(++count, value);
					}
				}
				pstmt.setObject(++count, cd.getPkField().getValue(obj));
			}
		});
	}

	/**
	 * 根据id，设置一些字段值
	 * 
	 * @param sets
	 * @param clazz
	 * @param id
	 */
	public void update(final Map<String, Object> sets, Class<?> clazz,
			final Object id) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_update__byPk();
		StringBuilder sb = new StringBuilder();
		final List<String> columnNames = new ArrayList<String>();
		for (String columnName : sets.keySet()) {
			Object obj = sets.get(columnName);
			if (obj != null) {
				sb.append(columnName).append(" = ");
				sb.append("?,");
				columnNames.add(columnName);
			}

		}
		sb.deleteCharAt(sb.length() - 1);
		sql = MessageFormat.format(sql, sb.toString());
		jdbc.execute(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				int size = columnNames.size();
				for (int i = 0; i < size; i++) {
					pstmt.setObject(i + 1, sets.get(columnNames.get(i)));
				}
				pstmt.setObject(size + 1, id);
			}
		});
	}

	/**
	 * 根据条件，设置一些字段值
	 * 
	 * @param sets
	 * @param clazz
	 * @param where
	 */
	public void update(final Map<String, Object> sets, Class<?> clazz,
			String where) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_update__by__();
		StringBuilder sb = new StringBuilder();
		final List<String> columnNames = new ArrayList<String>();
		for (String columnName : sets.keySet()) {
			Object obj = sets.get(columnName);
			if (obj != null) {
				sb.append(columnName).append(" = ");
				sb.append("?,");
				columnNames.add(columnName);
			}

		}
		sb.deleteCharAt(sb.length() - 1);
		sql = MessageFormat.format(sql, sb.toString(), where);
		jdbc.execute(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				int size = columnNames.size();
				for (int i = 0; i < size; i++) {
					pstmt.setObject(i + 1, sets.get(columnNames.get(i)));
				}
			}
		});
	}

	/**
	 * 根据id查找对象
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T select(Class<T> clazz, final Object id) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_selectByPk();
		Object ret = jdbc.query(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				pstmt.setObject(1, id);
			}
		}, new IHandler() {
			@Override
			public Object hanlder(ResultSet rs) throws SQLException {
				Object obj = null;
				try {
					List<Object> l = handleResultSet(rs, cd);
					if (l.size() > 0) {
						obj = l.get(0);
					}
				} catch (Exception e) {
					throw new ServiceException(e);
				}
				return obj;
			}
		});
		if (null != ret) {
			return (T) ret;
		}
		return null;
	}

	/**
	 * 根据id选择一些字段
	 * 
	 * @param clazz
	 * @param columns
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> select(Class<?> clazz, final String[] columns,
			final Object id) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = cd.getSql_select__byPk();
		sql = MessageFormat.format(sql, parseColumnArrayToString(columns));
		Object ret = jdbc.query(sql, new ISetter() {
			@Override
			public void setter(PreparedStatement pstmt) throws SQLException {
				pstmt.setObject(1, id);
			}
		}, new IHandler() {
			@Override
			public Object hanlder(ResultSet rs) throws SQLException {
				Object obj = null;
				try {
					List<Map<String, Object>> l = handleResultSet(rs, columns);
					if (l.size() > 0) {
						obj = l.get(0);
					}
				} catch (Exception e) {
					throw new ServiceException(e);
				}
				return obj;
			}
		});
		if (null != ret) {
			return (Map<String, Object>) ret;
		}
		return null;
	}

	/**
	 * 根据条件查找对象的某些属性
	 * 
	 * @param columns
	 * @param clazz
	 * @param where
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> select(final String[] columns,
			Class<?> clazz, String where) {
		final ClazzDesc cd = getClassDesc(clazz);
		String sql = MessageFormat.format(cd.getSql_select__by__(),
				parseColumnArrayToString(columns), where);
		Object ret = jdbc.query(sql, new IHandler() {
			@Override
			public Object hanlder(ResultSet rs) throws SQLException {
				Object obj = null;
				try {
					obj = handleResultSet(rs, columns);
				} catch (Exception e) {
					throw new ServiceException(e);
				}
				return obj;
			}
		});
		if (null != ret) {
			return (List<Map<String, Object>>) ret;
		}
		return null;
	}

}
