import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

@JsonAutoDetect
public class User {
    public long id;
    public List<String> requests;
    public Map<String, String> categoriesLinks;
    public boolean areItemsFound;
    public User(){}

    @JsonIgnore
    public User(long userId){
        id = userId;
        requests = new ArrayList<>();
        categoriesLinks = new HashMap<>();
    }
}