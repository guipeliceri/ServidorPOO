package Client.Presenter;

import Common.CommandDRAW;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Properties;

public class PresenterClient {
    private static String SERVER_IP;
    private static int SERVER_PORT;

    private Socket socket;
    private ObjectOutputStream output;
    private BufferedReader input;

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

    public PresenterClient() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new ObjectOutputStream(socket.getOutputStream());

            // Envia ao servidor que este cliente é um "Presenter"
            output.writeObject("Presenter");

            // Cria e configura o painel de desenho
            drawPanel = new DrawPanel();
            JFrame frame = new JFrame("Presenter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(drawPanel);
            frame.setVisible(true);

            // Recebe a confirmação de conexão
            input.readLine();  // Aguarda a mensagem de boas-vindas

            // Envia comandos de desenho
            sendDrawCommand();

        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    // Método para enviar comando de desenho
    private void sendDrawCommand() {
        // Este é um exemplo para enviar um comando de desenho
        // Você pode personalizar para capturar o desenho do painel, por exemplo
        List<Point> points = List.of(new Point(50, 50), new Point(150, 150), new Point(250, 50));
        Color color = Color.RED;
        CommandDRAW command = new CommandDRAW(points, color);

        try {
            // Envia o comando de desenho para o servidor
            output.writeObject(command);
            System.out.println("Comando de desenho enviado ao servidor.");
        } catch (IOException e) {
            System.err.println("Erro ao enviar comando de desenho: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PresenterClient();
    }

    // Painel de desenho que envia comandos de desenho para o servidor
    private class DrawPanel extends JPanel {
        private final java.util.List<Point> points = new java.util.ArrayList<>();
        private Color currentColor = Color.BLACK;

        public DrawPanel() {
            // Configura o painel para capturar eventos de mouse
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    points.add(e.getPoint());
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(currentColor);
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Enviar o comando de desenho para o servidor
        public void sendDrawCommand() {
            CommandDRAW command = new CommandDRAW(points, currentColor);
            try {
                // Enviar para o servidor
                output.writeObject(command);
                System.out.println("Comando de desenho enviado.");
            } catch (IOException e) {
                System.err.println("Erro ao enviar o comando de desenho: " + e.getMessage());
            }
        }
    }
}
