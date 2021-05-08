package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.NetworkPacketType;
import it.polimi.ingsw.network.common.sysmsg.ConnectionMessage;
import it.polimi.ingsw.network.common.ExSocket;
import it.polimi.ingsw.network.common.sysmsg.ISystemMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {

    private String hostName;
    private int hostPortNumber;

    private ExSocket socket;

    private Object lock;
    private boolean running;

    public GameClient(String hostName, int hostPortNumber){
        this.hostName = hostName;
        this.hostPortNumber = hostPortNumber;
        this.lock = new Object();
        this.running = false;
    }

    public ExSocket getSocket(){
        return socket;
    }

    public boolean isRunning(){
        synchronized (lock) {
            return running;
        }
    }

    public void execute(){

        if(isRunning()) return;
        setActive(true);

        try {
            socket = new ExSocket(new Socket(hostName, hostPortNumber));
            new Thread(new ClientConnectionHandler(this)).start();

        } catch (UnknownHostException ex){
            System.out.println("UnknownHostException while connecting.");
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void setActive(boolean state){
        synchronized (lock) {
            running = state;
        }
    }

    public void stop(){
        setActive(false);
        socket.close();
    }

    public void send(NetworkPacket np){
        socket.send(np);
    }

    public NetworkPacket recv (){
        return socket.receive();
    }




}
