package Chat.Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    ListView<String> clientList;

    @FXML
    Button disconnectBtn;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private void setAuthorized(boolean isAuthorized) {
        if(!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
            disconnectBtn.setVisible(false);
            disconnectBtn.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
            disconnectBtn.setVisible(true);
            disconnectBtn.setManaged(true);
        }
    }

    private void connect() {

        try {
            String IP_ADDRESS = "localhost";
            int PORT = 8189;
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if(str.startsWith("/authOk")) {
                            setAuthorized(true);
                            break;
                        } else if (str.equals("/serverClosed")) {
                            return;
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if(str.startsWith("/")) {
                            if (str.equals("/serverClosed")) {
                                break;
                            }
                            if (str.startsWith("/clientList")) {
                                String[] tokens = str.split(" ");

                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!upperPanel.isVisible()) {
                        textArea.appendText("Вы вышли из чата!\n");
                    }
                    setAuthorized(false);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDisconnectBtn() {
        try {
            out.writeUTF("/end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        if (!loginField.getText().equals("") || !passwordField.getText().equals("")) {
            if(socket == null || socket.isClosed()) {
                connect();
            }
            try {
                out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
                loginField.clear();
                passwordField.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            textArea.appendText("Не указан логин/пароль!\n");
            loginField.clear();
            passwordField.clear();
        }
    }
}
