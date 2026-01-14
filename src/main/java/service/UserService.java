package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class UserService {
    private static final String DATA_FILE = "portfolio_data.json";
    private static UserService instance;

    private Map<String, User> users;
    private User currentUser;
    private ObjectMapper objectMapper;

    private UserService() {
        users = new HashMap<>();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        loadData();
        if (users.isEmpty()) {
            users.put("admin@demo.com", new User("Admin User", "admin@demo.com", "123"));
            users.put("john@example.com", new User("John Doe", "john@example.com", "password123"));
            saveData();
        }
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    public static boolean loginUser(String email, String password) {
        User user = getInstance().users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            getInstance().currentUser = user;
            return true;
        }
        return false;
    }

    public static boolean registerUser(String name, String email, String password) {
        if (getInstance().users.containsKey(email)) {
            return false;
        }

        User newUser = new User(name, email, password);
        getInstance().users.put(email, newUser);
        getInstance().currentUser = newUser;
        getInstance().saveData();
        return true;
    }

    public static User getCurrentUser() {
        return getInstance().currentUser;
    }

    public static void logout() {
        getInstance().saveData(); // Save before logout
        getInstance().currentUser = null;
    }
    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                UserData data = objectMapper.readValue(file, UserData.class);
                if (data != null && data.getUsers() != null) {
                    this.users = data.getUsers();
                }
                System.out.println("Loaded " + users.size() + " users from " + DATA_FILE);
            } catch (IOException e) {
                System.err.println("Failed to load data: " + e.getMessage());
            }
        }
    }

    public void saveData() {
        try {
            UserData data = new UserData();
            data.setUsers(users);
            objectMapper.writeValue(new File(DATA_FILE), data);
            System.out.println("Saved " + users.size() + " users to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    public static void save() {
        getInstance().saveData();
    }

    public static String getCurrentUserName() {
        return getInstance().currentUser != null ? getInstance().currentUser.getName() : "Guest";
    }

    public static String getCurrentUserEmail() {
        return getInstance().currentUser != null ? getInstance().currentUser.getEmail() : "";
    }

    public static boolean isLoggedIn() {
        return getInstance().currentUser != null;
    }
    public static class UserData {
        private Map<String, User> users;

        public Map<String, User> getUsers() {
            return users;
        }

        public void setUsers(Map<String, User> users) {
            this.users = users;
        }
    }
}
