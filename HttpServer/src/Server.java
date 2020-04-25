import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private int port;
    private String directory; // путь к папке, файлы из которой будем раздавать

    public Server(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            while (true) {
                Socket socket = server.accept();
                /* при помощи accept() ждём, пока с сервером кто-то соединится.
				   И когда это произойдёт, возвратим объект типа Socket,
				   т.е. воссозданный клиентский сокет
				*/
                /* Теперь, когда сокет клиента создан на стороне сервера,
				   можно начинать двухстороннее общение
				*/
                Thread thread = new Handler(socket, this.directory);
                thread.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //int port = Integer.parseInt(args[0]);
        //String directory = args[1];
        int port = 9090;
        String directory = "files";
        new Server(port, directory).start();
    }
}