package com.javarush.task.task30.task3008;
import com.javarush.task.task30.task3008.client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
             serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен.");
            while (true){
                Socket clientSocket = serverSocket.accept();
                Handler handler = new Handler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

    }
    public static void sendBroadcastMessage(Message message){
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            }catch (IOException e){
                System.out.println("Сообщение не отправлено");
            }
        }
    }
    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run(){
            ConsoleHelper.writeMessage("Установлено соединение с" + socket.getRemoteSocketAddress());
            String userName = "";
            try(Connection connection= new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));
                notifyUsers(connection,userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }catch (IOException e){ConsoleHelper.writeMessage("Error");
            }catch (ClassNotFoundException x){ConsoleHelper.writeMessage("Error");}
        }
        
        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (String name : connectionMap.keySet()) {
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message in = connection.receive();
                if (in.getType()==(MessageType.TEXT)){
                    Message message = new Message(MessageType.TEXT, userName + ": " + in.getData());
                    sendBroadcastMessage(message);
                }else ConsoleHelper.writeMessage("Ошибка ввода текста");
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            String name = "";
            Message message = new Message(MessageType.NAME_REQUEST);
            connection.send(message);
            Message in = connection.receive();
            name = in.getData();
            if (in.getType().equals(MessageType.USER_NAME)) {
                if (!name.equals("")){
                    if(!connectionMap.containsKey(in.getData())) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                    }else return serverHandshake(connection);
                }else return serverHandshake(connection);
            } else return serverHandshake(connection);
            return name;
        }
    }
}
