package com.the9.daisy.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IHandler {
	Object hanlder(ResultSet rs) throws SQLException;
}
