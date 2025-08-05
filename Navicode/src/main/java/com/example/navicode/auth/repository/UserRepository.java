package com.example.navicode.auth.repository;

import com.example.navicode.auth.model.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Repository
public class UserRepository {

    private List<User> users = new ArrayList<>();
    private static final String CSV_FILE_PATH = "users.csv";
    private Long nextId = 1L;

    public UserRepository() {
        loadUsersFromCSV();
    }

    private void loadUsersFromCSV() {
        try {
            File file = new File(CSV_FILE_PATH);
            if (!file.exists()) {
                // 기본 admin 계정 생성
                createDefaultUser();
                return;
            }

            try (Reader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : csvParser) {
                    User user = new User(
                        Long.parseLong(record.get(0)), // id
                        record.get(1), // username
                        record.get(2)  // password (이미 암호화된 상태)
                    );
                    users.add(user);

                    // nextId 업데이트
                    if (user.getId() >= nextId) {
                        nextId = user.getId() + 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultUser();
        }
    }

    private void createDefaultUser() {
        // 기본 admin 계정 (비밀번호: admin123)
        User admin = new User(1L, "admin", "$2a$10$7BYhYXJHNBEBVBLXC9hOaOgEqW.n9VUbHJGJKW6w.R8gX.k7vwXCm");
        users.add(admin);
        nextId = 2L;
        saveUsersToCSV();
    }

    private void saveUsersToCSV() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_FILE_PATH), Charset.forName("UTF-8")))) {

            // Write header
            writer.println("id,username,password");

            // Write data
            for (User user : users) {
                writer.printf("%d,%s,%s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getPassword()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst();
    }

    public boolean existsByUsername(String username) {
        return users.stream()
            .anyMatch(user -> user.getUsername().equals(username));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
            users.add(user);
        } else {
            // 기존 사용자 업데이트
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(user);
        }

        saveUsersToCSV();
        return user;
    }

    public void deleteById(Long id) {
        users.removeIf(user -> user.getId().equals(id));
        saveUsersToCSV();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
