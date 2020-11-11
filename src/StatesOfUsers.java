import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class StatesOfUsers {
    private final HashMap<Long, User> states;
    private final String base;

    public StatesOfUsers(String baseAddress){
        states = new HashMap<>();
        base = baseAddress;
        File file = new File(base);
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

    public String getCurrentRequest(long id){
        return String.join(" ", getRequests(id));
    }

    public String getCategory(long id){
        return states.get(id).category;
    }

    public void setCategory(long id, String newCategory){
        states.get(id).category = newCategory;
        update();
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

    public void clearRequests(long id){
        User user = states.get(id);
        while (user.requests.size() > 1)
            user.requests.remove(0);
    }

    public void addRequest(long id, String str){
        states.get(id).requests.add(str);
        update();
    }

    public void removeRequest(long id, String str){
        User user = states.get(id);
        user.requests.remove(str);
        if (user.requests.size() == 0)
            user.category = null;
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
            File file = new File(base);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
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