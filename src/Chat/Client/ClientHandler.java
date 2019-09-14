package Chat.Client;

import Chat.Server.AuthService;
import Chat.Server.ServerTest;

import java.io.*;
import java.net.Socket;

public class ClientHandler {

    private DataOutputStream out;
    private DataInputStream in;
    private String nick;

    public ClientHandler(ServerTest server, Socket socket) {
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            int timeout = 300000;
            socket.setSoTimeout(timeout);

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] tokens = str.split(" ");
                            String newNick = AuthService.getNickLoginAndPass(tokens[1], tokens[2]);
                            if (newNick != null) {
                                if (server.notFind(newNick)) {
                                    sendMsg("/authOk");
                                    nick = newNick;
                                    server.subscribe(nick, ClientHandler.this);
                                    sendMsg("Вы вошли в чат!");
                                    break;
                                } else {
                                    sendMsg("Аккаунт уже используется!");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль!");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if(str.startsWith("/")) {
                            if(str.equals("/end")) {
                                out.writeUTF("/serverClosed");
                                break;
                            }
                            if(str.startsWith("/w ")) {
                                String[] tokens = str.split(" ",3);
                                server.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);
                            }
                        } else {
                            server.broadcastMsg(nick + ": " + str);
                        }
                    }
                } catch(InterruptedIOException e) {
                    if (nick != null) {
                        sendMsg("Вы долго отсутствовали!");
                        try {
                            out.writeUTF("/serverClosed");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        try {
                            out.writeUTF("/serverClosed");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (nick != null) {
                        server.unsubscribe(nick);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
