package com.the9.daisy.db.test;

import java.util.HashMap;
import java.util.Map;

import com.the9.daisy.db.AbstractPo;
import com.the9.daisy.db.Column;
import com.the9.daisy.db.Dao;
import com.the9.daisy.db.Table;
import com.the9.daisy.db.config.DbConfig;

@Table(name = "test_po")
public class TestPo extends AbstractPo {
	@Column(column = "uid")
	private int uid;
	@Column(column = "name")
	private String name;
	@Column(column = "xp")
	private long xp;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getXp() {
		return xp;
	}

	public void setXp(long xp) {
		this.xp = xp;
	}

	public static void main(String[] args) {
		DbConfig config = new DbConfig();
		config.setUrl("jdbc:mysql://localhost:3306/daisy_test_db?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf-8");
		config.setUser("root");
		config.setMaxIdleTime(2000);
		config.setMaxPoolSize(20);
		config.setPreferredTestQuery("select 1");
		config.setInitialPoolSize(1);
		Dao dt = new Dao(config);
		// final TestPo po = new TestPo();
		// po.setId(122);
		// po.setUid(2);
		// po.setName("nicho1");
		// po.setXp(200000);
		// po.setCreateTime(new Date());
		// po.setLastModifyTime(new Date());
		// dt.insertNotGenerateId(po);
		// TestPo p = dt.select(TestPo.class, 5);
		// p.setName("haha");
		// dt.update(p);
		Map<String, Object> sets = new HashMap<String, Object>();
		sets.put("name", "hehehe");
		sets.put("uid", 1234);
		dt.update(sets, TestPo.class, "id=5");
		System.out.println("finish");
	}
}
