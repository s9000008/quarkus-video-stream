package my.project.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class VttEntity extends PanacheEntity {
	
	public long fileId;
	
	public String local;
    
    public String path;
	
}
