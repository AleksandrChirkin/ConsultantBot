import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class StatesOfUsers {
    private final HashMap<Long, User> states;

    public StatesOfUsers(){
        states = new HashMap<>();
        File file = new File("./src/statesOfUsers.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                StringReader stringReader = new StringReader(line);
                ObjectMapper mapper = new ObjectMapper();
                User user = mapper.readValue(stringReader, User.class);
                states.put(user.id, user);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public boolean containsKey(long id) {
        return states.containsKey(id);
    }

    public void put(long id){
        states.put(id, new User(id));
        update();
    }

    public String getLastRequest(long id){
        List<String> requests = states.get(id).requests;
        return requests.get(requests.size()-1);
    }

    public List<String> getRequests(long id){
        return states.get(id).requests;
    }

    public void addRequest(long id, String str){
        states.get(id).updateRequests(str);
        update();
    }

    public void update()
    {
        try {
            File file = new File("./src/statesOfUsers.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            for (User user : states.values())
            {
                StringWriter stringWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(stringWriter, user);
                writer.write(stringWriter.toString());
            }
            writer.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}