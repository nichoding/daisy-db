package com.the9.daisy.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ISetter {
	void setter(PreparedStatement pstmt) throws SQLException;
}
