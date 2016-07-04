package org.nectarframework.base.service.mysql;


/** Short hand for MysqlPreparedStatement
 * 
 * @author skander
 *
 */
public class PrSt extends MysqlPreparedStatement {
	public PrSt() {
		super();
	}

	public PrSt(String sql) {
		super(sql);
	}

}
