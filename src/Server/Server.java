package Server;

import Common.CommandDRAW;

import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server {

    private boolean presenterConnected = false;
    private final List<Socket> viewers = new ArrayList<>();
    private final List<CommandDRAW> commandHistory = new ArrayList<>();

    public void start(String ip, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip))) {
            System.out.println("Servidor iniciado no IP: " + ip + ", Porta: " + port);

            while (true) {
                System.out.println("Aguardando conexões...");
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientType = in.readLine();

                if ("Presenter".equalsIgnoreCase(clientType)) {
                    if (!presenterConnected) {
                        presenterConnected = true;
                        System.out.println("Apresentador conectado: " + clientSocket.getInetAddress());
                        new Thread(new PresenterHandler(clientSocket)).start();
                    } else {
                        System.out.println("Já existe um apresentador conectado. Conexão rejeitada.");
                        clientSocket.close();
                    }
                } else if ("Viewer".equalsIgnoreCase(clientType)) {
                    if (presenterConnected) {
                        viewers.add(clientSocket);
                        System.out.println("Viewer conectado: " + clientSocket.getInetAddress());
                        new Thread(new ViewerHandler(clientSocket)).start();
                    } else {
                        System.out.println("Nenhum apresentador conectado. Conexão rejeitada.");
                        clientSocket.close();
                    }
                } else {
                    System.out.println("Tipo de cliente desconhecido. Conexão rejeitada.");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    // Starter do programa
    public static void main(String[] args) {
        Server server = new Server();
        String ip = "localhost";  // Default IP
        int port = 12345;         // Default Port

        // Carregar configurações diretamente do arquivo config.properties
        try (FileInputStream fis = new FileInputStream("src/config.properties")) {
            Properties properties = new Properties();
            properties.load(fis);

            ip = properties.getProperty("server.ip", ip);   // Carregar IP
            port = Integer.parseInt(properties.getProperty("server.port", String.valueOf(port))); // Carregar Porta
        } catch (IOException e) {
            System.err.println("Erro ao carregar configurações do arquivo: " + e.getMessage());
        }

        server.start(ip, port);
    }

    // Manipulador de conexões de apresentador
    private class PresenterHandler implements Runnable {
        private final Socket clientSocket;

        public PresenterHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                out.writeObject("Bem-vindo ao servidor, Apresentador!");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Mensagem do apresentador: " + message);
                    // Lógica para receber comandos de desenho do apresentador
                    if (message.startsWith("DRAW:")) {
                        // Exemplo: DRAW;255,0,0;10,10;100,100;150,150
                        String[] parts = message.split(";");
                        Color color = new Color(Integer.parseInt(parts[1].split(",")[0]),
                                Integer.parseInt(parts[1].split(",")[1]),
                                Integer.parseInt(parts[1].split(",")[2]));
                        List<Point> points = new ArrayList<>();
                        for (int i = 2; i < parts.length; i++) {
                            String[] coords = parts[i].split(",");
                            points.add(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
                        }

                        // Cria o comando de desenho com pontos e cor
                        CommandDRAW command = new CommandDRAW(points, color);
                        commandHistory.add(command);

                        // Envia o comando para todos os espectadores
                        for (Socket viewer : viewers) {
                            try (ObjectOutputStream viewerOut = new ObjectOutputStream(viewer.getOutputStream())) {
                                viewerOut.writeObject(command);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro na comunicação com o apresentador: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket do apresentador: " + e.getMessage());
                }
            }
        }
    }

    // Manipulador de conexões de espectador
    private class ViewerHandler implements Runnable {
        private final Socket clientSocket;

        public ViewerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                out.writeObject("Bem-vindo ao servidor, Expectador!");

                // Enviar todos os comandos de desenho já armazenados ao expectador
                for (CommandDRAW command : commandHistory) {
                    out.writeObject(command);
                }

                // Escutar novos comandos do servidor (enviados pelo apresentador)
                CommandDRAW command;
                while ((command = (CommandDRAW) in.readObject()) != null) {
                    // Replicar o comando para todos os outros espectadores
                    for (Socket viewer : viewers) {
                        if (!viewer.equals(clientSocket)) {
                            try (ObjectOutputStream viewerOut = new ObjectOutputStream(viewer.getOutputStream())) {
                                viewerOut.writeObject(command);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro na comunicação com o espectador: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket do espectador: " + e.getMessage());
                }
            }
        }
    }
}
