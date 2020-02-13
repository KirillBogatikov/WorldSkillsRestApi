package org.ws.mts.utils;

import java.util.ArrayList;
import java.util.List;

import org.ws.mts.models.Credentials;
import org.ws.mts.models.Photo;
import org.ws.mts.models.SearchQuery;
import org.ws.mts.models.User;
import org.ws.mts.models.WebCredentials;
import org.ws.mts.models.WebPhoto;
import org.ws.mts.models.WebUpdatePhoto;
import org.ws.mts.models.WebUser;

public class Mapper {
	public static User from(WebUser webUser) {
		User user = new User();
		
		user.setName(webUser.getName());
		user.setSurname(webUser.getSurname());
		user.setPhone(webUser.getPhone());
		user.setPassword(webUser.getPassword());
		
		return user;
	}
	
	public static WebUser from(User user) {
		WebUser webUser = new WebUser();
		
		webUser.setName(user.getName());
		webUser.setSurname(user.getSurname());
		webUser.setPhone(user.getPhone());
		webUser.setPassword(user.getPassword());
		
		return webUser;
	}
	
	public static Credentials from(WebCredentials webCredentials) {
		Credentials credentials = new Credentials();
		
		credentials.setPhone(webCredentials.getPhone());
		credentials.setPassword(webCredentials.getPassword());
		
		return credentials;
	}
	
	public static SearchQuery of(String name, String surname, String phone) {
		SearchQuery query = new SearchQuery();
		
		query.setNamePart(name);
		query.setSurnamePart(surname);
		query.setPhonePart(phone);
		
		return query;
	}
	
	public static List<WebUser> usersFrom(List<User> users) {
		ArrayList<WebUser> webUsers = new ArrayList<>();
		
		for(User user : users) {
			webUsers.add(from(user));
		}
		
		return webUsers;
	}
	
	public static List<WebPhoto> photosFrom(List<Photo> photos) {
		ArrayList<WebPhoto> webPhotos = new ArrayList<>();
		
		for(Photo photo : photos) {
			webPhotos.add(from(photo));
		}
		
		return webPhotos;
	}
	
	public static WebPhoto from(Photo photo) {
		WebPhoto webPhoto = new WebPhoto();
		
		webPhoto.setId(photo.getId());
		webPhoto.setLink(photo.getLink());
		webPhoto.setName(photo.getName());
		webPhoto.setOwner(photo.getOwner());
		webPhoto.setTrusted(photo.getUsers());
		
		return webPhoto;
	}
	
	public static WebUpdatePhoto from(String name, String method) {
		WebUpdatePhoto webPhoto = new WebUpdatePhoto();
		
		webPhoto.setName(name);
		webPhoto.setMethod(method);
		
		return webPhoto;
	}
}
