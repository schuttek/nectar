package org.nectarframework.base.service.sql;


/** Short hand for MysqlPreparedStatement
 * 
 * @author skander
 *
 */
public class PrSt extends SqlPreparedStatement {
	public PrSt() {
		super();
	}

	public PrSt(String sql) {
		super(sql);
	}

}
