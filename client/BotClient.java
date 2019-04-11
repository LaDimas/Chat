package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class BotClient extends Client {
    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        System.out.println(botClient.getUserName());
        botClient.run();
    }



    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                String[] inMessage = message.split(":");
                String name = inMessage[0];
                String data = inMessage[1].trim();
                SimpleDateFormat dateFormat;
                switch (data) {
                    case "дата": {
                        dateFormat = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    }
                    case "день": {
                        dateFormat = new SimpleDateFormat("d");
                        break;

                    }
                    case "месяц": {
                        dateFormat = new SimpleDateFormat("MMMM");
                        break;
                    }
                    case "год": {
                        dateFormat = new SimpleDateFormat("YYYY");
                        break;
                    }
                    case "время": {
                        dateFormat = new SimpleDateFormat("H:mm:ss");
                        break;
                    }
                    case "час": {
                        dateFormat = new SimpleDateFormat("H");
                        break;
                    }
                    case "минуты": {
                        dateFormat = new SimpleDateFormat("m");
                        break;
                    }
                    case "секунды": {
                        dateFormat = new SimpleDateFormat("s");
                        break;
                    }
                    default:
                        dateFormat = null;
                        break;
                }
                if (dateFormat != null) {
                    Calendar calendar = Calendar.getInstance();
                    String result = "Информация для " + name + ": ";
                    result += dateFormat.format(calendar.getTime());
                    sendTextMessage(result);
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%d",(int)(Math.random() * 100));
    }
}
