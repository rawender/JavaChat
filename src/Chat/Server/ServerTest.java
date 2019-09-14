package Chat.Server;

import Chat.Client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerTest {
    private HashMap<String, ClientHandler> clients;

    ServerTest(){
        clients = new HashMap <>();
        ServerSocket server = null;
        Socket socket = null;
        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");
            while (true) {
                socket = server.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(String nick, ClientHandler client) {
        clients.put(nick, client);
        broadcastClientList();
        System.out.println("Клиент подключился!");
    }

    public void unsubscribe(String nick) {
        clients.remove(nick);
        broadcastClientList();
        System.out.println("Клиент отключился!");
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o: clients.values()) {
            o.sendMsg(msg);
        }
    }

    public void sendPersonalMsg(ClientHandler sender, String nickTo, String msg) {
        if (notFind(nickTo)){
            sender.sendMsg(nickTo +" - отсутствующий пользователь.");
        } else if (sender.getNick().equals(nickTo)) {
            sender.sendMsg("Невозможно выполнить.");
        } else {
            for (Map.Entry<String, ClientHandler> o: clients.entrySet()) {
                if (o.getKey().equals(nickTo)) {
                    o.getValue().sendMsg("from " + sender.getNick() + ": " + msg);
                    sender.sendMsg("to " + nickTo + ": " + msg);
                }
            }
        }
    }

    public boolean notFind(String nick) {
        for (String o: clients.keySet()) {
            if (o.equals(nick)) {
                return false;
            }
        }return true;
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientList ");

        for (ClientHandler o: clients.values()) {
            sb.append(o.getNick()).append(" ");
        }

        String out = sb.toString();
        for (ClientHandler o: clients.values()) {
            o.sendMsg(out);
        }
    }
}
