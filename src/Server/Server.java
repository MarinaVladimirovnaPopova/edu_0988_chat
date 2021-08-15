package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {
    public static void main(String[] args) {
        {
            ArrayList<User> users = new ArrayList<>();
            try {
                ServerSocket serverSocket = new ServerSocket(8168); // Создаём серверный сокет
                System.out.println("Сервер запущен");
                while (true) { // Бесконечный цикл для ожидания родключения клиентов
                    Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                    System.out.println("Клиент подключился");
                    User currentUser = new User(socket);
                    users.add(currentUser);
                    DataInputStream in = new DataInputStream(currentUser.getSocket().getInputStream()); // Поток ввода
                    DataOutputStream out = new DataOutputStream(currentUser.getSocket().getOutputStream()); // Поток вывода
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                out.writeUTF("Добро пожаловать на сервер");
                                out.writeUTF("Введите ваше имя: ");
                                String userName = in.readUTF(); // Ожидаем имя от клиента
                                while (FreeName(users, userName)) { // проверка имени
                                    System.out.println("клиент ввел имя, используемое другим клиентом");
                                    out.writeUTF("Это имя - " + userName + " - уже занято, выберете другое имя.");
                                    userName = in.readUTF();

                                }
                                currentUser.setUserName(userName);
                                System.out.println(currentUser.getUserName() + " присоединяется к беседе");

                                for (User user : users) {
                                    DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                    out.writeUTF(currentUser.getUserName() + " присоединяется к беседе");
                                }
                                while (true) {
                                    String request = in.readUTF(); // Ждём сообщение от пользователя
                                    System.out.println(currentUser.getUserName() + ": " + request);
                                    if (request.contains("/m")) {
                                        privat(request, users, currentUser);
                                    }
                                    for (User user : users) {
                                        if (users.indexOf(user) == users.indexOf(currentUser)) continue;
                                        DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                        out.writeUTF(currentUser.getUserName() + ": " + request);
                                    }
                                }
                            } catch (IOException e) {
                                users.remove(currentUser);
                                for (User user : users) {
                                    try {
                                        DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                        out.writeUTF(currentUser.getUserName() + " покинул(а) чат");
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean FreeName(ArrayList<User> users, String name) {
        for (User user : users) {
            if (user.getUserName() != null && user.getUserName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static void privat(String request, ArrayList<User> users, User currentUser) throws IOException {
        User mUser = null;
        String[] split = (request.split(" ", 3));
        String name = "";
        DataOutputStream out = new DataOutputStream(currentUser.getSocket().getOutputStream());

            name = split[1];
        

        for (User user : users) {
            if (user.getUserName().equals(name)) {
                mUser = user;
                break;
            }
        }
        if (mUser != null) {
            DataOutputStream recipientOut = new DataOutputStream(mUser.getSocket().getOutputStream());
            recipientOut.writeUTF(currentUser.getUserName() + ": " + request);
        } else {
            out.writeUTF("Пользователя с именем " + name + " нет");
        }
    }
}
