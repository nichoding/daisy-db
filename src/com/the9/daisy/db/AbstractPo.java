package com.the9.daisy.db;

import java.util.Date;

public abstract class AbstractPo {
	@Column(isPrimaryKey = true, column = "id")
	protected int id;
	@Column(column = "create_time")
	protected Date createTime;
	@Column(column = "last_modify_time")
	protected Date lastModifyTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Date lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

}
