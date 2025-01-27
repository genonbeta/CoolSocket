package org.monora.coolsocket.core.config;

import org.jetbrains.annotations.NotNull;
import org.monora.coolsocket.core.session.Channel;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * This factory class contract configures the sockets when they are created so that extra management can be passed on
 * to a 3rd party class that doesn't live under the CoolSocket library.
 */
public interface ConfigFactory {
    /**
     * Configure the server socket for the server side before the usage. Throws whatever the error it faces due to a
     * misconfiguration.
     *
     * @param serverSocket To be configured.
     * @throws IOException When an unrecoverable error occurs due to misconfiguration.
     */
    void configureServer(@NotNull ServerSocket serverSocket) throws IOException;

    /**
     * Produce a {@link ServerSocket} instance preconfigured with {@link #configureServer(ServerSocket)}. It is up to
     * you make whether the server socket will be a part of a SSL context.
     *
     * @return A preconfigured server socket instance.
     * @throws IOException If the factory fails to create the server socket instance due to misconfiguration.
     */
    ServerSocket createServer() throws IOException;

    /**
     * Configure the socket connection to a client before its actual usage and produce an {@link Channel}
     * instance. The configuration may be different from that of {@link ServerSocket#accept()} assigns.
     *
     * @param client To configure.
     * @return The configured socket encapsulated in a {@link Channel}.
     * @throws SocketException When an unrecoverable error occurs due to misconfiguration.
     */
    Channel configureClient(@NotNull Socket client) throws IOException;

    /**
     * The address that the upcoming products will be assigned to. This does not necessarily reflect the address
     * the server is serving on.
     *
     * @return The address that the server will be bound to.
     */
    SocketAddress getSocketAddress();

    /**
     * Get the server port assigned to the server sockets coming out of this factory. This does not necessarily reflect
     * the ports assigned previous products.
     *
     * @return The port number assigned by this factory instance.
     */
    default int getPort() {
        return getSocketAddress() instanceof InetSocketAddress ? ((InetSocketAddress) getSocketAddress()).getPort() : 0;
    }

    /**
     * Time to wait for each client before throwing an error, {@link java.util.concurrent.TimeoutException} in the case
     * case {@link ServerSocket#accept()}.
     *
     * @param milliSeconds The max time to wait in milliseconds.
     * @see ServerSocket#setSoTimeout(int)
     * @see ServerSocket#accept()
     */
    void setAcceptTimeout(int milliSeconds);

    /**
     * Read timeout in any scenario. This doesn't affect existing instances. A "0" (zero) value will mean to wait
     * indefinitely.
     *
     * @param milliSeconds The max time to wait in milliseconds.
     * @see Socket#setSoTimeout(int)
     * @see InputStream#read()
     */
    void setReadTimeout(int milliSeconds);

    /**
     * Set socket address for the server.
     *
     * @param socketAddress To be used with server socket.
     */
    void setSocketAddress(@NotNull SocketAddress socketAddress);
}
