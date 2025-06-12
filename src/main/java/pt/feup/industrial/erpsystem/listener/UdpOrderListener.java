package pt.feup.industrial.erpsystem.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.feup.industrial.erpsystem.dto.ClientOrderRequestDto;
import pt.feup.industrial.erpsystem.service.OrderService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class UdpOrderListener implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UdpOrderListener.class);

    @Value("${erp.udp.port:24680}")
    private int udpPort;

    @Value("${erp.udp.buffer-size:1024}")
    private int bufferSize;

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final ExecutorService packetHandlerExecutor = Executors.newCachedThreadPool();

    @Autowired
    public UdpOrderListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread listenerThread = new Thread(this::listen);
        listenerThread.setName("UDP-Order-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listen() {
        log.info("Starting UDP listener on port {}", udpPort);
        try (DatagramSocket socket = new DatagramSocket(udpPort, null)) {
            byte[] buffer = new byte[bufferSize];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    packetHandlerExecutor.submit(() -> processPacket(packet));
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        log.error("Error receiving UDP packet", e);
                    } else {
                        log.info("UDP Socket closed.");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to start UDP listener on port {}", udpPort, e);
        } finally {
            packetHandlerExecutor.shutdown();
            log.info("UDP listener stopped.");
        }
    }

    private void processPacket(DatagramPacket packet) {
        String receivedData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        log.info("Received UDP data from {}:{}: {}", packet.getAddress(), packet.getPort(), receivedData);

        try {
            ClientOrderRequestDto orderRequest = objectMapper.readValue(receivedData, ClientOrderRequestDto.class);
            orderService.processAndSaveOrder(orderRequest);
        } catch (Exception e) {
            log.error("Failed to parse or process order from UDP packet: {}", receivedData, e);
        }
    }
}