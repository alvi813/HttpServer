import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Date;

public class Handler extends Thread {

    private static final Map<String, String> CONTENT_TYPES = new HashMap<String, String>() {{
        put("bmp", "image/bmp");
        put("doc", "application/msword");
        put("html", "text/html");
        put("jpe", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("jpg", "image/jpeg");
        put("js", "application/javascript");
        put("txt", "text/plain");
    }};

    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";

    private Socket socket;

    private String directory;

    Handler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (InputStream input = this.socket.getInputStream(); OutputStream output = this.socket.getOutputStream()) {
            String url = this.getRequestUrl(input); //указывает путь к желаемому файлу
            Path filePath = Paths.get(this.directory, url);

            //проверяем, есть ли такой путь и не является ли директорией
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String extension = this.getFileExtension(filePath);
                String type = CONTENT_TYPES.get(extension);
                byte[] fileBytes = Files.readAllBytes(filePath); // получаем массив байт файла
                this.sendHeader(output, 200, "OK", type, fileBytes.length);
                output.write(fileBytes);
            } else {
                String type = null;
                this.sendHeader(output, 404, "Not Found", type, NOT_FOUND_MESSAGE.length());
                //в метод write() OutputStream'а передаём массив байт сообщения NOT_FOUND_MESSAGE:
                output.write(NOT_FOUND_MESSAGE.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRequestUrl(InputStream input) {
        Scanner reader = new Scanner(input).useDelimiter("\r\n");
        String line = reader.next();
        String[] startingLine = line.split(" ");
        String url = startingLine[1];
        return url;
		/*
		Starting line запроса содержит:
		GET    /path    HTTP/1.1
		метод  /URL     версия_протокола
		-> таким образом, получим URL
		*/

    }

    // метод для получения расширения файла
    private String getFileExtension(Path path) {
        Path fileName = path.getFileName();
        String stringFileName = fileName.toString();
		/* GetFileName() метод java.nio.file.Path используется для получения имени файла или каталога,
		на который указывает путь этого объекта */
        int extensionStart = stringFileName.lastIndexOf(".");
		/* lastIndexOf() возвращает индекс последнего вхождения "." или -1, если "." не встречается
		   Получаем индекс символа, с которого начинается расширение, т.е. индекс точки "." */
        String extension = extensionStart == -1 ? "" : stringFileName.substring(extensionStart + 1);
		/* public String substring(int beginIndex)
           или
		   public String substring(int beginIndex, int endIndex)
		   //--------------------------------------------------------------
		   substring() в Java имеет два варианта и возвращает новую строку,
		   которая является подстрокой данной строки.
		   Подстрока начинается с символа, заданного индексом, и продолжается до конца данной строки
		   или до endIndex-1, если введен второй аргумент */
        return extension;
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long length) {
        PrintStream ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %d %s \r\n", statusCode, statusText);
        ps.printf("Content-Type: %s \r\n", type);
        ps.print("Date: " + new Date() + "\r\n");
        ps.print("Server: MyServer \r\n");
        ps.printf("Content-Length: %d \r\n", length);
        ps.print("Connection: close \r\n\r\n");

    }
}
