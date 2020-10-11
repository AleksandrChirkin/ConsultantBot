import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StatesOfUsers {
    private final ArrayList<User> users;
    private final ArrayList<Long> ids;

    public StatesOfUsers(){
        users = new ArrayList<>();
        ids = new ArrayList<>();
        File file = new File("./src/statesOfUsers.json");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                StringReader stringReader = new StringReader(line);
                ObjectMapper mapper = new ObjectMapper();
                User user = mapper.readValue(stringReader, User.class);
                users.add(user);
                ids.add(user.id);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void add(long id){
        users.add(new User(id));
        ids.add(id);
        update();
    }

    public void updateRequests(long id, String request){
        if (!contains(id))
            throw new NoSuchElementException();
        for (User user: users){
            if (user.id == id && !user.requests.contains(request)) {
                user.requests.add(request);
                break;
            }
        }
        update();
    }

    public void clearRequestsHistory(long id){
        if (!contains(id))
            throw new NoSuchElementException();
        for (User user: users){
            if (user.id == id) {
                user.requests.clear();
                break;
            }
        }
        update();
    }

    public boolean contains(long id){
        return ids.contains(id);
    }

    public String getLastRequest(long id){
        if(!contains(id))
            throw new NoSuchElementException();
        for (User user: users)
            if (user.id == id)
                return user.requests.size() > 0
                        ? user.requests.get(user.requests.size()-1)
                        : "";
        return "";
    }

    public int getRequestsNumber(long id){
        if(!contains(id))
            throw new NoSuchElementException();
        for (User user: users)
            if (user.id == id)
                return user.requests.size();
        return 0;
    }

    private void update()
    {
        File file = new File("./src/statesOfUsers.json");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            for (User user : users) {
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