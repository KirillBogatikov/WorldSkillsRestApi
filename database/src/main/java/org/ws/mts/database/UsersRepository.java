package org.ws.mts.database;

import java.sql.SQLException;
import java.util.List;

import org.cuba.sql.Insert;
import org.cuba.sql.Update;
import org.cuba.sql.select.Select;
import org.ws.mts.models.DbUser;
import org.ws.mts.utils.PostgreSQL;

public class UsersRepository {
	private DatabaseContext database;
	
	public UsersRepository(DatabaseContext database) throws SQLException {
		this.database = database;
		database.execute("create table if not exists users(id text unique, name text, surname text, phone text, password text, primary key(id, phone))");
	}
	
	public boolean exists(String id) throws SQLException {
		Select select = new Select();
		select.column("id")
			  .from("users")
			  .where()
			      .column("id").equals().value(PostgreSQL.stringify(id));
		return database.fetchOne(select.build(), sql -> sql.getString("id")) != null;
	}
	
	public boolean check(String phone) throws SQLException {
		Select select = new Select();
		select.column("id")
			  .from("users")
			  .where()
			      .column("phone").equals().value(PostgreSQL.stringify(phone));
		return database.fetchOne(select.build(), sql -> sql.getString("id")) == null;
	}
	
	public void save(DbUser user) throws SQLException {
		if(check(user.getPhone())) {
			insert(user);
		} else {
			update(user);
		}
	}
	
	public String password(String phone) throws SQLException {
		Select select = new Select();
		select.column("password")
			  .from("users")
			  .where()
			      .column("phone").equals().column(PostgreSQL.stringify(phone));
		
		return database.fetchOne(select.build(), sql -> sql.getString("password"));
	}
	
	public String userId(String phone) throws SQLException {
		Select select = new Select();
		select.column("id")
			  .from("users")
			  .where()
			      .column("phone").equals().column(PostgreSQL.stringify(phone));
		
		return database.fetchOne(select.build(), sql -> sql.getString("id"));
	}
	
	private void insert(DbUser user) throws SQLException {
		Insert insert = new Insert();
		insert.into("users")
			  .set("id", PostgreSQL.stringify(user.getId()))
			  .set("name", PostgreSQL.stringify(user.getName()))
			  .set("surname", PostgreSQL.stringify(user.getSurname()))
			  .set("phone", PostgreSQL.stringify(user.getPhone()))
			  .set("password", PostgreSQL.stringify(user.getPassword()));
		database.execute(insert.build());
	}
	
	private void update(DbUser user) throws SQLException {
		Update update = new Update();
		update.table("users")
			  .set("name", PostgreSQL.stringify(user.getName()))
			  .set("surname", PostgreSQL.stringify(user.getSurname()))
			  .set("phone", PostgreSQL.stringify(user.getPhone()))
			  .set("password", PostgreSQL.stringify(user.getPassword()))
			  .where()
			  .column("id").equals().value(PostgreSQL.stringify(user.getId()));
		database.execute(update.build());
	}
	
	public List<DbUser> search(String nameRegex, String surnameRegex, String phoneRegex) throws SQLException {
		Select select = new Select();
		select.all()
			  .from("users")
			  .where()
			      .column("name").operation("~").value(PostgreSQL.stringify(nameRegex)).or()
			      .column("surname").operation("~").value(PostgreSQL.stringify(surnameRegex)).or()
			      .column("phone").operation("~").value(PostgreSQL.stringify(phoneRegex)).end();
		return database.fetchAll(select.build(), sql -> {
			DbUser user = new DbUser();
			user.setId(sql.getString("id"));
			user.setName(sql.getString("name"));
			user.setSurname(sql.getString("surname"));
			user.setPhone(sql.getString("phone"));
			return user;
		});
	}
}
