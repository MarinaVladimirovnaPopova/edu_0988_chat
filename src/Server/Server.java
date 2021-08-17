package com.example.gui_caht_0988;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class HelloController {
    Socket socket;
    @FXML
    public HBox input_msg_box;
    @FXML
    Button snd_btn;
    @FXML
    Button cnt_btn;
    @FXML
    TextField messageField;
    @FXML
    TextArea TA_chat;
    @FXML
    TextArea usersOnlineTA;

    @FXML
    private void send() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String text = messageField.getText();
            out.writeUTF(text);
            messageField.clear(); // Очищаем поле ввода сообщения
            messageField.requestFocus(); // Возвращаем фокусировку на поле ввода
            TA_chat.appendText("Вы: " + text + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void connect() {
        try {
            socket = new Socket("localhost", 8188);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String response = "";
                        ArrayList<String> usersName = new ArrayList<String>();
                        try {
                            Object object = ois.readObject();
                            if (object.getClass().equals(usersName.getClass())) {
                                usersName = ((ArrayList<String>) object);
                                System.out.println(usersName);
                                usersOnlineTA.clear(); // Очищает TextArea
                                for (String userName : usersName) {
                                    usersOnlineTA.appendText(userName + "\n");
                                }
                            } else if (object.getClass().equals(response.getClass())) {
                                response = object.toString();
                                TA_chat.appendText(response + "\n");
                            } else {
                                TA_chat.appendText("Произошла ошибка");
                            }
                            cnt_btn.setVisible(false);
                            input_msg_box.setDisable(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            cnt_btn.setVisible(true);
                            input_msg_box.setDisable(true);
                        }
                    }
                }
            });
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
