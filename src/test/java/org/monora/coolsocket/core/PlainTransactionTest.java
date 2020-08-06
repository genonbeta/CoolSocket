package org.monora.coolsocket.core;

import org.monora.coolsocket.core.response.Response;
import org.monora.coolsocket.core.variant.BlockingCoolSocket;
import org.monora.coolsocket.core.variant.StaticMessageCoolSocket;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PlainTransactionTest
{
    private static final int PORT = 5506;

    @Test
    public void receiveTextDataTest() throws IOException, InterruptedException
    {
        final String message = "The quick brown fox jumped over the lazy dog!";

        StaticMessageCoolSocket coolSocket = new StaticMessageCoolSocket(PORT);
        coolSocket.setStaticMessage(message);
        coolSocket.start();

        ActiveConnection activeConnection = ActiveConnection.connect(new InetSocketAddress(PORT), 0);
        Response response = activeConnection.receive();

        activeConnection.close();
        coolSocket.stop();

        Assert.assertTrue("The response should have a body.", response.containsData());
        Assert.assertEquals("The sent and received msg should be the same.", message, response.getAsString());
    }

    @Test(timeout = 3000)
    public void sendTextDataTest() throws IOException, InterruptedException
    {
        final String message = "Almost before we knew it, we had left the ground.";

        BlockingCoolSocket coolSocket = new BlockingCoolSocket(PORT);
        coolSocket.start();

        ActiveConnection activeConnection = ActiveConnection.connect(new InetSocketAddress(PORT), 0);
        activeConnection.reply(message);

        Response response = coolSocket.waitForResponse();
        Assert.assertEquals("The messages should be same", message, response.getAsString());

        activeConnection.close();
        coolSocket.stop();
    }

    @Test
    public void receivedDataHasValidInfoTest() throws IOException, InterruptedException
    {
        final String message = "Stop acting so small. You are the universe in ecstatic motion.";

        StaticMessageCoolSocket coolSocket = new StaticMessageCoolSocket(PORT);
        coolSocket.setStaticMessage(message);
        coolSocket.start();

        ActiveConnection activeConnection = ActiveConnection.connect(new InetSocketAddress(PORT), 0);
        Response response = activeConnection.receive();

        activeConnection.close();
        coolSocket.stop();

        Assert.assertEquals("The length should be the same.", message.length(), response.length);
    }

    @Test
    public void multiplePartDeliveryTest() throws IOException, InterruptedException
    {
        final JSONObject headerJson = new JSONObject()
                .put("key1", "value1")
                .put("key2", 2);

        BlockingCoolSocket coolSocket = new BlockingCoolSocket(PORT);
        coolSocket.start();

        ActiveConnection activeConnection = ActiveConnection.connect(new InetSocketAddress(PORT), 0);
        activeConnection.reply(headerJson.toString());

        Response response = coolSocket.waitForResponse();
        JSONObject remoteHeader = response.getAsJson();

        coolSocket.stop();
        activeConnection.close();

        Assert.assertEquals("The JSON indexes should match.", headerJson.length(), remoteHeader.length());
        Assert.assertEquals("The length of the headers as texts should match.", headerJson.toString().length(),
                response.length);

        for (String key : headerJson.keySet())
            Assert.assertEquals("The keys in both JSON objects should be visible with the same value.",
                    headerJson.get(key), remoteHeader.get(key));
    }

    @Test
    public void directionlessDeliveryTest() throws IOException, InterruptedException
    {
        final String message = "Back to the days of Yore when we were sure of a good long summer.";
        final int loops = 20;

        CoolSocket coolSocket = new CoolSocket(PORT)
        {
            @Override
            public void onConnected(ActiveConnection activeConnection)
            {
                for (int i = 0; i < loops; i++) {
                    try {
                        activeConnection.receive();
                        activeConnection.reply(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        coolSocket.start();

        ActiveConnection activeConnection = ActiveConnection.connect(new InetSocketAddress(PORT), 0);

        for (int i = 0; i < loops; i++) {
            activeConnection.reply(message);
            Assert.assertEquals("The message should match with the original.", message,
                    activeConnection.receive().getAsString());
        }

        activeConnection.close();
        coolSocket.stop();
    }
}