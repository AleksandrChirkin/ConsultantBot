import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class StatesOfUsers {
    private final HashMap<Long, User> states;
    private final File base;

    public StatesOfUsers(String baseAddress){
        states = new HashMap<>();
        base = new File(baseAddress);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(base));
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

    public String getCurrentRequest(long id){
        return String.join(" ", getRequests(id));
    }

    public void updateCategoriesLinks(long id, HashMap<String, String> newCategories){
        states.get(id).categoriesLinks = newCategories;
        setItemsFound(id, !newCategories.isEmpty());
        update();
    }

    public Map<String, String> getCategoriesLinks(long id){
        return states.get(id).categoriesLinks;
    }

    public void clearCategoriesLinks(long id){
        states.get(id).categoriesLinks.clear();
        update();
    }

    public List<String> getRequests(long id){
        return states.get(id).requests;
    }

    public void clearRequests(long id, boolean deleteAll){
        User user = states.get(id);
        while (user.requests.size() > 1)
            user.requests.remove(0);
        if (deleteAll)
            user.requests.remove(0);
        update();
    }

    public void addRequest(long id, String request){
        if (!request.contains("/catalog")) {
            states.get(id).requests.add(request);
            update();
        }
    }

    public void removeRequest(long id, String request){
        states.get(id).requests.remove(request);
        update();
    }

    public boolean getItemsFound(long id){
        return states.get(id).areItemsFound;
    }

    public void setItemsFound(long id, boolean itemsFound){
        states.get(id).areItemsFound = itemsFound;
        update();
    }

    private void update()
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(base, false));
            for (User user : states.values())
            {
                StringWriter stringWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(stringWriter, user);
                writer.write(stringWriter.toString());
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}