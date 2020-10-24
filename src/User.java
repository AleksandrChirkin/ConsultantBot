import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect
public class User {
    public long id;
    public List<String> requests;
    public User(){}

    @JsonIgnore
    public User(long id){
        this.id = id;
        requests = new ArrayList<>();
    }

    public void updateRequests(String newRequest){
        requests.add(newRequest);
    }
}