package com.the9.daisy.db.config;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.the9.daisy.common.exception.ServiceException;
import com.the9.daisy.common.util.XmlUtil;

public class DbConfigManager {
	private static final Logger logger = LoggerFactory
			.getLogger(DbConfigManager.class);

	private final String configFilePath;

	public String getConfigFilePath() {
		return configFilePath;
	}

	public DbConfigManager(String configFilePath) {
		super();
		this.configFilePath = configFilePath;
	}

	private DbConfig config = new DbConfig();

	public DbConfig getConfig() {
		return config;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		File file = new File(configFilePath);
		logger.info("start to load config file path={}", file.getAbsolutePath());
		SAXReader reader = new SAXReader();
		Document doc;
		Element root = null;
		try {
			doc = reader.read(file);
			root = doc.getRootElement();
			Element db = XmlUtil.subElement(root, "db");
			String url = XmlUtil.attributeValueString(db, "url");
			config.setUrl(url);
			String user = XmlUtil.attributeValueString(db, "user");
			config.setUser(user);
			String password = XmlUtil.attributeValueString(db, "password");
			config.setPassword(password);
			String driverClass = XmlUtil
					.attributeValueString(db, "driverClass");
			config.setDriverClass(driverClass);
			if (db.element("property") != null) {
				Element propertyList = XmlUtil.subElement(db, "property");
				for (Element e : (List<Element>) propertyList.elements()) {
					String name = XmlUtil.attributeValueString(e, "name");
					if (name.equals("initialPoolSize")) {
						config.setInitialPoolSize(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("minPoolSize")) {
						config.setMinPoolSize(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("maxPoolSize")) {
						config.setMaxPoolSize(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("acqumaxPoolSizeireIncrement")) {
						config.setAcquireIncrement(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("maxIdleTime")) {
						config.setMaxIdleTime(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("maxStatements")) {
						config.setMaxStatements(XmlUtil.attributeValueInt(e,
								"value"));
						break;
					}
					if (name.equals("maxStatementsPerConnection")) {
						config.setMaxStatementsPerConnection(XmlUtil
								.attributeValueInt(e, "value"));
						break;
					}
					if (name.equals("preferredTestQuery")) {
						config.setPreferredTestQuery(XmlUtil
								.attributeValueString(e, "value"));
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceException("failed to load config file", e);
		}
		logger.info("successfully load config file path={}",
				file.getAbsoluteFile());
	}
}
