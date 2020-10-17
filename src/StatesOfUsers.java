import java.io.*;
import java.util.HashMap;

public class StatesOfUsers {
    private final HashMap<Long, String> states;

    public StatesOfUsers(){
        states = new HashMap<>();
        File file = new File("./src/statesOfUsers.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                states.put(Long.valueOf(line.substring(0, line.indexOf("—"))),
                        line.substring(line.indexOf("—") + 1));
                line = reader.readLine();
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public boolean containsKey(long id) {
        return states.containsKey(id);
    }

    public String get(long id){
        return states.get(id);
    }

    public void put(long id, String str){
        states.put(id, str);
        update();
    }

    public void replace(long id, String str){
        states.replace(id, str);
        update();
    }

    public void update()
    {
        try {
            File file = new File("./src/statesOfUsers.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            for (long id : states.keySet())
                writer.write(String.format("%d—%s\n", id, states.get(id)));
            writer.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}