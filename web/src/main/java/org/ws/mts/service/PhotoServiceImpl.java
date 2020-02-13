package org.ws.mts.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cuba.io.TypeDetector;
import org.cuba.io.utils.FileUtils;
import org.ws.mts.database.DatabaseContext;
import org.ws.mts.database.PhotoRepository;
import org.ws.mts.models.DbPhoto;
import org.ws.mts.models.Photo;
import org.ws.mts.models.Response;
import org.ws.mts.models.Status;
import org.ws.mts.utils.Generator;

public class PhotoServiceImpl implements PhotoService {
	private PhotoRepository repo;
	private File photoContainer;
	private TypeDetector typeDetector;

	public PhotoServiceImpl(DatabaseContext database, String path) throws SQLException {
		this.repo = new PhotoRepository(database);
		this.photoContainer = new File(path);
		if(!photoContainer.exists()) {
			photoContainer.mkdirs();
		}
		this.typeDetector =  new TypeDetector();
	}
	
	@Override
	public Response upload(byte[] imageContent, String user) throws Exception {
		String id = Generator.generate(16);
		
		String type = typeDetector.mimeType(imageContent);		
		if(!type.equals("image/jpeg") && !type.equals("image/png")) {
			return new Response(null, Status.INVALID);
		}
		
		DbPhoto dbPhoto = new DbPhoto();
		dbPhoto.setId(id);
		dbPhoto.setName("Untitled");
		dbPhoto.setOwner(user);
		dbPhoto.setFileName(id + "." + typeDetector.extension(imageContent));
		
		try(OutputStream os = new FileOutputStream(new File(photoContainer, dbPhoto.getFileName()));
			BufferedOutputStream bos = new BufferedOutputStream(os)) {
			bos.write(imageContent);
		}
		
		repo.add(dbPhoto);
		
		Photo photo = new Photo();
		photo.setId(id);
	    photo.setName(dbPhoto.getName());
	    photo.setOwner(user);
	    photo.setLink(dbPhoto.getFileName());
	    photo.setUsers(Collections.emptyList());
		
		return new Response(photo, Status.OK);
	}
	
	public static class UpdatePhotoItem {
		public String photo = "Поддерживается только два типа изображений: JPEG и PNG";
	}

	@Override
	public Response update(String id, byte[] imageContent, String name) throws Exception {
		Photo photo = new Photo();
		
		if(name != null) {
			if(!name.matches("[а-яА-Яa-zA-Z0-9_~!@$%+\\-]+")) {
				photo.setName("Название фото может состоять из букв русского и латинского алфавитов, цифр и может содержать следующие символы: _+-~!@$%");
				return new Response(photo, Status.INVALID);
			} else if(name.length() < 4 || name.length() > 32) {
				photo.setName("Название фото должно содержать от 4 до 32 символов");
				return new Response(photo, Status.INVALID);
			}
		}
		
		DbPhoto dbPhoto = new DbPhoto();
		dbPhoto.setId(id);
		dbPhoto.setName(name == null ? "Untitled" : name);
		
		if(imageContent != null) {
			dbPhoto.setFileName(id + "." + typeDetector.extension(imageContent));
			String type = typeDetector.mimeType(imageContent);		
			if(!type.equals("image/jpeg") && !type.equals("image/png")) {
				return new Response(new UpdatePhotoItem(), Status.INVALID);
			}
					
			try(OutputStream os = new FileOutputStream(new File(photoContainer, dbPhoto.getFileName()));
				BufferedOutputStream bos = new BufferedOutputStream(os)) {
				bos.write(imageContent);
			}
		}
		
		repo.update(dbPhoto);	
		dbPhoto = repo.byId(id);
		
		photo.setId(id);
		photo.setName(dbPhoto.getName());
		photo.setLink(dbPhoto.getFileName());
		
		return new Response(photo, Status.OK);
	}

	@Override
	public boolean checkAccess(String user, String photo) throws Exception {
		return repo.isOwner(photo, user);
	}

	@Override
	public List<Photo> list(String user) throws Exception {
		List<DbPhoto> dbPhotos = repo.all(user);
		ArrayList<Photo> photos = new ArrayList<>();
		
		for(DbPhoto dbPhoto : dbPhotos) {
			Photo photo = new Photo();
			
			photo.setId(dbPhoto.getId());
			photo.setLink(dbPhoto.getFileName());
			photo.setName(dbPhoto.getName());
			photo.setOwner(dbPhoto.getOwner());
			photo.setUsers(repo.users(dbPhoto.getId()));
			
			photos.add(photo);
		}
		
		return photos;
	}

	@Override
	public Photo get(String id) throws Exception {
		DbPhoto dbPhoto = repo.byId(id);
		
		Photo photo = new Photo();		
		photo.setId(id);
		photo.setLink(dbPhoto.getFileName());
		photo.setName(dbPhoto.getName());
		photo.setOwner(dbPhoto.getOwner());
		photo.setUsers(repo.users(id));
		
		return photo;
	}

	@Override
	public void delete(String photo) throws Exception {
		File file = new File(photoContainer, repo.fileName(photo));
		file.delete();
		repo.delete(photo);
	}

	@Override
	public List<String> share(List<String> photos, String user) throws Exception {
		ArrayList<String> alreadyShared = new ArrayList<>();
		
		for(String photo : photos) {
			if(repo.isShared(photo, user)) {
				alreadyShared.add(photo);
			} else {
				repo.share(photo, user);
			}
		}
		
		return alreadyShared;
	}

	@Override
	public byte[] content(String id) throws Exception {
		File file = new File(photoContainer, repo.fileName(id));
		return FileUtils.readAllBytes(file, 2048);
	}

}
