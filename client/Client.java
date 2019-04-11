package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread{
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{

        public void run(){
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                System.out.println("Поздоровались");
                clientMainLoop();
                System.out.println("Работаем");
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true){
                Message in = connection.receive();
                if (in.getType() == MessageType.NAME_REQUEST) {
                    Message message = new Message(MessageType.USER_NAME, getUserName());
                    connection.send(message);
                }else {
                    if(in.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                    }else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message in = connection.receive();
                if (in.getType() == MessageType.TEXT) processIncomingMessage(in.getData());
                else if (in.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(in.getData());
                else if (in.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(in.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " присоединился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }
    protected void notifyConnectionStatusChanged(boolean cc){
        Client.this.clientConnected = cc;
        synchronized (Client.this) {
            Client.this.notify();
        }
    }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this){
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка соединения");
                return;
            }
        }
        if (clientConnected) ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (clientConnected){
            String message = ConsoleHelper.readString();
            if (message.equals("exit"))break;
            if (shouldSendTextFromConsole()) sendTextMessage(message);
        }
    }

    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            Message message = new Message(MessageType.TEXT,text);
            connection.send(message);
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage("Ошибка передачи");
        }
    }
}
