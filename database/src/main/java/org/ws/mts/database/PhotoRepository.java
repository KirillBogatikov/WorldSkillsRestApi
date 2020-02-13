package org.ws.mts.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cuba.sql.Delete;
import org.cuba.sql.Insert;
import org.cuba.sql.Update;
import org.cuba.sql.select.Select;
import org.ws.mts.models.DbPhoto;
import org.ws.mts.utils.PostgreSQL;

public class PhotoRepository {
	private DatabaseContext database;
	
	public PhotoRepository(DatabaseContext database) throws SQLException {
		this.database = database;
		database.execute("create table if not exists photos(id text, name text, file text, owner text, "
				+ "primary key(id), "
				+ "foreign key(owner) references users(id) on delete cascade)");
		database.execute("create table if not exists sharing(photo text, userId text, "
				+ "foreign key(photo) references photos(id) on delete cascade, "
				+ "foreign key(userId) references users(id) on delete cascade)");
	}
	
	public void add(DbPhoto photo) throws SQLException {
		Insert insert = new Insert();
		insert.into("photos") 
			  .set("id", PostgreSQL.stringify(photo.getId()))
			  .set("name", PostgreSQL.stringify(photo.getName()))
			  .set("file", PostgreSQL.stringify(photo.getFileName()))
			  .set("owner", PostgreSQL.stringify(photo.getOwner()));
		database.execute(insert.build());
	}
	
	public void update(DbPhoto photo) throws SQLException {
		Update update = new Update();
		update.table("photos") 
			  .set("name", PostgreSQL.stringify(photo.getName()));
		if(photo.getFileName() != null) {
			update.set("file", PostgreSQL.stringify(photo.getFileName()));
		}
		update.where()
			  .column("id").equals().value(PostgreSQL.stringify(photo.getId()));
		database.execute(update.build());
	}
	
	public DbPhoto byId(String id) throws SQLException {
		Select select = new Select();
		select.all()
			  .from("photos")
			  .where()
			      .column("id").equals().value(PostgreSQL.stringify(id));
		return database.fetchOne(select.build(), this::map);
	}
	
	public String fileName(String id) throws SQLException {
		Select select = new Select();
		select.column("file")
			  .from("photos")
			  .where()
			      .column("id").equals().value(PostgreSQL.stringify(id));
		return database.fetchOne(select.build(), sql -> sql.getString("file"));
	}
	
	public List<String> users(String id) throws SQLException {
		Select select = new Select();
		select.all()
			  .from("sharing")
			  .where()
			      .column("photo")
			      .equals()
			      .value(PostgreSQL.stringify(id));
		return database.fetchAll(select.build(), sql -> sql.getString("userId"));
	}
	
	public List<DbPhoto> all(String user) throws SQLException {
		Select selectOwn = new Select();
		selectOwn.all()
			     .from("photos")
			         .where()
			             .column("owner")
			             .equals()
			             .value(PostgreSQL.stringify(user));
		List<DbPhoto> own = database.fetchAll(selectOwn.build(), this::map);
		
		Select temp = new Select();
		temp.column("photo")
			.from("sharing")
			.where()
			    .column("user")
			    .equals()
			    .value(PostgreSQL.stringify(user));
		
		Select selectShared = new Select();
		selectShared.all()
			        .from("photos")
			        .where()
			            .column("id")
			            .in(temp.build());
		List<DbPhoto> shared = database.fetchAll(selectShared.build(), this::map);
		own.addAll(shared);
		return own;
	}
	
	public void share(String photo, String user) throws SQLException {
		Insert insert = new Insert();
		insert.into("sharing")
			  .set("photo", PostgreSQL.stringify(photo))
			  .set("userId", PostgreSQL.stringify(user));
		database.execute(insert.build());
	}
	
	public boolean isShared(String photo, String user) throws SQLException {
		Select select = new Select();
		select.column("userId")
			  .from("sharing")
			  .where()
			      .column("userId").equals().value(PostgreSQL.stringify(user)).and()
			      .column("photo").equals().value(PostgreSQL.stringify(photo));
		return database.fetchOne(select.build(), sql -> "") != null;
	}
	
	public boolean isOwner(String photo, String user) throws SQLException {
		Select select = new Select();
		select.column("owner")
			  .from("photos")
			  .where()
			      .column("owner").equals().value(PostgreSQL.stringify(user)).and()
			      .column("id").equals().value(PostgreSQL.stringify(photo));
		return database.fetchOne(select.build(), sql -> "") != null;
	}
	
	public void delete(String id) throws SQLException {
		Delete delete = new Delete();
		delete.from("photos")
			  .where()
			      .column("id").equals().column(PostgreSQL.stringify(id));		
		database.execute(delete.build());
	}
	
	private DbPhoto map(ResultSet sql) throws SQLException {
		DbPhoto photo = new	DbPhoto();
		
		photo.setId(sql.getString("id"));
		photo.setName(sql.getString("name"));
		photo.setOwner(sql.getString("owner"));
		photo.setFileName(sql.getString("file"));
		
		return photo;
	}
}
