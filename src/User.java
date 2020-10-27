import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect
public class User {
    public long id;
    public List<String> requests;
    public Map<String, String> categories;
    public User(){}

    @JsonIgnore
    public User(long id){
        this.id = id;
        requests = new ArrayList<>();
        categories = new HashMap<>();
    }

    public void updateRequests(String newRequest){
        requests.add(newRequest);
    }

    public void setCategories(HashMap<String, String> newCategories){
        categories = newCategories;
    }
}