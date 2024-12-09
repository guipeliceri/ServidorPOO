package Client.Viewer;

import Common.CommandDRAW;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Properties;

public class ViewerClient {
    private static String SERVER_IP;
    private static int SERVER_PORT;

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private DrawPanel drawPanel;

    static {
        // Carregar configurações diretamente do arquivo config.properties
        String ip = "localhost";  // IP padrão
        int port = 12345;         // Porta padrão

        try (FileInputStream fis = new FileInputStream("src/config.properties")) {
            Properties properties = new Properties();
            properties.load(fis);

            ip = properties.getProperty("server.ip", ip);   // Carregar IP
            port = Integer.parseInt(properties.getProperty("server.port", String.valueOf(port))); // Carregar Porta
        } catch (IOException e) {
            System.err.println("Erro ao carregar configurações do arquivo: " + e.getMessage());
        }

        SERVER_IP = ip;
        SERVER_PORT = port;
    }

    public ViewerClient() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            // Envia ao servidor que este cliente é um "Viewer"
            output.writeObject("Viewer");

            // Cria e configura o painel de desenho
            drawPanel = new DrawPanel();
            JFrame frame = new JFrame("Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(drawPanel);
            frame.setVisible(true);

            // Recebe comandos de desenho do servidor
            receiveDrawCommands();

        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    private void receiveDrawCommands() {
        try {
            while (true) {
                CommandDRAW command = (CommandDRAW) input.readObject();
                if (command != null) {
                    drawPanel.addDrawCommand(command);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao receber comandos de desenho: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ViewerClient();
    }

    // Painel de desenho que recebe comandos do servidor
    private static class DrawPanel extends JPanel {
        private final java.util.List<CommandDRAW> drawCommands = new java.util.ArrayList<>();

        public void addDrawCommand(CommandDRAW command) {
            drawCommands.add(command);
            repaint(); // Repaint the panel to draw the new command
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (CommandDRAW command : drawCommands) {
                g.setColor(command.getColor());
                for (int i = 0; i < command.getPoints().size() - 1; i++) {
                    Point p1 = command.getPoints().get(i);
                    Point p2 = command.getPoints().get(i + 1);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }
}
