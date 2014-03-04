package com.the9.daisy.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.the9.daisy.common.exception.ServiceException;

public class JDBCTemplate {
	private static final Logger logger = LoggerFactory
			.getLogger(JDBCTemplate.class);
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public JDBCTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			logger.error("error in get connection", e);
			throw new ServiceException(e);
		}
	}

	/**
	 * 关闭所有资源
	 * 
	 * @param conn
	 * @param stmt
	 * @param rs
	 */
	public void close(Connection conn, Statement stmt, ResultSet rs) {
		if (null != rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error("can not close ResultSet", e);
				throw new ServiceException(e);
			}
		}
		if (null != stmt) {
			try {
				stmt.close();
			} catch (SQLException e) {
				logger.error("can not close Statement", e);
				throw new ServiceException(e);
			}
		}
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("can not close Connection", e);
				throw new ServiceException(e);
			}
		}
	}

	/**
	 * 无参查询
	 * 
	 * @param sql
	 * @param handler
	 * @return
	 */
	public Object query(String sql, IHandler handler) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			if (null != conn) {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs != handler) {
					return handler.hanlder(rs);
				}
			}
		} catch (Exception e) {
			logger.error("error in query", e);
			throw new ServiceException(e);
		} finally {
			close(conn, stmt, rs);
		}
		return null;
	}

	/**
	 * 有参查询
	 * 
	 * @param sql
	 * @param handler
	 * @return
	 */
	public Object query(String sql, ISetter setter, IHandler handler) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			if (null != conn) {
				pstmt = conn.prepareStatement(sql);
				if (null != setter) {
					setter.setter(pstmt);
				}
				rs = pstmt.executeQuery();
				if (rs != handler) {
					return handler.hanlder(rs);
				}
			}
		} catch (Exception e) {
			logger.error("error in query", e);
			throw new ServiceException(e);
		} finally {
			close(conn, pstmt, rs);
		}
		return null;
	}

	/**
	 * 执行sql
	 * 
	 * @param sql
	 * @return
	 */
	public void execute(String sql) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			if (null != conn) {
				stmt = conn.createStatement();
				stmt.execute(sql);
			}
		} catch (Exception e) {
			logger.error("error in execute", e);
			throw new ServiceException(e);
		} finally {
			close(conn, stmt, null);
		}
	}

	/**
	 * 设置参数，并执行sql
	 * 
	 * @param sql
	 * @return
	 */
	public void execute(String sql, ISetter setter) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			if (null != conn) {
				pstmt = conn.prepareStatement(sql,
						PreparedStatement.RETURN_GENERATED_KEYS);
				if (null != setter) {
					setter.setter(pstmt);
				}
				pstmt.execute();
			}
		} catch (Exception e) {
			logger.error("error in execute", e);
			throw new ServiceException(e);
		} finally {
			close(conn, pstmt, null);
		}
	}

	/**
	 * 执行存储过程
	 * 
	 * @param sql
	 * @return
	 */
	public void executeProcedure(String procedure, Object[] params) {
		Connection conn = null;
		CallableStatement cstmt = null;
		try {
			conn = getConnection();
			int paramLength = params.length;
			StringBuilder sb = new StringBuilder();
			sb.append("call ").append(procedure).append("(");
			for (int i = 0; i < paramLength; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append("?");
			}
			sb.append(")");
			cstmt = conn.prepareCall(sb.toString());
			for (int i = 0; i < paramLength; i++) {
				cstmt.setObject(i + 1, params[i]);
			}
			cstmt.execute();
		} catch (Exception e) {
			logger.error("error in executeProcedure", e);
			throw new ServiceException(e);
		} finally {
			close(conn, cstmt, null);
		}
	}
}
