package org.ws.mts.database;

import java.sql.SQLException;
import java.util.Date;

import org.cuba.sql.Delete;
import org.cuba.sql.Insert;
import org.cuba.sql.select.Select;
import org.ws.mts.models.DbToken;
import org.ws.mts.utils.PostgreSQL;

public class TokensRepository {
	private DatabaseContext database;
	
	public TokensRepository(DatabaseContext database) throws SQLException {
		this.database = database;
		database.execute("create table if not exists tokens(userId text, token text, die_date timestamp with time zone, "
				+ "primary key(token), "
				+ "foreign key(userId) references users(id) on delete cascade)");
	}
	
	public String userId(String token) throws SQLException {
		Select select = new Select();
		select.column("userId")
			  .from("tokens")
			  .where()
			      .column("token").equals().value(PostgreSQL.stringify(token));
		
		return database.fetchOne(select.build(), sql -> sql.getString("userId"));
	}
	
	public void save(DbToken token) throws SQLException {
		Insert insert = new Insert();
		insert.into("tokens")
			  .set("userId", PostgreSQL.stringify(token.getUser()))
			  .set("token", PostgreSQL.stringify(token.getToken()))
			  .set("die_date", PostgreSQL.stringify(token.getDieTime()));
		
		database.execute(insert.build());
	}
	
	public Date dieDate(String token) throws SQLException {
		Select select = new Select();
		select.column("die_date")
			  .from("tokens")
			  .where()
			      .column("token").equals().value(PostgreSQL.stringify(token));
		
		return database.fetchOne(select.build(), sql -> sql.getDate("die_date"));
	}
	
	public void delete(String token) throws SQLException {
		Delete delete = new Delete();
		delete.from("tokens")
			  .where()
			      .column("token").equals().value(PostgreSQL.stringify(token));
		
		database.execute(delete.build());
	}
}
