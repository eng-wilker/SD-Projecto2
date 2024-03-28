package tukano.impl.java.servers.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Following{

	@Id 
	String follower;
	
	@Id 
	String followee;

	Following() {}

	public Following(String follower, String followee) {
		super();
		this.follower = follower;
		this.followee = followee;
	}

	public String getFollower() {
		return follower;
	}

	public void setFollower(String follower) {
		this.follower = follower;
	}

	public String getFollowee() {
		return followee;
	}

	public void setFollowee(String followee) {
		this.followee = followee;
	}

}